package com.flyfish.fileexplorer;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by gaoxuan on 2016/10/8.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init();
    }
}
