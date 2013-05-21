/**
 *  DirectSIDING: Log-in directo al SIDING desde tu dispositivo Android.
 *  La idea original de DirectSIDING fue de Pedro Pablo Aste Kompen.
 *  
    Copyright (C) 2013  Lukas Zorich

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.directsiding.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.directsiding.Interfaces.ISharedPrefs;

public class LoginOpActivity extends SherlockActivity implements ISharedPrefs {
	
	public final static String POST_URL = "https://intrawww.ing.puc.cl/siding/index.phtml";
	public final static String INGCURSOS_URL = "https://intrawww.ing.puc.cl/siding/dirdes/ingcursos/cursos/index.phtml";
	private final static String USER_ID = "login";
	private final static String PASSWD_ID = "passwd";
	public final static String EXTRA_URL = "EXTRA_URL";
	
	public static Cookie cookie;
	
	protected ProgressDialog progressDialog;
	protected boolean recordarPref;
	protected String userPref, passwdPref;
	protected boolean redirigirIngCursos;
	protected boolean entrarAuto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		recordarPref = prefs.getBoolean(RECORDAR_PREF, false);
		redirigirIngCursos = prefs.getBoolean(INGCURSOS_PREF, false);
		userPref = prefs.getString(USUARIO_PREF, "user");
		passwdPref = prefs.getString(PASSWD_PREF, "password");
		entrarAuto = prefs.getBoolean(ENTRARAUTO_PREF, false);
		
	}
	
	protected void initProgressDialog(Context context) {
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Ingresando al SIDING ...");
		progressDialog.setCancelable(true);
	}
	
	protected void login(String usuario, String password) {
		new LoginOperation().execute(usuario, password);
	}
	
	/**
	 * Clase que se encarga de hacer la operacion login. Los parametros de execute son usuario y password.
	 * @author Lukas Zorich
	 *
	 */
	protected class LoginOperation extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// llamamos a la pagina
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(POST_URL);
			
			try {
				
				List<NameValuePair> nameValuesPairs = new ArrayList<NameValuePair>(2);
				nameValuesPairs.add(new BasicNameValuePair(USER_ID, params[0]));
				nameValuesPairs.add(new BasicNameValuePair(PASSWD_ID, params[1]));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuesPairs));
				
				httpClient.execute(httpPost);
				
				List<Cookie> cookies = httpClient.getCookieStore().getCookies();
				for (int i = 0; i < cookies.size(); i ++) 
					cookie = cookies.get(i);
				
				return true;
			} catch (Exception e) {
				return false;
			} 
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result) {
				String url = redirigirIngCursos ? INGCURSOS_URL : POST_URL;
				Intent i = new Intent(getApplicationContext(), WebActivity.class);
				i.putExtra(EXTRA_URL, url);
				startActivity(i);
			} 
			else 
				Toast.makeText(getApplicationContext(), R.string.ErrorConexion, Toast.LENGTH_SHORT).show();
		}
		
	}

}
