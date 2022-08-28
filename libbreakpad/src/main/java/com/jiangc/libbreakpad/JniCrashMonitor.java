package com.jiangc.libbreakpad;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * @time: 2022/2/22
 * @author: jiangchao
 * @describe: native crash 捕获 保存文件到/data/data/app package/cache中
 **/
public class JniCrashMonitor {
    private static final String TAG = "JniCrashMonitor";
    private Context mContext;
    private String mPath;
    private ICrashCallback mCallback;

    static {
        System.loadLibrary("crash");
    }

    /**
     * 带日志存储路径
     *
     * @param context
     * @param path
     */
    public void init(Context context, String path, ICrashCallback callback) {
        mPath = path;
        mCallback = callback;
        init(context);
    }

    /**
     * 默认存储路径
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        if (TextUtils.isEmpty(mPath)) {
            mPath = mContext.getCacheDir().getPath();
        }
        init(mPath);
    }

    /**
     * 初始化，传入存放文件的目录
     */
    private native void init(String path);

    /**
     * 测试本地崩溃
     */
    public native void testNativeCrash();

    /**
     * ndk回调
     * @param path
     */
    public void callback(String path) {

        Log.e(TAG, "callback: " );

    }
}
