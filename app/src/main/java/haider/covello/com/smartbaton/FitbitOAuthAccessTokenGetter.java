package haider.covello.com.smartbaton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import fitbit.client.FitBitApiClientAgent;
import fitbit.client.FitbitAPIEntityCache;
import fitbit.client.FitbitApiCredentialsCache;
import fitbit.client.FitbitApiCredentialsCacheMaplmpl;
import fitbit.client.FitbitApiEntityCacheMaplmpl;
import fitbit.client.FitbitApiSubscriptionStorage;
import fitbit.client.FitbitApiSubscriptionStorageInMemorylmpl;
import fitbit.client.LocalUserDetail;
import fitbit.client.service.FitbitAPIClientService;
import fitbit.common.model.user.UserInfo;
import fitbit.model.APIResourceCredentials;


/**
 * Created by Haider on 8/18/2014.
 */
public class FitbitOAuthAccessTokenGetter {

    public static void main(String[] args ) throws Exception {

        String apiBaseUrl = "api.fitbit.com";
        String webBaseUrl = "https://www.fitbit.com";

        // Consumer Key and Secret Key
        String consumerKey = "c66f06524a6e4ce1a0e18329542a5173";
        String consumerSecret = "07bfbe756e3344aaae4af689113b8f50";

        // oauth callback url for oauth verification
        String callbackUrl = "http://fitbit.com/oauth/callback";

        FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMaplmpl();
        FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMaplmpl();
        FitbitApiSubscriptionStorage subscriptionStorage = new FitbitApiSubscriptionStorageInMemorylmpl();
        FitBitApiClientAgent clientAgent = new FitBitApiClientAgent(apiBaseUrl, webBaseUrl, credentialsCache);

        FitbitAPIClientService<FitBitApiClientAgent> apiClientService =
                new FitbitAPIClientService<FitBitApiClientAgent>(
                        clientAgent,
                        consumerKey,
                        consumerSecret,
                        credentialsCache,
                        entityCache,
                        subscriptionStorage
                );

        LocalUserDetail userDetail = new LocalUserDetail("-");
        String authorizationURL = apiClientService.getResourceOwnerAuthorizationURL(userDetail, callbackUrl);

        System.out.println(authorizationURL + " |Please allow authentication by accessing the web browser|");

        // Setup buffered reader for redirectUrl
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String redirectUrl = r.readLine();

        // query string
        Properties params = getParameters(redirectUrl);

        String oauth_token = params.getProperty("oauth_token");
        String oauth_verifier = params.getProperty("oauth_verifier");
        APIResourceCredentials resourceCredentials =
                apiClientService.getResourceCredentialsByTempToken(oauth_token);

        if (resourceCredentials == null) {
            throw new Exception("Unrecognized temporary token when attempting to complete authorization: " +
            oauth_token);
        }
        if (!resourceCredentials.isAuthorized()) {
            resourceCredentials.setTempTokenVerifier(oauth_verifier);
            apiClientService.getTokenCredentials(new LocalUserDetail(resourceCredentials.getLocalUserId()));
        }

        // authorization variables
        String userId = resourceCredentials.getLocalUserId();
        String token = resourceCredentials.getAccessToken();
        String tokenSecret = resourceCredentials.getAccessTokenSecret();

        System.out.println("UserId=" + userId);
        System.out.println("Token=" + token);
        System.out.println("TokenSecret=" + tokenSecret);

        LocalUserDetail user = new LocalUserDetail(userId);
        FitBitApiClientAgent agent = apiClientService.getClient();
        //agent.setOAuthAccessToken(accessToken);
        UserInfo userInfo = agent.getUserInfo(user);
        System.out.println(userInfo.getNickname());

    }

    // query string
    private static Properties getParameters(String url) {
        Properties params = new Properties();
        String query_string = url.substring(url.indexOf('?') + 1);
        String[] pairs = query_string.split("&");

        for (String pair : pairs) {
            String[] kv = pair.split("=");
            params.setProperty(kv[0], kv[1]);
        }

        return params;
    }

}
