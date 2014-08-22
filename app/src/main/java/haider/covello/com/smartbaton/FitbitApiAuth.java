package haider.covello.com.smartbaton;

import android.util.Log;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fitbit.FitbitAPIException;
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
 * Created by Haider on 8/20/2014.
 */
public class FitbitApiAuth extends HttpServlet {

    private final String TAG = "FitbitApiAuth";

    public static final String OAUTH_TOKEN = "oauth_token";
    public static final String OAUTH_VERIFIER = "oauth_verifier";

    private FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMaplmpl();
    private FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMaplmpl();
    private FitbitApiSubscriptionStorage subscriptionStorage = new FitbitApiSubscriptionStorageInMemorylmpl();

    private String apiBaseUrl;
    private String fitbitSiteBaseUrl;
    private String exampleBaseUrl;
    private String clientConsumerKey;
    private String clientSecret;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // Setup properties of keys and values
        try {
            Properties properties = new Properties();
            properties.load(((Object) this).getClass().getClassLoader().getResourceAsStream("config.properties"));
            apiBaseUrl = properties.getProperty("apiBaseUrl");
            fitbitSiteBaseUrl = properties.getProperty("apiBaseUrl");
            exampleBaseUrl = properties.getProperty("exampleBaseUrl").replace("/app", "");
            clientConsumerKey = properties.getProperty("clientConsumerKey");
            clientSecret = properties.getProperty("clientSecret");
        } catch (IOException e) {
            throw new ServletException("Exception during loading properties");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Setup FitbitClientService
        FitbitAPIClientService<FitBitApiClientAgent> apiClientService = new FitbitAPIClientService<FitBitApiClientAgent>(
                new FitBitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl, credentialsCache),
                clientConsumerKey,
                clientSecret,
                credentialsCache,
                entityCache,
                subscriptionStorage
        );

        if (request.getParameter("completeAuthorization") != null) {
            String tempTokenReceived = request.getParameter(OAUTH_TOKEN);
            String tempTokenVerifier = request.getParameter(OAUTH_VERIFIER);
            APIResourceCredentials resourceCredentials = apiClientService.getResourceCredentialsByTempToken(tempTokenReceived);

            if (resourceCredentials == null) {
                throw new ServletException("Unrecognized temporary token when attempting to complete authorization: " +
                tempTokenReceived);
            }

            // Get token credentials only if necessary:
            if (!resourceCredentials.isAuthorized()) {
                // The verifier received in the request to get token credentials:
                resourceCredentials.setTempTokenVerifier(tempTokenVerifier);
                try {
                    // Get token credentials for user:
                    apiClientService.getTokenCredentials(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                } catch (FitbitAPIException e) {
                    Log.e(TAG, "Unable to authorize with Fitbit: " + e);
                    throw new ServletException("Unable to authorize with Fitbit: " +
                     e);
                }
            }

            try {
                UserInfo userInfo = apiClientService.getClient().getUserInfo(new LocalUserDetail(resourceCredentials.getLocalUserId()));
                request.setAttribute("userInfo", userInfo);
                request.getRequestDispatcher("/fitbitApiAuth.jsp").forward(request, response);
            } catch (FitbitAPIException e) {
                Log.e(TAG, "Exception during retrieving user info: " + e);
                throw new ServletException("Exception during retreiving user info: " + e);
            }
        } else {
            try {
                response.sendRedirect(apiClientService.getResourceOwnerAuthorizationURL(new LocalUserDetail("-"), exampleBaseUrl + "/fitbitApiAuth?completeAuthorization="));
            } catch (FitbitAPIException e) {
                Log.e(TAG, "Exception during authorization: " + e);
                throw new ServletException("Exception performing authorization: " + e);
            }
        }
    }

}
