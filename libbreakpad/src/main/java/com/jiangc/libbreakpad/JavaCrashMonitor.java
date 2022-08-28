package com.jiangc.libbreakpad;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @time: 2022/2/22
 * @author: jiangchao
 * @describe: java 未捕获的异常处理类,保存文件到/data/data/app package/cache中
 **/
public class JavaCrashMonitor implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "JavaCrashMonitor";
    private static Context mContext = null;
    private static String mPath = null;
    private static ICrashCallback mCallback;
    // 程序默认的异常处理handler-->kill
    static Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    private JavaCrashMonitor() {

    }

    public static void init(Context context, String path, ICrashCallback callback) {
        mPath = path;
        mCallback = callback;
        init(context);
    }

    public static void init(Context context) {
        mContext = context;
        if (TextUtils.isEmpty(mPath)) {
            mPath = mContext.getCacheDir().getPath();
        }
        // 这里保存一下系统默认的异常处理机制
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置我们自己的异常处理，之后在异常发生的时候就会调用我们自己的,为什么要保存，是因为我们自己处理完成之后，还要交给系统处理，否则会出现无限重启的情况
        Thread.setDefaultUncaughtExceptionHandler(new JavaCrashMonitor());
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 这里就是异常处理回调了
        final long l = System.currentTimeMillis();
        // 获取缓存目录
        try {
            File file = File.createTempFile(l + "_", null, mContext.getCacheDir());
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.println(t.getName());
            e.printStackTrace(printWriter);
            printWriter.flush();
            printWriter.close();
            // 重命名文件
            String filePath = renameFile(mContext.getCacheDir().getPath(), file.getName());
            if (null != mCallback) {
                mCallback.onCrash(filePath);
            }
            // TODO 删除源文件
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } finally {
            // 在这里调用系统的默认处理方式
            if (null != defaultUncaughtExceptionHandler) {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        }
    }

    /**
     * 重命名文件
     *
     * @param fileName
     * @return
     */
    public static String renameFile(String filePath, String fileName) {
        SimpleDateFormat fmdate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String oldFileName = filePath + "/" + fileName;
        File oldFile = new File(oldFileName);
        String newFileName = filePath + "/java_" + fmdate.format(new Date()) + "." + fileName.split("\\.")[1];
        Log.e(TAG, "renameFile: " + newFileName);
        File newFile = new File(newFileName);
        if (oldFile.exists() && oldFile.isFile()) {
            oldFile.renameTo(newFile);
        }

        return newFileName;
    }
}
