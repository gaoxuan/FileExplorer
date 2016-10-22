package com.flyfish.fileexplorer;

import java.util.concurrent.Callable;

/**
 * Created by gaoxuan on 2016/10/20.
 */
public interface Task<T> extends Callable<T> {
    String getTaskName();
}