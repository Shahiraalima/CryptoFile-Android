package com.example.cryptofile_android;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CryptoFIleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
