package com.tomoto.glass.njslyr;

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
import android.os.Handler;
import android.text.format.Time;
import android.view.Menu;

import com.google.android.glass.app.Card;
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
	
	private List<StoryModel> storyModels;

	private class RootCardScrollAdapter extends AbstractCardScrollAdapter<StoryModel> {
		public RootCardScrollAdapter(Context context, List<StoryModel> storyModels) {
			this.items = storyModels;
			
			cards = new ArrayList<Card>();
			for (StoryModel sm : storyModels) {
				Card card = new Card(context);
				card.setText(sm.getTitle());
				card.setFootnote(R.string.select_story);
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
					StoryModel selectedStory = (StoryModel) cardScrollAdapter.getItem(cardScrollView.getSelectedItemPosition());
					drillDownToStory(selectedStory, 0);
				} else {
					float azimuth = om.getAzimuth();
					int max = cardScrollAdapter.getCount();
					int rawIndex = (int) Math.floor((azimuth + 180) / 360 * 2 * max);
					cardScrollView.setSelection(rawIndex % max);
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
		
		final int storyIndex = pref.getInt(SavedStateConstants.CURRENT_STORY_INDEX, -1);
		if (storyIndex < 0) {
			return;
		}
		
		final int lineIndex = pref.getInt(SavedStateConstants.CURRENT_LINE_INDEX, 0);
		
		drillDownToStoryInternal(storyModels.get(storyIndex), lineIndex);
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

	@Override
	protected void onResume() {
		super.onResume();
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		orientationManager.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		orientationManager.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.root, menu);
		return true;
	}

}
