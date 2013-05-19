package com.directsiding;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.directsiding.Interfaces.ISharedPrefs;

public class ConfigActivity extends SherlockActivity implements ISharedPrefs {
	
	private CheckBox boxIngCursos;
	private CheckBox boxAccAuto;
	private String previousActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE|
        		ActionBar.DISPLAY_SHOW_HOME|
        		ActionBar.DISPLAY_HOME_AS_UP);
        //actionBar.setTitle(getString(R.string.));
        
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(RECORDAR_PREF, false)) {
        	((ImageButton)findViewById(R.id.ImageButton_delete)).setVisibility(View.VISIBLE);
        	((TextView)findViewById(R.id.textView_usuario)).setText(prefs.getString(USUARIO_PREF, "UsuarioUc"));
        }
	
		boxIngCursos = (CheckBox)findViewById(R.id.checkBox_IngCursos);
		boxIngCursos.setChecked(prefs.getBoolean(INGCURSOS_PREF, false));
		
		boxAccAuto = (CheckBox)findViewById(R.id.checkBox_AccAuto);
		boxAccAuto.setChecked(prefs.getBoolean(ENTRARAUTO_PREF, false));
		
		TextView textView_version = (TextView)findViewById(R.id.textView_Version);
		String version = "v ";
		
		try {
			version +=  getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version += "0.1";
		}
		
		textView_version.setText(version);
		
		ImageButton delete = (ImageButton)findViewById(R.id.ImageButton_delete);
		delete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetUserData(v);
			}
		});
		
		previousActivity = getIntent().getExtras().getString(DirectSIDING.EXTRA_WHICH_ACTIVITY);
	}
	
	private void resetUserData(View v) {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENTRARAUTO_PREF, false);
		editor.putBoolean(INGCURSOS_PREF, false);
		editor.putBoolean(RECORDAR_PREF, false);
		editor.putString(USUARIO_PREF, "UsuarioUC");
		editor.putString(PASSWD_PREF, "password");
		
		((TextView)findViewById(R.id.textView_usuario)).setVisibility(View.INVISIBLE);
		v.setVisibility(View.VISIBLE);
		boxIngCursos.setChecked(false);	
		boxAccAuto.setChecked(false);
		
		editor.commit();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        /* Si el usuario viene de LoginActivity, volvemos a ella. Si viene de la webview activity, emulamos el
         * boton back.
         */
            case android.R.id.home:
            	if (previousActivity.equals(getString(R.string.LoginActivity))) 
            		NavUtils.navigateUpFromSameTask(this);
            	else {
                    onBackPressed();
            	}
                return true;
            case R.id.action_about:
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            	alertDialogBuilder.setTitle(R.string.AlertDialogTitle)
            				.setMessage(R.string.AlertDialogMessage)
            				.setCancelable(false)
            				.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
            	AlertDialog alertDialog = alertDialogBuilder.create();
            	alertDialog.show();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.config, menu);
    	
        return true;
    }
	
	//grabamos la configuracion
	@Override
	protected void onPause() {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(INGCURSOS_PREF, boxIngCursos.isChecked());
		editor.putBoolean(ENTRARAUTO_PREF, boxAccAuto.isChecked());
		editor.commit();
		
		super.onPause();
	}

}
