package com.jiangc.libbreakpad;

/**
 * @time: 2022/2/23
 * @author: jiangchao
 * @describe:
 **/
public interface ICrashCallback {
    void onCrash(String path);
}
