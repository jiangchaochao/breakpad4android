package com.jiangc.libbreakpad;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @time: 2022/2/22
 * @author: jiangchao
 * @describe: 异常捕获管理类, 包含Java及native异常捕获，将异常信息保存到指定目录中(默认为app内部存储catch)
 **/
public class CrashManager implements ICrashCallback {

    private static CrashManager instance = null;
    private String mPath = null;
    private JniCrashMonitor jniCrashMonitor = null;

    private CrashManager() {
    }

    public static CrashManager getInstance() {
        if (null == instance) {
            synchronized (CrashManager.class) {
                if (null == instance) {
                    instance = new CrashManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化异常捕获
     */
    public void init(Context context) {
        if (mPath == null || mPath.isEmpty()) {
            mPath = context.getCacheDir().getPath();
        }
        JavaCrashMonitor.init(context, mPath, this);
        if (jniCrashMonitor == null) {
            jniCrashMonitor = new JniCrashMonitor();
            jniCrashMonitor.init(context, mPath, this);
        }
    }

    public void init(Context context, String path) {
        mPath = path;
        init(context);
    }


    /**
     * Java异常测试
     */
    public void testJavaCrash() {
        int a = 10 / 0;
    }

    /**
     * native异常测试
     */
    public void testNativeCrash() {
        if (null != jniCrashMonitor) {
            jniCrashMonitor.testNativeCrash();
        }
    }

    @Override
    public void onCrash(String path) {

    }

    /**
     * 保存堆栈文件到Download,如果需要导出日志分析就调用此方法
     * 此方法会删除catch目录下的文件
     *
     * @param context 上下文
     */
    public static void saveStackToDownload(Context context) {
        new Thread(() -> {
            File cacheDir = context.getCacheDir();
            File[] files = cacheDir.listFiles();
            if (files != null && files.length != 0) {
                for (File file : files) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        FileOutputStream os = null;
                        FileInputStream is = null;
                        try {
                            os = new FileOutputStream(externalStoragePublicDirectory.getPath() + "/" + file.getName());
                            is = new FileInputStream(file);
                            int read;
                            byte[] buffer = new byte[1024];
                            while ((read = is.read(buffer)) != -1) {
                                os.write(buffer, 0, read);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        // 获取uri
                        Uri uri = insertFileIntoMediaStore(context, file.getName(), file.getPath());
                        // 保存到Download
                        saveFile(context, uri, file.getPath());
                    }
                    // 删除catch中的文件
                    boolean delete = file.delete();
                }
            }
        }).start();
    }

    //这里的fileName指文件名，不包含路径
    //relativePath 包含某个媒体下的子路径
    private static Uri insertFileIntoMediaStore(Context context, String fileName, String relativePath) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        //设置文件参数到ContentValues中
        ContentValues values = new ContentValues();
        //设置文件名
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        //设置文件类型
        values.put(MediaStore.Downloads.MIME_TYPE, "/");
        //EXTERNAL_CONTENT_URI代表外部存储器
        Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        //insertUri表示文件保存的uri路径
        return resolver.insert(external, values);
    }

    private static void saveFile(Context context, Uri insertUri, String sourcePath) {
        if (insertUri == null) {
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            os = resolver.openOutputStream(insertUri);
            if (os == null) {
                return;
            }
            int read;
            File sourceFile = new File(sourcePath);
            if (sourceFile.exists()) { // 文件存在时
                is = new FileInputStream(sourceFile); // 读入原文件
                byte[] buffer = new byte[1024];
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
