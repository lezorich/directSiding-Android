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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.http.cookie.Cookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

@SuppressLint("setJavaScriptEnabled")
public class WebActivity extends SherlockActivity {
	
	private WebView webView;
	private ProgressBar mProgressBar;
	
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	
	private static int actualNotifyId = 0;
	private static final int LOGOUT_MILI_TIME = 3600000;
	
	private static final int[] downloadAnimationIcons = { R.drawable.stat_sys_download_anim0, 
		R.drawable.stat_sys_download_anim1, R.drawable.stat_sys_download_anim2, R.drawable.stat_sys_download_anim3,
		R.drawable.stat_sys_download_anim4, R.drawable.stat_sys_download_anim5 };

	private long lastTimeStamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_web);
		
		// Le ponemos la font Signika al titulo del Action Bar
		SpannableString s = new SpannableString(getString(R.string.app_name));
		s.setSpan(new TypefaceSpan(this, LoginActivity.PATH_SIGNIKA_FONT), 0, s.length(),
		        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		// Agregamos lo necesario al Action Bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(s);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        
        // Obtenemos la url a la que el usuario va a ingresar
		String url = getIntent().getExtras().getString(LoginActivity.EXTRA_URL);
		
		// Obtenemos la cookie y la agregamos al webview
		Cookie sessionCookie = LoginOpActivity.cookie;
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		if (sessionCookie != null) {
			//cookieManager.removeSessionCookie();
			String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
			cookieManager.setCookie(LoginActivity.POST_URL, cookieString);
			CookieSyncManager.getInstance().sync();
		}
		
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar_webView);
		
		// Configuración del web view
		webView = (WebView)findViewById(R.id.webView_ing);
		//webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setJavaScriptEnabled(true);
		//webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				//getSherlock().setProgress(progress*100);
				mProgressBar.setProgress(progress);
			}
		});
		
		webView.setWebViewClient(new DirectSidingWebViewClient(this));
		
		webView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype, long contentLength) {
				Toast.makeText(WebActivity.this, "Iniciando descarga ...", Toast.LENGTH_SHORT).show();
				new DownloadFile(WebActivity.actualNotifyId++).execute(url);	
			}
		});
		webView.loadUrl(url);
		
		// guardamos el tiempo en el que se creo la actividad
		lastTimeStamp = SystemClock.elapsedRealtime();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Si la actividad estubo inactiva mas de dos horas, el SIDING deslogea automaticamente al usuario,
		// entonces para que no pase eso, iniciamos la InitActivity para que se autologee
		if (SystemClock.elapsedRealtime() - lastTimeStamp > LOGOUT_MILI_TIME) {
			startActivity(new Intent(this, InitActivity.class));
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// guardamos el tiempo en el que salio de la actividad
		lastTimeStamp = SystemClock.elapsedRealtime();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: 
				if (getParent() != null) {
					NavUtils.navigateUpFromSameTask(this);
				} else {
	            	Intent i = new Intent(getApplicationContext(), LoginActivity.class);
	            	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            	startActivity(i);
	            	
	            	finish();
				}
                return true;
            case R.id.action_settings:
            	Intent i = new Intent(getApplicationContext(), ConfigActivity.class);
            	i.putExtra(DirectSIDING.EXTRA_WHICH_ACTIVITY, getString(R.string.WebActivity));
            	startActivity(i);
            	return true;
            default:
            	return super.onOptionsItemSelected(item);
        }
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.web, menu);
    	
        return true;
    }
    
    @Override
    public void onBackPressed() {
    	if (webView.canGoBack())
    		webView.goBack();
    	else
    		super.onBackPressed();
    }
    
    private class DirectSidingWebViewClient extends WebViewClient {
    	
    	
    	public DirectSidingWebViewClient(Activity activity) {
    	}
    	
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		super.onPageStarted(view, url, favicon);
    		
    		getSherlock().setProgressBarIndeterminateVisibility(true);
    		View v = findViewById(R.id.progressBar_webView);
    		v.setVisibility(View.VISIBLE);
    		
    	}
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		super.onPageFinished(view, url);
    		
    		getSherlock().setProgressBarIndeterminateVisibility(false);
    		Animation animation = AnimationUtils.loadAnimation((Activity)view.getContext(), R.anim.progressbar_alpha_anim);
    		animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					View v = findViewById(R.id.progressBar_webView);
					v.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) { }

				@Override
				public void onAnimationStart(Animation animation) { }
			});
    		animation.reset();
    		View v = findViewById(R.id.progressBar_webView);

    		if (v != null) {
    			v.clearAnimation();
    			v.startAnimation(animation);
    		}
    	}
    	
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.endsWith("logout.phtml")) { // si pone salir en el SIDING, volvemos a la LoginActivity
				if (getParent() != null) {
					NavUtils.navigateUpFromSameTask((Activity)view.getContext());
				} else {
	            	Intent i = new Intent(getApplicationContext(), LoginActivity.class);
	            	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            	startActivity(i);
	            	
	            	finish();
				}
				return true;
			}
			else if (url.equals("http://www.ing.puc.cl/")) { // si entra a esa pagina, es porque fallo el login (datos incorrectos)
				Toast.makeText(getApplicationContext(), R.string.LoginFailed, Toast.LENGTH_SHORT).show();
            	Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	i.putExtra(LoginActivity.EXTRA_LOGINFAILED, true);
            	startActivity(i);
            	
            	finish();
				return true;
			}
			else if (!url.startsWith("https://intrawww.ing.puc.cl/siding")) { // si abre un link fuera del dominio del SIDING, lo abro con el browser
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Clase que se encarga de descargar el archivo. El proceso devuelve un String[] de tres elementos. 
	 * El primer elemento es el tipo de archivo, el segunda es el path en donde se descargo el archivo y el tercero la cantidad descargada.
	 * @author Lukas Zorich
	 *
	 */
	private class DownloadFile extends AsyncTask<String, String, String[]> {
		
		private int _notifyId;
		private int icono;
		private long lastMilli;
		private static final int DOWNLOAD_ANIMATION_UPDATE = 500;
		private static final String ERROR_FILENOTFOUNDEXCEPTION = "FileNotFound";
		private static final String ERROR_IOEXCEPTION = "IOException";
		private static final String ERROR = "Error";
		
		public DownloadFile(int notifyId) {
			this._notifyId = notifyId;
			icono = 0;
		}
		
		@Override
		protected void onPreExecute() {
			mNotifyManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			mBuilder = new NotificationCompat.Builder(WebActivity.this);
			mBuilder.setContentTitle("Descargando archivo ...")
					.setContentText("")
					.setSmallIcon(downloadAnimationIcons[0])
					.setProgress(0,0, true);
			lastMilli = SystemClock.elapsedRealtime();
		}

		@SuppressLint("NewApi")
		@Override
		protected String[] doInBackground(String... params) {
			try{
		        URL url = new URL(params[0]);
	
		        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	
		        urlConnection.setRequestMethod("GET");
		        urlConnection.setDoOutput(true);
		        urlConnection.addRequestProperty("Cookie", LoginOpActivity.cookie.getName() + "=" + LoginOpActivity.cookie.getValue());
		        urlConnection.connect();
		        
		        String aux = urlConnection.getHeaderField("Content-Disposition").split("; ")[1];
		        String filename = aux.substring("filename=\"".length(), aux.length() - 1);
		        
		        File file = null;
		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
		        	file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
		        } else {
		        	file = new File(Environment.getExternalStorageDirectory() + "Download/", filename);
		        }
		        //file.mkdirs(); //nos aseguramos que el directorio existe
		        
		        // hacemos la notificacion
		        mBuilder.setContentText(filename + " - 0.0 Mb");
		        Intent notificationIntent = new Intent();
		        //notificationIntent.setDataAndType(Uri.fromFile(file), urlConnection.getContentType());
		        PendingIntent pendingIntent = PendingIntent.getActivity(WebActivity.this, 0, notificationIntent, 0);
		        mBuilder.setContentIntent(pendingIntent);
		        mNotifyManager.notify(_notifyId, mBuilder.build());
		        
		        FileOutputStream fileOutput = new FileOutputStream(file);
		        InputStream inputStream = urlConnection.getInputStream();
	
		        int downloadedSize = 0;
		        byte[] buffer = new byte[1024];
		        int bufferLength = 0; 

		        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
		                fileOutput.write(buffer, 0, bufferLength);
		                downloadedSize += bufferLength;
		                
		                // actualizamos el tamaño descargado en la notificacion y actualizamos el icono con la animación
		                if (SystemClock.elapsedRealtime() - lastMilli >= DOWNLOAD_ANIMATION_UPDATE) {
		                	mBuilder.setContentText(filename + " - " + String.format("%.1f", downloadedSize/(1000.0*1000)) + " Mb")
		                			.setSmallIcon(downloadAnimationIcons[(++icono) % downloadAnimationIcons.length]);
		                	mNotifyManager.notify(_notifyId, mBuilder.build());
		                	lastMilli = SystemClock.elapsedRealtime();
		                }
		        }

		        fileOutput.close();
		        return new String[] {urlConnection.getContentType(), file.getAbsolutePath(), String.format("%.1f", downloadedSize/(1000.0*1000))};
			} catch (FileNotFoundException e) {
				return new String[] { ERROR_FILENOTFOUNDEXCEPTION };
			} catch (MalformedURLException e) {
				return new String[] { ERROR_IOEXCEPTION };
			} catch (IOException e) {
				return new String[] { ERROR_IOEXCEPTION };
			} catch (Exception e) {
				return new String[] { ERROR };
			}
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			if (result.length > 1) {
				File file = new File(result[1]); 
	            if(file.exists()) 
	            {
	            	mBuilder.setContentTitle(file.getName())
	            			.setContentText("Descarga finalizada - " + result[2] + " Mb")
	            			.setSmallIcon(downloadAnimationIcons[0])
	            			.setAutoCancel(true)
	            			.setProgress(0, 0, false);
			        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
			        notificationIntent.setDataAndType(Uri.fromFile(file), result[0]);
			        PendingIntent pendingIntent = PendingIntent.getActivity(WebActivity.this, 0, notificationIntent, 0);
			        mBuilder.setContentIntent(pendingIntent);
			        mNotifyManager.notify(_notifyId, mBuilder.build());
	            	Toast.makeText(WebActivity.this, "Descarga finalizada", Toast.LENGTH_SHORT).show();
	            }
			} else {
				if (result[0] == ERROR_FILENOTFOUNDEXCEPTION) {
					Toast.makeText(getApplicationContext(), R.string.FileNotFound, Toast.LENGTH_SHORT).show();
					mNotifyManager.cancel(_notifyId);
				} else if (result[0] == ERROR_IOEXCEPTION) {
	            	mBuilder.setContentTitle("ERROR")
	    					.setContentText("No se pudo descargar el archivo")
	    					.setSmallIcon(downloadAnimationIcons[0])
	    					.setContentIntent(PendingIntent.getActivity(WebActivity.this, 0, new Intent(), 0))
	    					.setAutoCancel(true)
	    					.setProgress(0, 0, false);
	            	mNotifyManager.notify(_notifyId, mBuilder.build());
					Toast.makeText(getApplicationContext(), R.string.ErrorConexion, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Hubo un error", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
	}
 
}
