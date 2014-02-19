package com.tomoto.glass.njslyr;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;

// Workaround for TTS callbacks (seemingly) not implemented
public class TTSWatcher {
	private Handler handler;
	private TextToSpeech tts;
	private Listener listener;
	
	private Timer timer;
	private TTSWatcherTimerTask timerTask;
	private boolean speaking;
	
	public static interface Listener {
		void onStart();
		void onStop(boolean force);
	}
	
	private class TTSWatcherTimerTask extends TimerTask {
		// 0: idle
		// 1: waiting for start
		// 2: waiting for finish
		private int state = 0;
		private long startedTime;
		private long timeout;

		public void disable() {
			state = 0;
		}
		
		public void start(long timeout) {
			state = 1;
			this.timeout = timeout;
			this.startedTime = System.currentTimeMillis();
		}
		
		@Override
		public void run() {
			switch (state) {
			case 0:
				break;
			case 1:
				Log.d("Gouranga", "Waiting for speech to start");
				if (tts.isSpeaking()) {
					state = 2;
				} else if (timeout > 0 && System.currentTimeMillis() - startedTime > timeout) {
					endSpeech(); 
				}
				break;
			case 2:
				Log.d("Gouranga", "Waiting for speech to finish");
				if (!tts.isSpeaking()) {
					endSpeech();
				}
				break;
			}
		}

		private void endSpeech() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					stopWatching(false);
				}
			});
			state = 0;
		}
	};
	

	public TTSWatcher(TextToSpeech tts, Listener listener) {
		this.handler = new Handler();
		this.tts = tts;
		this.listener = listener;
		this.timer = new Timer("TTSWatcher", true);
		this.timerTask = new TTSWatcherTimerTask();
		this.timer.schedule(timerTask, 1000, 1000);
		this.speaking = false;
	}
	
	public void shutdown() {
		timer.cancel();
		tts.shutdown();
	}
	
	public void speak(String text, int queueMode, HashMap<String, String> params) {
		if (Pattern.compile("[aiueoAIUEO0-9]").matcher(text).find()) {
			// readable
			tts.speak(text, queueMode, params);
			startWatching(0);
		} else {
			startWatching(1000);
		}
	}
	
	public void stop() {
		tts.stop();
		stopWatching(true);
	}
	
	private synchronized void startWatching(long timeout) {
		if (speaking) {
			stopWatching(true);
		}
		
		timerTask.start(timeout);
//		timer.schedule(timerTask, 1000, 1000);
		speaking = true;
		
		listener.onStart();
	}

	private synchronized void stopWatching(boolean force) {
		if (speaking) {
			timerTask.disable();
			speaking = false;
			listener.onStop(force);
		}
	}
	
}
