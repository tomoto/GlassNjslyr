package com.tomoto.glass.njslyr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;
import com.tomoto.glass.model.factory.StoryModelFactory;
import com.tomoto.glass.njslyr.model.StoryModel;

public class RootActivity extends Activity {

	public static final String EXTRA_STORY = "story";
	public static final String EXTRA_LINE = "line";
	
	private CardScrollView cardScrollView;
	private RootCardScrollAdapter cardScrollAdapter;
	private OrientationManager orientationManager;
	
	private int baseSelectedIndex;
	private float baseAzimuth;
	private boolean isBaseAzimuthValid;
	private long lastDetectedTime;
	
	private List<StoryModel> storyModels;

	private class RootCardScrollAdapter extends AbstractCardScrollAdapter<StoryModel> {
		public RootCardScrollAdapter(Context context, List<StoryModel> storyModels) {
			this.items = storyModels;
			
			cards = new ArrayList<Card>();
			for (StoryModel sm : storyModels) {
				Card card = new Card(context);
				card.setText(MessageFormat.format("{0} ({1}/{2})", sm.getTitle(), sm.getIndex()+1, storyModels.size()));
				card.setFootnote(R.string.select_story);
				card.setImageLayout(ImageLayout.FULL);
				cards.add(card);
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientationManager = new OrientationManager(sensorManager, new OrientationManager.Listener() {
			@Override
			public void onSensorChanged(OrientationManager om) {
				if (om.getPitch() > 30) {
					// Ojigi
					drillDownToCurrentlySelectedStory();
				} else {
					float azimuth = om.getAzimuth();
					
					float faceDirection = azimuth - baseAzimuth;
					while (faceDirection <= -180) faceDirection += 360;
					while (faceDirection > 180) faceDirection -= 360;
					
					long currentTime = System.currentTimeMillis();
					if (isBaseAzimuthValid && Math.abs(faceDirection) > 15 && Math.abs(faceDirection) < 90) {
						Log.i("Gouranga", "" + faceDirection);
						if (currentTime - lastDetectedTime > 250) {
							int index = cardScrollView.getSelectedItemPosition();
							index += (faceDirection < 0) ? 1 : -1;
							index = Math.min(Math.max(0,  index), storyModels.size() - 1);
							cardScrollView.setSelection(index);
							lastDetectedTime = currentTime;
						}
					} else if (!isBaseAzimuthValid || currentTime - lastDetectedTime > 3000) {
						lastDetectedTime = currentTime;
						baseSelectedIndex = cardScrollView.getSelectedItemPosition();
						baseAzimuth = azimuth;
						isBaseAzimuthValid = true;
					}
				}
			}
		});
		
		cardScrollView = new CardScrollView(this);
		storyModels = Arrays.asList(StoryModelFactory.getStoryModels(getResources()));
		cardScrollAdapter = new RootCardScrollAdapter(this, storyModels);
		cardScrollView.setAdapter(cardScrollAdapter);
		cardScrollView.activate();
		setContentView(cardScrollView);
		
		restoreState();
	}
	
	private void restoreState() {
		SharedPreferences pref = getSharedPreferences(SavedStateConstants.NAME, MODE_PRIVATE);
		
		int storyIndex = pref.getInt(SavedStateConstants.CURRENT_STORY_INDEX, -1);
		if (storyIndex < 0) {
			return;
		}
		cardScrollView.setSelection(storyIndex);
		
		// final int lineIndex = pref.getInt(SavedStateConstants.CURRENT_LINE_INDEX, 0);
		
		// drillDownToStoryInternal(storyModels.get(storyIndex), lineIndex);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		orientationManager.start();
		isBaseAzimuthValid = false;
		
		highlightPreviouslySelectedCard();
	}

	private void highlightPreviouslySelectedCard() {
		for (Card card : cardScrollAdapter.cards) {
			card.clearImages();
			card.addImage(R.drawable.ic_black);
			card.setFootnote(R.string.select_story);
		}
		
		SharedPreferences pref = getSharedPreferences(SavedStateConstants.NAME, MODE_PRIVATE);
		
		int storyIndex = pref.getInt(SavedStateConstants.CURRENT_STORY_INDEX, -1);
		if (storyIndex >= 0) {
			Card card = cardScrollAdapter.cards.get(storyIndex); 
			card.clearImages();
			card.addImage(R.drawable.ic_selected);
			card.setFootnote(R.string.continue_story);
		}
		
		cardScrollView.updateViews(true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		orientationManager.stop();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			drillDownToCurrentlySelectedStory();
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void drillDownToCurrentlySelectedStory() {
		int storyIndex = cardScrollView.getSelectedItemPosition();
		
		SharedPreferences pref = getSharedPreferences(SavedStateConstants.NAME, MODE_PRIVATE);
		
		int savedStoryIndex = pref.getInt(SavedStateConstants.CURRENT_STORY_INDEX, -1);
		int lineIndex = 0;
		if (storyIndex == savedStoryIndex) {
			lineIndex = pref.getInt(SavedStateConstants.CURRENT_LINE_INDEX, 0);
		}
		
		drillDownToStory(storyModels.get(storyIndex), lineIndex);
	}
	
	protected void drillDownToStory(StoryModel story, int line) {
		if (!orientationManager.isStarted()) {
			return;
		}
		
    	orientationManager.stop();
    	
    	drillDownToStoryInternal(story, line);
	}
	
	private void drillDownToStoryInternal(StoryModel story, int line) {
    	AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	audio.playSoundEffect(Sounds.TAP);
    	
    	Intent intent = new Intent(this, MainActivity.class);
    	intent.putExtra(EXTRA_STORY, story);
    	intent.putExtra(EXTRA_LINE, line);
    	startActivity(intent);
	}

}
