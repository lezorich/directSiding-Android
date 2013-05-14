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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
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
import com.directsiding.Interfaces.ISharedPrefs;

@SuppressLint("setJavaScriptEnabled")
public class WebActivity extends SherlockActivity {
	
	private WebView webView; 
	private DownloadManager downloadManager;
	private long downloadReference;
	
	private ProgressDialog downloadDialog;
	

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
		
		downloadDialog = new ProgressDialog(this);
		downloadDialog.setIndeterminate(false);
		downloadDialog.setMax(100);
		downloadDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
		downloadDialog.setTitle("Descargando ...");
		downloadDialog.setCancelable(true);
		
		
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
				new DownloadFile().execute(url);	
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
    
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if (downloadReference == referenceId) {
					DownloadManager.Query query = new DownloadManager.Query();
					query.setFilterById(referenceId);
					
					Cursor c = downloadManager.query(query);
					
					if (c.moveToFirst()) {
						Uri uri = Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
						//Intent i = new Intent(Intent.ACTION_VIEW, uri);
						//context.startActivity(i);
						/*Toast.makeText(context, "status: " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
								+ " file type: " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))
								+ " loc: " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME))
								+ " a: " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
								, Toast.LENGTH_SHORT).show();*/
						Log.d("file type:", c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
					}
				}
			}
		}
	};
	
	/**
	 * Clase que se encarga de descargar el archivo. El proceso devuelve un String[] de dos elementos. 
	 * La primera posicion es el tipo de archivo y la segunda es el path en donde se descargó el archivo.
	 * @author Lukas Zorich
	 *
	 */
	private class DownloadFile extends AsyncTask<String, Integer, String[]> {
		
		@Override
		protected void onPreExecute() {
			downloadDialog.setProgress(0);
			downloadDialog.show();
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
	
		        FileOutputStream fileOutput = new FileOutputStream(file);
		        
		        InputStream inputStream = urlConnection.getInputStream();
	
		        int totalSize = urlConnection.getContentLength();

		        int downloadedSize = 0;
	

		        byte[] buffer = new byte[1024];
		        int bufferLength = 0; 
	

		        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
		                fileOutput.write(buffer, 0, bufferLength);
		                downloadedSize += bufferLength;
		                publishProgress(downloadedSize, totalSize);
		        }

		        fileOutput.close();
		        return new String[] {urlConnection.getContentType(), file.getAbsolutePath()};
			} catch (MalformedURLException e) {
			        return null;
			} catch (IOException e) {
			        return null;
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			downloadDialog.setProgress(values[0]*100/values[1]);
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			downloadDialog.dismiss();
			if (result != null) {
				File file = new File(result[1]); 
	            if(file.exists()) 
	            {
	                Uri path = Uri.fromFile(file); 
	                Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
	                openFileIntent.setDataAndType(path, result[0]);
	                //pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(openFileIntent);
	            }
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.ErrorConexion, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
 
}
