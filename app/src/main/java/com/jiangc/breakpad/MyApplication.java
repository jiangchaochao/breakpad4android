package com.jiangc.breakpad;

import android.app.Application;

import com.jiangc.libbreakpad.CrashManager;

/**
 * @time: 2022/8/28
 * @author: jiangchao
 * @describe:
 **/
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashManager.getInstance().init(this);
        CrashManager.saveStackToDownload(this);
    }
}
