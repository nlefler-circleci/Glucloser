package com.nlefler.glucloser.util;

/**
 * Created by nathan on 8/2/14.
 */
public abstract class Callback<T> {
    public abstract void call(T data);
    public abstract void error(String message);
}
