/*
 * Copyright (C) 2011 DroidGiro authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.droidgiro;

import se.droidgiro.scanner.CaptureActivity;
import se.droidgiro.scanner.CloudClient;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PairingActivity extends Activity {

	private static final String TAG = "DroidGiro.PairingActivity";

	private EditText digit1;
	private EditText digit2;
	private EditText digit3;
	private EditText digit4;

	private final Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pairing);
		DigitWatcher dw = new DigitWatcher();

		digit1 = (EditText) findViewById(R.id.pairDigit1);
		digit2 = (EditText) findViewById(R.id.pairDigit2);
		digit3 = (EditText) findViewById(R.id.pairDigit3);
		digit4 = (EditText) findViewById(R.id.pairDigit4);
		digit1.requestFocus();

		digit1.addTextChangedListener(dw);
		digit2.addTextChangedListener(dw);
		digit3.addTextChangedListener(dw);
		digit4.addTextChangedListener(dw);
		
		TextView serverLinkTextView = (TextView) findViewById(R.id.pairHelp);
		SpannableString str = SpannableString.valueOf(serverLinkTextView
				.getText());
		Linkify.addLinks(str, Linkify.ALL);
		serverLinkTextView.setText(str);
		serverLinkTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://droidgiro.se"));
				startActivity(myIntent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		digit1.setText("");
		digit2.setText("");
		digit3.setText("");
		digit4.setText("");
		digit1.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about: {
			Intent intent = new Intent(this, About.class);
			startActivity(intent);
			break;
		}
		case R.id.settings: {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setClassName(this, PreferencesActivity.class.getName());
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private class DigitWatcher implements TextWatcher {

		public void afterTextChanged(Editable s) {
			if (digit1.getText().length() == 0)
				digit1.requestFocus();
			else if (digit2.getText().length() == 0)
				digit2.requestFocus();
			else if (digit3.getText().length() == 0)
				digit3.requestFocus();
			else if (digit4.getText().length() == 0)
				digit4.requestFocus();
			else {
				// compose pin
				final String pin = digit1.getText().toString()
						+ digit2.getText().toString()
						+ digit3.getText().toString()
						+ digit4.getText().toString();

				final ProgressDialog pd = ProgressDialog.show(
						PairingActivity.this, null,
						getString(R.string.pairing_msg));

				new Thread() {
					public void run() {
						final Registration registration = register(pin);
						handler.post(new Runnable() {
							public void run() {
								pd.dismiss();
								if (registration.isSuccessful()) {
									// Open scanner
									Intent intent = new Intent(
											PairingActivity.this,
											CaptureActivity.class);
									intent.putExtra("channel", registration
											.getChannel());
									startActivity(intent);
								} else {
									Toast.makeText(PairingActivity.this,
											R.string.pairing_failed,
											Toast.LENGTH_SHORT).show();
									clear();
								}
							}
						});
					}

				}.start();
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}

	public void clear() {
		digit4.setText("");
		digit3.setText("");
		digit2.setText("");
		digit1.setText("");
		digit1.requestFocus();
	}

	public Registration register(String pin) {
		Registration registration;
		try {
			registration = CloudClient.register(pin);
		} catch (Exception e) {
			return new Registration();
		}
		if (!registration.isSuccessful()) {
			return registration;
		} else {
			return registration;
		}

	}

	/**
	 * Temporary by-pass method for accessing the CaptureActivity without a pin
	 * code
	 * 
	 * @param v
	 */
	public void bypass(View v) {
		Intent intent = new Intent(PairingActivity.this, CaptureActivity.class);
		intent.putExtra("identifier", "debug");
		intent.putExtra("channel", "debug");
		startActivity(intent);
	}

}
