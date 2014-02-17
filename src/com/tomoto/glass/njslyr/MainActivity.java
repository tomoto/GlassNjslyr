package com.tomoto.glass.njslyr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class MainActivity extends Activity {
	
//	private SensorManager sensorManager;
//	private Sensor gravitySensor;

	private Handler handler = new Handler();
	private CardScrollView cardScrollView;
	private GourangaCardScrollAdapter cardScrollAdapter;
	
	private TextToSpeech tts;
	// private UtteranceProgressListener ttsListener; // Doesn't work...
	
	private class TtsWatcherTask extends TimerTask {
		// 0: disabled
		// 1: waiting for start
		// 2: waiting for finish
		private int state = 0;
		
		public void start() {
			state = 1;
		}
		
		public void clear() {
			state = 0;
		}
		
		@Override
		public void run() {
			switch (state) {
			case 0:
				break;
			case 1:
				Log.i("Gouranga", "Waiting for speech to start");
				if (tts.isSpeaking()) {
					state = 2;
				}
				break;
			case 2:
				Log.i("Gouranga", "Waiting for speech to finish");
				if (!tts.isSpeaking()) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							proceedWithText();
						}
					});
					state = 0;
				}
				break;
			}
		}
		
	};
	
	// Workaround for TTS listener
	private Timer ttsWatcher;
	private TtsWatcherTask ttsWatcherTask;
	
	public static final String SAVED_STATE_CURRENT_LINE_INDEX = "currentLineIndex";
	
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
	
	private class GourangaCardScrollAdapter extends CardScrollAdapter {
		private List<TextLineModel> textLineModels;
		private List<Card> cards;
		
		public GourangaCardScrollAdapter(Context context, List<TextLineModel> textLineModels) {
			this.textLineModels = textLineModels;
			
			cards = new ArrayList<Card>();
			for (TextLineModel tlm : textLineModels) {
				String text = MessageFormat.format("{0} ({1}/{2})", tlm.getText(), tlm.getIndex()+1, textLineModels.size());
				Card card = new Card(context);
				card.setText(text);
				cards.add(card);
			}
		}
		
//		public List<Card> getCards() {
//			return cards;
//		}
		
		@Override
		public int findIdPosition(Object id) {
			if (id instanceof Number) {
				return ((Number)id).intValue();
			} else {
				return CardScrollView.INVALID_POSITION;
			}
		}

		@Override
		public int findItemPosition(Object item) {
			return textLineModels.indexOf(item);
		}

		@Override
		public int getCount() {
			return textLineModels.size();
		}

		@Override
		public Object getItem(int position) {
			return textLineModels.get(position);
		}

		@Override
		public View getView(int position, View contentView, ViewGroup parent) {
			return cards.get(position).toView();
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//		gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		
		String[] texts = getResources().getStringArray(R.array.story);
		String[] speakingTexts = getResources().getStringArray(R.array.story_speech); 
		List<TextLineModel> textLineModels = new ArrayList<TextLineModel>();
		
		for (int i = 0; i < texts.length; i++) {
			textLineModels.add(new TextLineModel(i, texts[i], speakingTexts[i]));
		}
		
		activateTTS();
		
		cardScrollView = new CardScrollView(this);
		cardScrollAdapter = new GourangaCardScrollAdapter(this, textLineModels);
		cardScrollView.setAdapter(cardScrollAdapter);
		cardScrollView.activate();
		cardScrollView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				speak();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				tts.stop();
			}
		});
		
		restoreState();
		
		activateSensors();
		
		setContentView(cardScrollView);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// nothing to do
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						speak();
					}
				});
			}
		}).start();
	}

	private void activateTTS() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });
        
        // tts.setOnUtteranceProgressListener(ttsListener); // does not work sadly
        
        // Workaround for listener
        ttsWatcher = new Timer("ttsWatcher", true);
        ttsWatcherTask = new TtsWatcherTask();
        ttsWatcher.schedule(ttsWatcherTask, 1000, 1000);
	}
	
	private void deactivateTTS() {
		ttsWatcherTask.cancel();
		ttsWatcher.cancel();
		tts.shutdown();
	}

	private void proceedWithText() {
		int position = cardScrollView.getSelectedItemPosition();
		if (position < cardScrollAdapter.getCount() - 1) {
			cardScrollView.setSelection(position + 1);
			speak();
		}
	}
	
	private void speak() {
		int position = cardScrollView.getSelectedItemPosition();
		// String text = "Card " + (position + 1) + "is selected";
		String text = ((TextLineModel) cardScrollAdapter.getItem(position)).getSpeakingText();
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		
		ttsWatcherTask.start();
	}

	@Override
	protected void onDestroy() {
		deactivateSensors();
		deactivateTTS();
		saveState();
		cardScrollView.deactivate();
		super.onDestroy();
	}
	
	// Not sure if it works
	@Override
	protected void onResume() {
		super.onResume();
		activateTTS();
		activateSensors();
	}
	
	// Not sure if it works
	@Override
	protected void onPause() {
		super.onPause();
		deactivateSensors();
		deactivateTTS();
	}
	
	private void activateSensors() {
//		sensorManager.registerListener(sensorEventListener, gravitySensor , SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void deactivateSensors() {
//		sensorManager.unregisterListener(sensorEventListener);
	}

	private void saveState() {
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		int currentLineIndex = cardScrollView.getSelectedItemPosition();
		prefEditor.putInt(SAVED_STATE_CURRENT_LINE_INDEX, currentLineIndex);
		prefEditor.commit();
	}
	
	private void restoreState() {
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		int currentLineIndex = pref.getInt(SAVED_STATE_CURRENT_LINE_INDEX, 0); 
		cardScrollView.setSelection(currentLineIndex);
	}
	
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
