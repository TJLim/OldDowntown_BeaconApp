package com.example.jinwoo.olddowntown_beaconapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.jinwoo.olddowntown_beaconapp.main.MainActivity;

public class Intro extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);

		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			public void run() {
				Intent intent = new Intent(Intro.this, MainActivity.class);
				startActivity(intent);

				finish();
			}
		}, 2700);

	}
}
