package com.holamundo.ciudaduniversitariainteligente;

import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class Notifications extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Notifications() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final String mensaje= remoteMessage.getNotification().getBody();
        if(mensaje!=null){
            Handler handler= new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
