/**
 *  DirectSIDING: Log-in directo al SIDING desde tu dispositivo Android.
 *  La idea original de DirectSIDING fue de Pedro Pablo Aste Kompen.
 *  
 *  Copyright (C) 2013  Lukas Zorich
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.directsiding.android;

import com.actionbarsherlock.app.ActionBar;

import android.os.Bundle;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;

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
		
		//initProgressDialog(this);
		if (entrarAuto) {
			new LoginOperation(this) {
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
