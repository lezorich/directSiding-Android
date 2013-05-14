package com.directsiding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.directsiding.Interfaces.ISharedPrefs;

public class LoginActivity extends LoginOpActivity implements ISharedPrefs, OnClickListener {
	
	/*public final static String POST_URL = "https://intrawww.ing.puc.cl/siding/index.phtml";
	public final static String INGCURSOS_URL = "https://intrawww.ing.puc.cl/siding/dirdes/ingcursos/cursos/index.phtml";
	private final static String USER_ID = "login";
	private final static String PASSWD_ID = "passwd";
	
	public final static String EXTRA_URL = "EXTRA_URL";*/
	
	public final static String PATH_SIGNIKA_FONT = "fonts/Signika-Semibold.ttf";
	
	/*public static Cookie cookie;
	public static boolean redirigirIngCursos;
	
	private ProgressDialog progressDialog;*/
	
	//private CheckBox recordar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().hide();
		setContentView(R.layout.activity_login_activiy);
		
		// Le cambiamos la letra al titulo
		TextView textViewTitulo = (TextView)findViewById(R.id.textView_LoginTitle);
		Typeface typeFace = Typeface.createFromAsset(getAssets(), PATH_SIGNIKA_FONT);
		textViewTitulo.setTypeface(typeFace);
		
		initProgressDialog(this);
		/*SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		boolean recordar = prefs.getBoolean(RECORDAR_PREF, false);
		redirigirIngCursos = prefs.getBoolean(INGCURSOS_PREF, false);
		((CheckBox)findViewById(R.id.checkBox_Recordar)).setChecked(recordar);
		
		
		if (recordar) {
			String usuario = prefs.getString(USUARIO_PREF, "user");
			String passwd = prefs.getString(PASSWD_PREF, "password");
			
			EditText actvUser = (EditText)findViewById(R.id.EditText_user);
			actvUser.setText(usuario);
			
			EditText etPasswd = (EditText)findViewById(R.id.editText_Passwd);
			etPasswd.setText(passwd);
		}
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Entrando ...");
		progressDialog.setCancelable(true);*/
		
		((CheckBox)findViewById(R.id.checkBox_Recordar)).setChecked(recordarPref);
		
		if(recordarPref) {
			EditText actvUser = (EditText)findViewById(R.id.EditText_user);
			EditText etPasswd = (EditText)findViewById(R.id.editText_Passwd);
			
			actvUser.setText(userPref);
			etPasswd.setText(passwdPref);
		}
		
		((ImageButton)findViewById(R.id.imageButton_Settings)).setOnClickListener(this);
		((Button)findViewById(R.id.button_entrar)).setOnClickListener(this);	
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.imageButton_Settings:
            	Intent i = new Intent(getApplicationContext(), ConfigActivity.class);
            	i.putExtra(DirectSIDING.EXTRA_WHICH_ACTIVITY, getString(R.string.LoginActivity));
            	startActivity(i);
				break;
			case R.id.button_entrar:
				EditText actvUser = (EditText)findViewById(R.id.EditText_user);
				EditText etPasswd = (EditText)findViewById(R.id.editText_Passwd);
				
				String usuario = actvUser.getText().toString();
				String passwd = etPasswd.getText().toString();
				
				new LoginOperation().execute(usuario, passwd);
				break;
		}
		
	}
	
	private void login() {
		// Recuperamos el usuario y la contraseña. Si el checkbox esta checkeado, guardamos los datos.
		EditText actvUser = (EditText)findViewById(R.id.EditText_user);
		EditText etPasswd = (EditText)findViewById(R.id.editText_Passwd);
		
		String usuario = actvUser.getText().toString();
		String passwd = etPasswd.getText().toString();
		
		// hacemos el login
		//new LoginOperation().execute(usuario, passwd);
	}
	
	@Override
	protected void onPause() {
		// Recuperamos el usuario y la contraseña. Si el checkbox esta checkeado, guardamos los datos.
		EditText actvUser = (EditText)findViewById(R.id.EditText_user);
		EditText etPasswd = (EditText)findViewById(R.id.editText_Passwd);
		
		String usuario = actvUser.getText().toString();
		String passwd = etPasswd.getText().toString();
		
		boolean recordar_actual = ((CheckBox)findViewById(R.id.checkBox_Recordar)).isChecked();
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(RECORDAR_PREF, recordar_actual);
		if (recordar_actual) {
			editor.putString(USUARIO_PREF, usuario);
			editor.putString(PASSWD_PREF, passwd);
		}
		editor.commit();
		
		super.onPause();
	}
	
	/**
	 * Clase que se encarga de hacer la operación login. Los parámetros de execute son usuario y password.
	 * @author Lukas Zorich
	 *
	 */
	/*private class LoginOperation extends AsyncTask<String, Void, Boolean> {
		
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
		
	}*/
}
