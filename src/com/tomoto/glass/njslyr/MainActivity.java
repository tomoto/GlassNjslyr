package com.tomoto.glass.njslyr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.TimelineManager;

public class MainActivity extends Activity {

//	private TimelineManager tlm;
//	private long cardId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setData(Uri.parse("http://www10.atwiki.jp/njslyr/pages/18.html"));
//		intent.setClassName("com.google.glass.browser", "com.google.glass.browser.WebBrowserActivity");
//		startActivity(intent);
		
//		Log.i("Gouranga", "MainActivity.onCreate " + cardId);
//		tlm = TimelineManager.from(this);
		Card card = new Card(this);
		card.setText(R.string.content_text);
//		cardId = tlm.insert(card);
		
		setContentView(card.toView());
	}
}
