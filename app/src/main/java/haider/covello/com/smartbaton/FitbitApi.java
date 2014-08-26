package haider.covello.com.smartbaton;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

/**
 * Created by Haider on 8/25/2014.
 */
public class FitbitApi extends DefaultApi10a {

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth/authorize?oauth_token=%s";

    public String getAccessTokenEndpoint() {
        return "https://api.fitbit.com/oauth/access_token";
    }

    public String getRequestTokenEndpoint() {
        return "https://api.fitbit.com/oauth/request_token";
    }

    public String getAuthorizationUrl(Token token) {
        return String.format(AUTHORIZE_URL, token.getToken());
    }

}
