package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.FcmTokenCreationData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    private void registerToken(final String token) {
        final Context self = this;

        final SharedPreferences preferences = self.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        final PfoertnerService service = PfoertnerService.makeService();

        new RequestTask<Void>() {
            @Override
            protected Void doRequests() {
                final Password pswd = Password.loadPassword(preferences);
                final User device = User.loadDevice(preferences, service, pswd);
                final Authentication authentication = Authentication.authenticate(preferences, service, device, pswd, self);

                service.setFcmToken(authentication.id, device.id, new FcmTokenCreationData(token));

                return null;
            }

            //TODO, was tun bei onException?
        }.execute();
    }

    @Override
    public void onNewToken(final String token) {
        Log.d(TAG, "Refreshed token: " + token);

        registerToken(token);
    }
}
