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

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginOpActivity extends SherlockActivity {
	
	public final static String POST_URL = "https://intrawww.ing.puc.cl/siding/index.phtml";
	public final static String INGCURSOS_URL = "https://intrawww.ing.puc.cl/siding/dirdes/ingcursos/cursos/index.phtml";
	private final static String USER_ID = "login";
	private final static String PASSWD_ID = "passwd";
	public final static String EXTRA_URL = "EXTRA_URL";
	
	public static Cookie cookie;
	
	//protected ProgressDialog progressDialog;
	protected boolean recordarPref;
	protected String userPref, passwdPref;
	protected boolean redirigirIngCursos;
	protected boolean entrarAuto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = getSharedPreferences(DirectSIDING.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		recordarPref = prefs.getBoolean(DirectSIDING.RECORDAR_PREF, false);
		redirigirIngCursos = prefs.getBoolean(DirectSIDING.INGCURSOS_PREF, false);
		userPref = prefs.getString(DirectSIDING.USUARIO_PREF, "user");
		passwdPref = prefs.getString(DirectSIDING.PASSWD_PREF, "password");
		entrarAuto = prefs.getBoolean(DirectSIDING.ENTRARAUTO_PREF, false);
		
	}
	
	protected void login(String usuario, String password) {
		new LoginOperation(this).execute(usuario, password);
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
 
        ((DirectSIDING) getApplication()).detach(this);
    }
 
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
 
        ((DirectSIDING) getApplication()).attach(this);
    }
    
	
	protected static class LoginOperation extends CustomAsyncTask<String, Void, Boolean> {
		
		private ProgressDialog mProgressDialog;
		
		private boolean mRedirigirIngCursos;
		
		public LoginOperation(LoginOpActivity activity) {
			super(activity);
			
			mRedirigirIngCursos = activity.redirigirIngCursos;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog();
		}
		
		@Override
		protected void onActivityDetached() {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		}
		
		@Override
		protected void onActivityAttached() {
			showProgressDialog();
		}
		
		private void showProgressDialog() {
			mProgressDialog = new ProgressDialog(mActivity);
			mProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage("Ingresando al SIDING ...");
			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			
			mProgressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			// llamamos a la pagina
			//DefaultHttpClient httpClient = new DefaultHttpClient();
			DefaultHttpClient httpClient = getNewHttpClient();
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
			super.onPostExecute(result);
			
			//boolean redirigirIngCursos = ((LoginOpActivity)mActivity).redirigirIngCursos;
			
			//mProgressDialog.dismiss();
			if (mActivity != null) {
				if (result) {
					String url = mRedirigirIngCursos ? INGCURSOS_URL : POST_URL;
					Intent i = new Intent(mActivity, WebActivity.class);
					i.putExtra(EXTRA_URL, url);
					mActivity.startActivity(i);
				} else { 
					Toast.makeText(mActivity, R.string.ErrorConexion, Toast.LENGTH_SHORT).show();
				}
				
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
		} 
		
		private DefaultHttpClient getNewHttpClient() {
			 try {
			        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			        trustStore.load(null, null);

			        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			        HttpParams params = new BasicHttpParams();
			        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			        SchemeRegistry registry = new SchemeRegistry();
			        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			        registry.register(new Scheme("https", sf, 443));

			        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			        return new DefaultHttpClient(ccm, params);
			    } catch (Exception e) {
			        return new DefaultHttpClient();
			    }
		}
	}
	
	/*private static class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }
	    
	    public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
	        super(null);
	        sslContext = context;
	     }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}*/

}
