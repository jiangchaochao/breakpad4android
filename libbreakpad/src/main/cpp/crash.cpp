//
// Created by Administrator on 2022/2/22.
//
#include <iostream>
#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdio.h>
#include <time.h>
#include <ctime>
#include "breakpad/src/client/linux/handler/minidump_descriptor.h"
#include "breakpad/src/client/linux/handler/exception_handler.h"

#define TAG "NativeCrash" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型
using namespace std;

// 回调签名：(Ljava/lang/String;)V


std::string filePath;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_6;
}

bool
DumpCallback(const google_breakpad::MinidumpDescriptor &descriptor, void *context, bool succeeded) {
    LOGE("Dump path: %s\n", descriptor.path());
    // 重命名文件
    time_t t = time(NULL);
    struct tm *stime = localtime(&t);
    char tmp[64];
    sprintf(tmp, "%04d_%02d_%02d_%02d_%02d_%02d", 1900 + stime->tm_year, 1 + stime->tm_mon,
            stime->tm_mday, stime->tm_hour, stime->tm_min, stime->tm_sec);
    LOGE("NEW file path: %s\n", (filePath + "/native_" + tmp + ".dmp").c_str());
    rename(descriptor.path(), (filePath + "/native_" + tmp + ".dmp").c_str());
    // return false 是让我们自己的程序处理完之后交给系统处理
    return false;
}

void Crash() {
    volatile int *a = reinterpret_cast<volatile int *>(NULL);
    *a = 1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jiangc_libbreakpad_JniCrashMonitor_init(JNIEnv
                                                 *env,
                                                 jobject thiz, jstring
                                                 path_) {
    const char *path = env->GetStringUTFChars(path_, nullptr);
    filePath = path;
    google_breakpad::MinidumpDescriptor descriptor(path);
// 加static 是为了延长它的声明周期，不然方法执行完就没了，就监测不到了,也可以放全局
    static google_breakpad::ExceptionHandler eh(descriptor, nullptr, DumpCallback, nullptr, true,
                                                -1);
    env->
            ReleaseStringUTFChars(path_, path
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jiangc_libbreakpad_JniCrashMonitor_testNativeCrash(JNIEnv
                                                            *env,
                                                            jobject thiz
) {
    Crash();

}