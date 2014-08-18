package fitbit.client;

import fitbit.model.APIResourceCredentials;

public interface FitbitApiCredentialsCache {

    APIResourceCredentials getResourceCredentials(LocalUserDetail user);

    APIResourceCredentials getResourceCredentialsByTempToken(String tempToken);

    APIResourceCredentials saveResourceCredentials(LocalUserDetail user, APIResourceCredentials resourceCredentials);

    APIResourceCredentials expireResourceCredentials(LocalUserDetail user);

}
