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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;
import com.tomoto.glass.model.factory.StoryModelFactory;
import com.tomoto.glass.model.util.MathUtils;
import com.tomoto.glass.njslyr.model.StoryModel;

public class RootActivity extends Activity {

	public static final String EXTRA_STORY = "story";
	public static final String EXTRA_LINE = "line";
	
	private CardScrollView cardScrollView;
	private RootCardScrollAdapter cardScrollAdapter;
	private OrientationManager orientationManager;
	private boolean transitionStarted;
	
	private int baseSelectedIndex;
	private float baseHeading;
	private boolean isBaseHeadingValid;
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
		orientationManager = new OrientationManager(sensorManager, null);
		orientationManager.addOnChangedListener(new OrientationManager.OnChangedListener() {
			
			@Override
			public void onOrientationChanged(OrientationManager om) {
				if (om.getPitch() > 30) {
					// Ojigi
					drillDownToCurrentlySelectedStory();
				} else {
					float heading = om.getHeading();
					long currentTime = System.currentTimeMillis();
					
					if (!isBaseHeadingValid) {
						lastDetectedTime = currentTime;
						baseSelectedIndex = cardScrollView.getSelectedItemPosition();
						baseHeading = heading;
						isBaseHeadingValid = true;
						return;
					}
					
					float direction = MathUtils.mod(heading - baseHeading + 180, 360) - 180;
					float adjustedDirection = direction > 15 ? direction - 15 : direction < -15 ? direction + 15 : 0;  
					
					int index = (int) MathUtils.mod(baseSelectedIndex + adjustedDirection / (360 - 15 * 2) * storyModels.size(), storyModels.size());
					
					cardScrollView.setSelection(index);
				}
			}
			
			@Override
			public void onLocationChanged(OrientationManager orientationManager) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAccuracyChanged(OrientationManager orientationManager) {
				// TODO Auto-generated method stub
				
			}
		});
		
		cardScrollView = new CardScrollView(this);
		storyModels = Arrays.asList(StoryModelFactory.getStoryModels(getResources()));
		cardScrollAdapter = new RootCardScrollAdapter(this, storyModels);
		cardScrollView.setAdapter(cardScrollAdapter);
		cardScrollView.activate();
		setContentView(cardScrollView);
		
		cardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long it) {
				drillDownToCurrentlySelectedStory();
			}
		});
		
		restoreState();
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// If user started to use the finger, stop the orientation sensing.
			orientationManager.stop();
		}
		return super.onGenericMotionEvent(event);
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
		isBaseHeadingValid = false;
		transitionStarted = false;
		
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
		if (transitionStarted) {
			return; // guard from reentrance
		}
		transitionStarted = true;
		
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
