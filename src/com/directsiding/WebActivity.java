package com.directsiding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.cookie.Cookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

@SuppressLint("setJavaScriptEnabled")
public class WebActivity extends SherlockActivity {
	
	private WebView webView; 
	
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	
	private static int actualNotifyId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_web);
		
		SpannableString s = new SpannableString(getString(R.string.app_name));
		s.setSpan(new TypefaceSpan(this, LoginActivity.PATH_SIGNIKA_FONT), 0, s.length(),
		        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(s);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
		
		String url = getIntent().getExtras().getString(LoginActivity.EXTRA_URL);
		
		Cookie sessionCookie = LoginOpActivity.cookie;
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		if (sessionCookie != null) {
			//cookieManager.removeSessionCookie();
			String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
			cookieManager.setCookie(LoginActivity.POST_URL, cookieString);
			CookieSyncManager.getInstance().sync();
		}
		
		
		webView = (WebView)findViewById(R.id.webView_ing);
		//webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setJavaScriptEnabled(true);
		//webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				getSherlock().setProgress(progress*100);
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.endsWith("logout.phtml")) { // si pone salir en el SIDING, volvemos a la LoginActivity
					NavUtils.navigateUpFromSameTask((Activity)view.getContext());
					return true;
				}
				else if (url.equals("http://www.ing.puc.cl/")) { // si entra a esa pagina, es porque fallo el login (datos incorrectos)
					Toast.makeText(getApplicationContext(), R.string.LoginFailed, Toast.LENGTH_SHORT).show();
					startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
		});
		
		webView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype, long contentLength) {
				Toast.makeText(WebActivity.this, "Iniciando descarga ...", Toast.LENGTH_SHORT).show();
				new DownloadFile(WebActivity.actualNotifyId++).execute(url);	
			}
		});
		webView.loadUrl(url);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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
	
	/**
	 * Clase que se encarga de descargar el archivo. El proceso devuelve un String[] de dos elementos. 
	 * La primera posicion es el tipo de archivo y la segunda es el path en donde se descarg� el archivo.
	 * @author Lukas Zorich
	 *
	 */
	private class DownloadFile extends AsyncTask<String, String, String[]> {
		
		private int _notifyId;
		
		public DownloadFile(int notifyId) {
			this._notifyId = notifyId;
		}
		
		@Override
		protected void onPreExecute() {
			mNotifyManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			mBuilder = new NotificationCompat.Builder(WebActivity.this);
			mBuilder.setContentTitle("Descargando archivo ...")
					.setContentText("")
					.setSmallIcon(R.drawable.ic_notification_download)
					.setProgress(0,0, true);
		}

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
		        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
		        
		        // hacemos la notificacion
		        mBuilder.setContentText(filename + " - 0.0 Mb");
		        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
		        notificationIntent.setDataAndType(Uri.fromFile(file), urlConnection.getContentType());
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
		                
		                // actualizamos el tamaño descargado
				        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				        	mBuilder.setContentText(filename + " - " + String.format("%.1f", downloadedSize/(1000.0*1000)) + " Mb");
				        	mNotifyManager.notify(_notifyId, mBuilder.build());
				        }
		        }

		        fileOutput.close();
		        return new String[] {urlConnection.getContentType(), file.getAbsolutePath(), "" + downloadedSize/(1000.0*1000)};
			} catch (MalformedURLException e) {
				return null;
			} catch (IOException e) {
				return null;
			} catch (Exception e) {
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			if (result != null) {
				File file = new File(result[1]); 
	            if(file.exists()) 
	            {
	            	mBuilder.setContentTitle(file.getName())
	            			.setContentText("Descarga finalizada")
	            			.setAutoCancel(true)
	            			.setProgress(0, 0, false);
	            	mNotifyManager.notify(_notifyId, mBuilder.build());
	            	Toast.makeText(WebActivity.this, "Descarga finalizada", Toast.LENGTH_SHORT).show();
	            }
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.ErrorConexion, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
 
}
