package com.flyfish.fileexplorer;

import android.os.Handler;
import android.os.Process;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaoxuan on 2016/10/20.
 */
public class CutTask implements Task<Integer> {
    private static AtomicInteger mCount = new AtomicInteger(0);
    private List<String> oldFileList;
    private String newPath;
    private Handler handler;

    public CutTask(List<String> oldFileList, String newPath, Handler handler) {
        this.oldFileList = oldFileList;
        this.newPath = newPath;
        this.handler = handler;
    }

    @Override
    public String getTaskName() {
        return "cut-task-" + mCount.incrementAndGet();
    }

    @Override
    public Integer call() throws Exception {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Iterator<String> it = oldFileList.iterator();
        String temp;
        while (it.hasNext()) {
            temp = it.next();
            FileUtils.move(temp, newPath + File.separator + temp.substring(temp.lastIndexOf(File.separator)));
        }
        handler.sendEmptyMessage(AppConstants.MSG_CUT_OK);
        return null;
    }
}
