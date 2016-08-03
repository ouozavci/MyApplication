package com.example.oguzhan.myapplication;

import android.app.Application;
import android.content.Intent;

import com.firebase.client.Firebase;

/**
 * Created by oguzhan on 1.08.2016.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(getApplicationContext());
        startService(new Intent(this, NotificationListener.class));
    }
}
