package com.directsiding.android;

import com.actionbarsherlock.app.ActionBar;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;

public class InitActivity extends LoginOpActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
		
		SpannableString s = new SpannableString(getString(R.string.app_name));
		s.setSpan(new TypefaceSpan(this, LoginActivity.PATH_SIGNIKA_FONT), 0, s.length(),
		        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(s);
		
		initProgressDialog(this);
		if (entrarAuto) {
			new LoginOperation() {
				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);
					
					if (!result) {
						startActivity(new Intent(getApplicationContext(), LoginActivity.class));
					}
					finish();
				};
			}.execute(userPref, passwdPref);
		}
		else {
			startActivity(new Intent(getApplicationContext(), LoginActivity.class));
			finish();
		}
	}

}
