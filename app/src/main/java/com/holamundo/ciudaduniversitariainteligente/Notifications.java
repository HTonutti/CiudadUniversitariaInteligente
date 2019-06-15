package com.holamundo.ciudaduniversitariainteligente;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Notifications extends Service {
    public Notifications() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
