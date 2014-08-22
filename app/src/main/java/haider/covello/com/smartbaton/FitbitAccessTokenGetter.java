package haider.covello.com.smartbaton;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;

import fitbit.FitbitAPIException;
import fitbit.client.FitBitApiClientAgent;
import fitbit.client.FitbitAPIEntityCache;
import fitbit.client.FitbitApiCredentialsCache;
import fitbit.client.FitbitApiCredentialsCacheMaplmpl;
import fitbit.client.FitbitApiEntityCacheMaplmpl;
import fitbit.client.FitbitApiSubscriptionStorage;
import fitbit.client.FitbitApiSubscriptionStorageInMemorylmpl;
import fitbit.client.LocalUserDetail;
import fitbit.client.http.AccessToken;
import fitbit.client.http.TempCredentials;
import fitbit.client.service.FitbitAPIClientService;
import fitbit.common.model.user.UserInfo;
import fitbit.model.APIResourceCredentials;

/**
 * Created by Haider on 8/20/2014.
 */
public class FitbitAccessTokenGetter {

    public final static String TAG = "FitbitOAuthAccessTokenGetterPinBased";

    public final static String apiBaseUrl = "https://api.fitbit.com";
    public final static String webBaseUrl = "https://www.fitbit.com";

    // https://dev.fitbit.com/apps/new for application registered in here
    // consumer key and consumer secret
    private String consumerKey;
    private String consumerSecret;

    private FitBitApiClientAgent clientAgent;
    private TempCredentials tempCredentials;
    private String authorizationUrl;
    private String pin;
    private AccessToken accessToken;

    private String userId;
    private String token;
    private String tokenSecret;

    private APIResourceCredentials apiResourceCredentials;
    private FitbitAPIClientService<FitBitApiClientAgent> apiClientService;
    private UserInfo userInfo;



    public FitbitAccessTokenGetter(String consumerKey, String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMaplmpl();
        FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMaplmpl();
        FitbitApiSubscriptionStorage subscriptionStorage = new FitbitApiSubscriptionStorageInMemorylmpl();
        clientAgent = new FitBitApiClientAgent(apiBaseUrl, webBaseUrl, credentialsCache);

        apiClientService
                = new FitbitAPIClientService<FitBitApiClientAgent>(
                    clientAgent,
                    consumerKey,
                    consumerSecret,
                    credentialsCache,
                    entityCache,
                    subscriptionStorage
        );

        // Grab temp credentials
        try {
            tempCredentials = clientAgent.getOAuthTempToken();
            authorizationUrl = tempCredentials.getAuthorizationURL();
            Log.e(TAG, "Authorization URL: " + authorizationUrl);
        } catch (FitbitAPIException e) {
            // Catch exception
            Log.e(TAG, "Exception getting temporary token: " + e);
        }

        // Grab the pin
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        try {
            pin = r.readLine();
        } catch (IOException e) {
            e.printStackTrace();;
        }

        // Grab AccessToken
        try {
            accessToken = clientAgent.getOAuthAccessToken(tempCredentials, pin);
        } catch (FitbitAPIException e) {
            Log.e(TAG, "FitbitAPIException: " + e);
        }

        userId = accessToken.getEncodedUserId();
        token = accessToken.getToken();
        tokenSecret = accessToken.getTokenSecret();
    }

    protected void setApiResourceCredentials() {
        apiResourceCredentials = new APIResourceCredentials(userId, token, tokenSecret);
        apiResourceCredentials.setAccessToken(token);
        apiResourceCredentials.setAccessTokenSecret(tokenSecret);
        apiResourceCredentials.setResourceId(userId);

        LocalUserDetail userDetail = new LocalUserDetail(userId);
        apiClientService.saveResourceCredentials(userDetail, apiResourceCredentials);

        FitBitApiClientAgent agent = apiClientService.getClient();

        try {
            userInfo = agent.getUserInfo(userDetail);
        } catch (FitbitAPIException e) {
            e.printStackTrace();
            Log.e(TAG, "FitbitAPIException: " + e);
        }
    }

    public String getUserName() {
        return userInfo.getFullName();
    }

    public String getUserId() {
        return userId;
    }

    public String getPin() {
        return pin;
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

}
