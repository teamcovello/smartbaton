package haider.covello.com.smartbaton;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import fitbit.client.http.HttpClient;

/**
 * Created by Haider on 8/16/2014.
 */
public class FitBitActivity extends Activity {

    WebView webView;
    static String authToken;
    Context context = this;
    ProgressDialog progressBar;
    String finalAuthToken = "";
    String finalAuthTokenSecret = "";
    String finalEncodedUserId = "";
    String authVerifier = "";
    String tempAuthToken = "";
    String fitbitUser = "";
    SharedPreferences sharedPref;
    String userId;

    /*
    Temporary Client credentials
     */
    private String consumerKey = "c66f06524a6e4ce1a0e18329542a5173";
    private String consumerSecret = "07bfbe756e3344aaae4af689113b8f50";

    /*
    Request, Access URLs
     */
    private String requestTokenUrl = "https://api.fitbit.com/oauth/request_token";
    private String accessTokenUrl = "https://api.fitbit.com/oauth/access_token";
    private String authorizeUrl = "https://www.fitbit.com/oauth/authorize";
    private String callbackUrl = "http://fitbit.com/oauth/callback";
    private Token requestToken;
    private OAuthService service;
    private Token accessToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitbit);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /*
        Get User ID from shared preferences
         */
        sharedPref = context.getSharedPreferences("savedCredentials", MODE_PRIVATE);
        userId = sharedPref.getString("userId", userId);
        System.out.println("User ID:**************" + userId);

        webView = (WebView) findViewById(R.id.webview1);
        new login().execute();
    }

    private void login() {

        try {
            HttpResponse response = null;
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
            HttpConnectionParams.setSoTimeout(httpParams, 20000);
            org.apache.http.client.HttpClient client = new DefaultHttpClient(httpParams);



            HttpGet request = new HttpGet(
                    "http://api.fitbit.com/oauth/request_token?oauth_consumer_key=" + consumerKey + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1358921319&oauth_nonce=456236281&oauth_callback=" + callbackUrl + "&oauth_token=" + accessToken + "&oauth_signature=QdVUzMvT6tveGyoPu%2BEevzvo07s%3D");

            response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()
            ));

            String webServiceInfo = "";
            while ((webServiceInfo = rd.readLine()) != null) {
                Log.d("****Step 1***", "Webservice: " + webServiceInfo);
                String[] result = webServiceInfo.split("=");
                String result2 = result[1];
                String[] result3 = result2.split("&");
                authToken = result3[0];
                Log.d("Auth Token:", "Webservice: " + authToken);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFinalToken() {

        try {
            HttpResponse response = null;
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
            HttpConnectionParams.setSoTimeout(httpParams, 20000);
            org.apache.http.client.HttpClient client = new DefaultHttpClient(httpParams);

            HttpGet request = new HttpGet(
                    "http://api.fitbit.com/oauth/access_token?oauth_consumer_key=7af733f021f649bcac32f6f7a4fe2e9a&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1358921319&oauth_nonce=456236281&oauth_signature=QdVUzMvT6tveGyoPu%2BEevzvo07s%3D&oauth_version=1.0&oauth_verifier="
                            + authVerifier + "&oauth_token=" + tempAuthToken
            );

            response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()
            ));

            String webServiceInfo2 = "";
            while ((webServiceInfo2 = rd.readLine()) != null) {
                Log.d("****Step 2***", "Webservice: " + webServiceInfo2
                    + "---Size: " + webServiceInfo2.length());
                finalAuthToken = webServiceInfo2.substring(12, 44);
                finalAuthTokenSecret = webServiceInfo2.substring(64, 96);
                finalEncodedUserId = webServiceInfo2.substring(113, 119);
                Log.d("Final Auth Token:", "Webservice Result: " + finalAuthToken +
                    "----" + finalAuthTokenSecret + "---" + finalEncodedUserId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void openCallbackUrl() {
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading...");
        progressBar.show();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getUrl();
                String finalToken = view.getUrl();
                Log.d("Final Whole Response: ", finalToken);
                Log.d("check url", finalToken.substring(11, 18));

                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }

                if (finalToken.substring(11, 18).equals("android")) {
                    Log.d("Final token length", String.valueOf(finalToken.length()));
                    tempAuthToken = finalToken.substring(43, 75);
                    authVerifier = finalToken.substring(91, 117);
                    Log.d("get temp auth token: ", tempAuthToken);
                    Log.d("get auth verifier: ", authVerifier);
                    new getFinalToken().execute();
                }
            }

        });

        String fitUrl = "https://www.fitbit.com/oauth/authorize?oauth_token="
                + authToken + "&display=touch";
        webView.loadUrl(fitUrl);

        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!arg0.hasFocus()) {
                            arg0.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

    }

    public class login extends AsyncTask<String, Void, String> {

        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(context, "Please Wait", "Loading please wait...", true);
            pd.setCancelable(true);

        }

        @Override
        protected String doInBackground(String... params) {
            login();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            openCallbackUrl();
            pd.dismiss();
        }


    }

    public class getFinalToken extends AsyncTask<String, Void, String> {
        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(context, "Please wait",
                    "Loading please wait..", true);
            pd.setCancelable(true);

        }

        @Override
        protected String doInBackground(String... params) {
            getFinalToken();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pd.dismiss();
            Intent intent = new Intent(FitBitActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack() == true) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


}
