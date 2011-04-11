/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.countdown;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.Toast;

public class CountdownDemo extends Activity {

	CountdownChronometer countdown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chronometer);

		Button button;

		countdown = (CountdownChronometer) findViewById(R.id.chronometer);

		countdown.setBase(System.currentTimeMillis() + 30000);

		button = (Button) findViewById(R.id.start);
		button.setOnClickListener(mStartListener);

		button = (Button) findViewById(R.id.stop);
		button.setOnClickListener(mStopListener);

		button = (Button) findViewById(R.id.reset);
		button.setOnClickListener(mResetListener);

		button = (Button) findViewById(R.id.set_format);
		button.setOnClickListener(mSetFormatListener);

		button = (Button) findViewById(R.id.clear_format);
		button.setOnClickListener(mClearFormatListener);

		button = (Button) findViewById(R.id.set_listener);
		button.setOnClickListener(mSetOnCompleteListener);

	}

	@Override
	protected void onResume() {
		countdown.start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		countdown.stop();
		super.onPause();
	}

	View.OnClickListener mStartListener = new OnClickListener() {
		public void onClick(View v) {
			countdown.start();
		}
	};

	View.OnClickListener mStopListener = new OnClickListener() {
		public void onClick(View v) {
			countdown.stop();
		}
	};

	View.OnClickListener mResetListener = new OnClickListener() {
		public void onClick(View v) {
			Calendar c = Calendar.getInstance();
			c.set(2011, Calendar.AUGUST, 26, 9, 0, 0);
			countdown.setBase(c.getTimeInMillis());
		}
	};

	View.OnClickListener mSetFormatListener = new OnClickListener() {
		public void onClick(View v) {
			countdown
					.setCustomChronoFormat("%1$02d days, %2$02d hours, %3$02d minutes "
							+ "and %4$02d seconds remaining");
			countdown.setFormat("Formatted time (%s)");
		}
	};

	View.OnClickListener mClearFormatListener = new OnClickListener() {
		public void onClick(View v) {
			countdown.setCustomChronoFormat(null);
			countdown.setFormat(null);
		}
	};

	View.OnClickListener mSetOnCompleteListener = new OnClickListener() {
		public void onClick(View v) {
			countdown.setOnCompleteListener(new OnChronometerTickListener() {
				@Override
				public void onChronometerTick(Chronometer chronometer) {
					Toast.makeText(CountdownDemo.this, "We have lift off!",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	};

}
