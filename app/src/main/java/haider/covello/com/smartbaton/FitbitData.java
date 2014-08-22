package haider.covello.com.smartbaton;

import android.app.Activity;
import android.os.Bundle;

import fitbit.client.http.OAuth;

/**
 * Created by Haider on 8/20/2014.
 */
public class FitbitData extends Activity {

    private final String consumerKey = "c66f06524a6e4ce1a0e18329542a5173";
    private final String consumerSecret = "07bfbe756e3344aaae4af689113b8f50";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FitbitAccessTokenGetter accessTokenGetter = new FitbitAccessTokenGetter(consumerKey, consumerSecret);
    }

}
