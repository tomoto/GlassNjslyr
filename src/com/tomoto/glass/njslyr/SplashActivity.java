package com.tomoto.glass.njslyr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f, metrics.widthPixels / 2, metrics.heightPixels / 2);
		scaleAnimation.setDuration(1000);
		findViewById(R.id.splash_image).startAnimation(scaleAnimation);
		
		scaleAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// Nothing to do
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// Nothing to do
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				startActivity(new Intent(SplashActivity.this, RootActivity.class));
				finish();
			}
		});
	}

}
