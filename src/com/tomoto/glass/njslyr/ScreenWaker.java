package com.tomoto.glass.njslyr;

import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class ScreenWaker {
	private Window window;
	private Handler handler;
	private int counter;
	private Object lock = new Object();
	
	private Runnable expire = new Runnable() {
		@Override
		public void run() {
			synchronized (lock) {
				if (--counter == 0) {
					window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				Log.d("Gouranga", "Screen Lock Dec: " + counter);
			}
		}
	};

	public ScreenWaker(Window window) {
		this.window = window;
		this.handler = new Handler();
		this.counter = 0;
	}
	
	public void extend(long extendMillis) {
		synchronized (lock) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			counter++;
			Log.d("Gouranga", "Screen Lock Inc: " + counter);
			
			handler.postDelayed(expire, extendMillis);
		}
	}
}
