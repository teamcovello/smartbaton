package haider.covello.com.smartbaton;

import org.joda.time.LocalDate;

import java.util.List;

import fitbit.client.FitBitApiClientAgent;
import fitbit.client.FitbitAPIEntityCache;
import fitbit.client.FitbitApiCredentialsCache;
import fitbit.client.FitbitApiCredentialsCacheMaplmpl;
import fitbit.client.FitbitApiEntityCacheMaplmpl;
import fitbit.client.FitbitApiSubscriptionStorage;
import fitbit.client.FitbitApiSubscriptionStorageInMemorylmpl;
import fitbit.client.LocalUserDetail;
import fitbit.client.service.FitbitAPIClientService;
import fitbit.common.model.activities.Activities;
import fitbit.common.model.activities.ActivitiesSummary;
import fitbit.common.model.activities.ActivityDistance;
import fitbit.common.model.body.WeightLog;
import fitbit.common.model.sleep.Sleep;
import fitbit.common.model.sleep.SleepLog;
import fitbit.common.model.user.UserInfo;
import fitbit.common.service.FitbitApiService;
import fitbit.model.APIResourceCredentials;
import fitbit.model.FitbitUser;

/**
 * Created by Haider on 8/19/2014.
 */
public class FitbitUserInfo {

    public static void main(String[] args) throws Exception {

        String apiBaseUrl = "api.fitbit.com";
        String webBaseUrl = "https://www.fitbit.com";

        // Consumer key と Consumer secret
        String consumerKey = "c66f06524a6e4ce1a0e18329542a5173";
        String consumerSecret = "07bfbe756e3344aaae4af689113b8f50";

        // ユーザーの ID (不要) と Token と TokenSecret
        String userId = "-";
        String token = "1234512345abcdeabcde6789067890fg";
        String tokenSecret = "abcabcdefdef123123456456ghighijk";

        FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMaplmpl();
        FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMaplmpl();
        FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemorylmpl();
        FitBitApiClientAgent apiClientAgent = new FitBitApiClientAgent(apiBaseUrl, webBaseUrl, credentialsCache);

        FitbitAPIClientService<FitBitApiClientAgent> apiClientService
                = new FitbitAPIClientService<FitBitApiClientAgent>(
                apiClientAgent,
                consumerKey,
                consumerSecret,
                credentialsCache,
                entityCache,
                subscriptionStore
        );

        LocalUserDetail user = new LocalUserDetail(userId);

        APIResourceCredentials resourceCredentials = new APIResourceCredentials(userId, token, tokenSecret);
        resourceCredentials.setAccessToken(token);
        resourceCredentials.setAccessTokenSecret(tokenSecret);

        apiClientService.saveResourceCredentials(user, resourceCredentials);

        FitBitApiClientAgent agent = apiClientService.getClient();

        UserInfo userInfo = agent.getUserInfo(user);
        System.out.println(userInfo.getNickname());

        LocalDate date = FitbitApiService.getValidLocalDateOrNull("2013-01-11");

        // activity
        System.out.println("***** Activity *****");
        Activities activities = agent.getActivities(user, FitbitUser.CURRENT_AUTHORIZED_USER, date);
        ActivitiesSummary activitiesSummary = activities.getSummary();
        System.out.println(activitiesSummary.getCaloriesOut() + " calories burned");
        System.out.println("Elevation: " + activitiesSummary.getElevation());
        System.out.println(activitiesSummary.getFloors() + " floors climbed");
        System.out.println("Sedentary Minutes: " + activitiesSummary.getSedentaryMinutes() + "min");
        System.out.println("Very Active Minutes: " + activitiesSummary.getVeryActiveMinutes() + "min");
        System.out.println(activitiesSummary.getSteps() + " steps taken");
        for(ActivityDistance activityDistance : activitiesSummary.getDistances()){
            System.out.println("Distance(" + activityDistance.getActivity() + "): " + activityDistance.getDistance() + " km");
        }

        // sleep
        System.out.println("***** Sleep *****");
        Sleep sleep = agent.getSleep(user, FitbitUser.CURRENT_AUTHORIZED_USER, date);
        List<SleepLog> sleepLogList= sleep.getSleepLogs();
        for(SleepLog sleepLog : sleepLogList){
            if(sleepLog.isMainSleep()){
                System.out.println("Actual sleep time: " + (sleepLog.getMinutesAsleep() / 60) + "hrs " + (sleepLog.getMinutesAsleep() % 60) + "min");
                System.out.println("Bed time: " + sleepLog.getStartTime());
                System.out.println("Fell asleep in: " + sleepLog.getMinutesToFallAsleep() + "min");
                System.out.println("Awakened: " + sleepLog.getAwakeningsCount() + " times");
                System.out.println("In bed time: " + (sleepLog.getTimeInBed() / 60) + "hrs " + (sleepLog.getTimeInBed() % 60) + "min");
                System.out.println("Sleep efficiency: " + sleepLog.getEfficiency() + "%");
                System.out.println("Duration: " + sleepLog.getDuration());
                System.out.println("After Wakeup: " + sleepLog.getMinutesAfterWakeup() + "min");
                System.out.println("Awake: " + sleepLog.getMinutesAwake() + "min");
            }
        }

        // weight
        System.out.println("***** Weight *****");
        List<WeightLog> wwightLogLst = agent.getLoggedWeight(user, FitbitUser.CURRENT_AUTHORIZED_USER, date);
        for(WeightLog weightLog : wwightLogLst){
            System.out.println("Date: " + weightLog.getDate());
            System.out.println("Time: " + weightLog.getTime());
            System.out.println("Weight: " + weightLog.getWeight());
            System.out.println("BMI: " + weightLog.getBmi());
        }
    }

}
