package haider.covello.com.smartbaton;


import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

/**
 * Created by Haider on 8/16/2014.
 */
public class FitBitActivity extends DefaultApi10a {
    private static final String AUTHORIZATION_URL = "http://www.fitbit.com/oauth/authorize?oauth_token=%s";

    @Override
    public String getRequestTokenEndpoint() {
        return "http://api.fitbit.com/oauth/request_token";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "http://api.fitbit.com/oauth/access_token";
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return String.format(AUTHORIZATION_URL, requestToken.getToken());
    }
}
