package haider.covello.com.smartbaton;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;


public class MainActivity extends Activity {

    OAuthService service;
    Token requestToken;
    // Replace these with your own api key and secret
    private String apiKey = "c66f06524a6e4ce1a0e18329542a5173";
    private String apiSecret = "07bfbe756e3344aaae4af689113b8f50";

    private String accessToken;
    private String tokenSecret;

    private WebView wvAuthorize;
    private JSONObject jsonResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wvAuthorize = (WebView) findViewById(R.id.wvAuthorize);
        final EditText etPIN = (EditText) findViewById(R.id.etPIN);

        service = new ServiceBuilder().provider(FitbitApi.class).apiKey(apiKey)
                .apiSecret(apiSecret).build();


        // network operation shouldn't run on main thread
        new Thread(new Runnable() {
            public void run() {
                requestToken = service.getRequestToken();
                final String authURL = service
                        .getAuthorizationUrl(requestToken);

                // Webview nagivation should run on main thread again...
                wvAuthorize.post(new Runnable() {
                    @Override
                    public void run() {
                        wvAuthorize.loadUrl(authURL);

                        if (accessToken != null) {
                            wvAuthorize.setVisibility(View.GONE);
                        }

                    }
                });
            }
        }).start();

    }

    public void btnRetrieveData(View view) {
        EditText etPIN = (EditText) findViewById(R.id.etPIN);
        String gotPIN = etPIN.getText().toString();

        final Verifier v = new Verifier(gotPIN);

        // network operation shouldn't run on main thread
        new Thread(new Runnable() {
            public void run() {
                Token accessToken = service.getAccessToken(requestToken, v);

                OAuthRequest request = new OAuthRequest(Verb.GET,
                        "http://api.fitbit.com/1/user/-/profile.json");
                service.signRequest(accessToken, request); // the access token from step
                // 4
                final Response response = request.send();
                final TextView tvOutput = (TextView) findViewById(R.id.tvOutput);


                // Visual output should run on main thread again...
                tvOutput.post(new Runnable() {
                    @Override
                    public void run() {
                        tvOutput.setText(response.getBody());

                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
