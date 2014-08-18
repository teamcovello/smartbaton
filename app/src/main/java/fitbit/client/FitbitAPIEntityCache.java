package fitbit.client;

import fitbit.model.APIResourceCredentials;

public interface FitbitAPIEntityCache {
    Object get(APIResourceCredentials credentials, Object key);
    Object put(APIResourceCredentials credentials, Object key, Object value);
    Object remove(APIResourceCredentials credentials, Object key);
}
