package com.tomoto.glass.njslyr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;
import com.tomoto.glass.njslyr.model.StoryModel;
import com.tomoto.glass.njslyr.model.TextLineModel;

public class MainActivity extends Activity {
	
//	private SensorManager sensorManager;
//	private Sensor gravitySensor;

	private StoryModel storyModel;
	
	private CardScrollView cardScrollView;
	private GourangaCardScrollAdapter cardScrollAdapter;
	
	private TextToSpeech tts;
	// private UtteranceProgressListener ttsListener; // Doesn't work...
	
	// Workaround for TTS listener
	private TTSWatcher ttsw;
	private TTSWatcher.Listener ttswListener = new TTSWatcher.Listener() {
		@Override
		public void onStart() {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		@Override
		public void onStop(boolean force) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			
			if (!force) {
				goToNextPage();
			}
		}
		
	};
	
//	private SensorEventListener sensorEventListener = new SensorEventListener() {
//		private static final float THRESHOLD = 0.5f;
//		long lastSensedTime = 0;
//		
//		@Override
//		public void onSensorChanged(SensorEvent event) {
//			if (event.values[2] < -THRESHOLD) {
//				long currentTime = System.currentTimeMillis();
//				if (currentTime - lastSensedTime > 1000) {
//					int position = cardScrollView.getSelectedItemPosition();
//					if (position < cardScrollAdapter.getCount() - 1) {
//						cardScrollView.setSelection(position + 1);
//					}
//				}
//				lastSensedTime = currentTime;
//			}
//		}
//		
//		@Override
//		public void onAccuracyChanged(Sensor sensor, int accuracy) {
//			// Nothing to do
//		}
//	};
	
	private class GourangaCardScrollAdapter extends AbstractCardScrollAdapter<TextLineModel> {
		public GourangaCardScrollAdapter(Context context, List<TextLineModel> textLineModels) {
			this.items = textLineModels;
			
			cards = new ArrayList<Card>();
			for (TextLineModel tlm : textLineModels) {
				String text = MessageFormat.format("{0} ({1}/{2})", tlm.getText(), tlm.getIndex()+1, textLineModels.size());
				Card card = new Card(context);
				card.setText(text);
				cards.add(card);
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//		gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		
		Intent intent = getIntent();
		storyModel = (StoryModel) intent.getSerializableExtra(RootActivity.EXTRA_STORY);
		List<TextLineModel> textLineModels = Arrays.asList(storyModel.getLines());
		
		cardScrollView = new CardScrollView(this);
		cardScrollAdapter = new GourangaCardScrollAdapter(this, textLineModels);
		cardScrollView.setAdapter(cardScrollAdapter);
		cardScrollView.activate();
		cardScrollView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				readCurrentCardAloud();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				stopCurrentSpeech();
			}
		});
		
		cardScrollView.setSelection(intent.getIntExtra(RootActivity.EXTRA_LINE, 0));
		
		setContentView(cardScrollView);
		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// nothing to do
//				}
//				handler.post(new Runnable() {
//					@Override
//					public void run() {
//						speak();
//					}
//				});
//			}
//		}).start();
	}

	private void activateTTS() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.JAPANESE);
            	readCurrentCardAloud();
            }
        });
        
        // tts.setOnUtteranceProgressListener(ttsListener); // does not work sadly
        
        // Workaround for listener
        ttsw = new TTSWatcher(tts, ttswListener);
	}
	
	private void deactivateTTS() {
		ttsw.shutdown();
	}

	private void goToNextPage() {
		int position = cardScrollView.getSelectedItemPosition();
		if (position < cardScrollAdapter.getCount() - 1) {
			cardScrollView.setSelection(position + 1);
		} else {
			cardScrollView.setSelection(0);
			finish(); // reached to the end
		}
	}
	
	private void readCurrentCardAloud() {
		if (ttsw != null) {
			int position = cardScrollView.getSelectedItemPosition();
			// String text = "Card " + (position + 1) + "is selected";
			String text = ((TextLineModel) cardScrollAdapter.getItem(position)).getSpeakingText();
			ttsw.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	private void stopCurrentSpeech() {
		ttsw.stop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		activateTTS();
		activateSensors();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		deactivateSensors();
		deactivateTTS();
		saveState();
	}

	@Override
	protected void onDestroy() {
		cardScrollView.deactivate();
		super.onDestroy();
	}
	
	private void activateSensors() {
//		sensorManager.registerListener(sensorEventListener, gravitySensor , SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void deactivateSensors() {
//		sensorManager.unregisterListener(sensorEventListener);
	}

	private void saveState() {
		SharedPreferences pref = getSharedPreferences(SavedStateConstants.NAME, MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		
		int currentLineIndex = cardScrollView.getSelectedItemPosition();
		prefEditor.putInt(SavedStateConstants.CURRENT_STORY_INDEX, storyModel.getIndex());
		prefEditor.putInt(SavedStateConstants.CURRENT_LINE_INDEX, currentLineIndex);
		
		prefEditor.commit();
	}
	
//	private void restoreState() {
//		SharedPreferences pref = getPreferences(MODE_PRIVATE);
//		
//		String savedStoryTitle = pref.getString(SAVED_STATE_CURRENT_STORY_TITLE, "");
//		Log.i("Gouranga", "savedStoryTitle: " + savedStoryTitle);
//		Log.i("Gouranga", "currentStoryTitle: " + storyModel.getTitle());
//		if (savedStoryTitle.equals(storyModel.getTitle())) {
//			int currentLineIndex = pref.getInt(SAVED_STATE_CURRENT_LINE_INDEX, 0); 
//			cardScrollView.setSelection(currentLineIndex);
//		}
//	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//			openOptionsMenu();
//			return true;
//		}
//		
//		return super.onKeyDown(keyCode, event);
//	}
//	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//	
//	@Override
//	public void onOptionsMenuClosed(Menu menu) {
//		finish();
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.next:
//			Log.i("Gouranga", "Next");
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
}
