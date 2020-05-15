package com.leedoo.printerdemo;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
       // NewPrinterManager.createInstance(getApplicationContext());
       // ACSEVKCardReaderManager.createInstance(getApplicationContext());
    }
}
