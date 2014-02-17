package com.tomoto.glass.njslyr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class MainActivity extends Activity {
	
	private SensorManager sensorManager;
	private Sensor gravitySensor;

	private CardScrollView cardScrollView;
	private GourangaCardScrollAdapter cardScrollAdapter;
	
	private TextToSpeech tts;
	
	public static final String SAVED_STATE_CURRENT_LINE_INDEX = "currentLineIndex";
	
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		private static final float THRESHOLD = 0.5f;
		long lastSensedTime = 0;
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.values[2] < -THRESHOLD) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastSensedTime > 1000) {
					int position = cardScrollView.getSelectedItemPosition();
					if (position < cardScrollAdapter.getCount() - 1) {
						cardScrollView.setSelection(position + 1);
					}
				}
				lastSensedTime = currentTime;
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Nothing to do
		}
	};
	
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
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		
		final String[] texts = getResources().getStringArray(R.array.story);
		List<TextLineModel> textLineModels = new ArrayList<TextLineModel>();
		int index = 0;
		for (String text : texts) {
			textLineModels.add(new TextLineModel(index++, text));
		}
		
		activateTTS();
		
		cardScrollView = new CardScrollView(this);
		cardScrollAdapter = new GourangaCardScrollAdapter(this, textLineModels);
		cardScrollView.setAdapter(cardScrollAdapter);
		cardScrollView.activate();
		cardScrollView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// String text = "Card " + (position + 1) + "is selected";
				String text = ((TextLineModel) cardScrollAdapter.getItem(position)).getText();
				tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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
	}

	private void activateTTS() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });
	}
	
	private void deactivateTTS() {
		tts.shutdown();
	}

	@Override
	protected void onDestroy() {
		deactivateSensors();
		deactivateTTS();
		saveState();
		cardScrollView.deactivate();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		activateSensors();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		deactivateSensors();
	}
	
	private void activateSensors() {
		sensorManager.registerListener(sensorEventListener, gravitySensor , SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void deactivateSensors() {
		sensorManager.unregisterListener(sensorEventListener);
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
