package com.directsiding.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.directsiding.Interfaces.ISharedPrefs;

public class LoginActivity extends LoginOpActivity implements ISharedPrefs, OnClickListener {
	
	public final static String PATH_SIGNIKA_FONT = "fonts/Signika-Semibold.ttf";
	public final static String EXTRA_LOGINFAILED = "LoginFailedExtra";
	
	private EditText actvUser;
	private EditText etPasswd;

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
		
		CheckBox checkBoxRecordar = (CheckBox)findViewById(R.id.checkBox_Recordar);
		actvUser = (EditText)findViewById(R.id.EditText_user);
		etPasswd = (EditText)findViewById(R.id.editText_Passwd);
		
		Bundle extras = getIntent().getExtras();
		
		// si el login fallo, borrar contraseña y el check de recordar
		if (extras != null) {
			checkBoxRecordar.setChecked(false);
			etPasswd.setText("");
		} else {
			checkBoxRecordar.setChecked(recordarPref);
		}
		
		if(recordarPref) {			
			actvUser.setText(userPref);
			if (extras == null) {
				etPasswd.setText(passwdPref);
			}
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
				String usuario = actvUser.getText().toString();
				String passwd = etPasswd.getText().toString();
				
				//activateAnim(usuario, passwd);
				boolean enter = true;
				Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
				if (usuario == null || usuario.equals("")) {
					actvUser.startAnimation(shake);
					enter = false;
				}
				if (passwd == null || passwd.equals("")) {
					etPasswd.startAnimation(shake);
					enter = false;
				}
				
				if (enter)
					new LoginOperation().execute(usuario, passwd);
				
				break;
		}
		
	}
	
	private boolean activateAnim(String usuario, String password) {
		if (usuario.equals("newton")) {
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
			findViewById(R.id.textView_LoginTitle).startAnimation(animation);
			findViewById(R.id.imageButton_Settings).startAnimation(animation);
			findViewById(R.id.button_entrar).startAnimation(animation);
			findViewById(R.id.checkBox_Recordar).startAnimation(animation);
			actvUser.startAnimation(animation);
			etPasswd.startAnimation(animation);
			return true;
		} else if (usuario.equalsIgnoreCase("harlem") && password.equalsIgnoreCase("shake")) {
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.harlemshake_initshake);
			findViewById(R.id.textView_LoginTitle).startAnimation(animation);
		}
		return false;
	}
	
	private void login() {
		// Recuperamos el usuario y la contrase�a. Si el checkbox esta checkeado, guardamos los datos.
		EditText actvUser = (EditText)findViewById(R.id.EditText_user);
		EditText etPasswd = (EditText)findViewById(R.id.editText_Passwd);
		
		String usuario = actvUser.getText().toString();
		String passwd = etPasswd.getText().toString();
	}
	
	@Override
	protected void onPause() {
		// Recuperamos el usuario y la contrase�a. Si el checkbox esta checkeado, guardamos los datos.
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
}
