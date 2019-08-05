package com.gx.fileexplorer.task;

import android.os.Handler;
import android.os.Process;

import com.gx.fileexplorer.AppConstants;
import com.gx.fileexplorer.utils.FileUtils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaoxuan on 2016/10/8.
 */
public class CopyTask implements Task<Integer> {
    private static AtomicInteger mCount = new AtomicInteger(0);
    private List<String> oldFileList;
    private String newPath;
    private Handler handler;

    public CopyTask(List<String> oldFileList, String newPath, Handler handler) {
        this.oldFileList = oldFileList;
        this.newPath = newPath;
        this.handler = handler;
    }

    @Override
    public Integer call() throws Exception {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Iterator<String> it = oldFileList.iterator();
        String temp;
        while (it.hasNext()) {
            temp = it.next();
            FileUtils.copy(temp, newPath);
        }
        handler.sendEmptyMessage(AppConstants.MSG_COPY_OK);
        return null;
    }

    @Override
    public String getTaskName() {
        return "copy-task-" + mCount.incrementAndGet();
    }
}
