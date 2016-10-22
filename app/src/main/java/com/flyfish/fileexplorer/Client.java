package com.flyfish.fileexplorer;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaoxuan on 2016/10/10.
 */
public class Client {
    private static Client mClient;
    private ExecutorService executorService;
    private HashMap<String, FutureTask> taskMap;

    private Client() {
        executorService = Executors.newFixedThreadPool(AppConstants.COUNT_THREAT_NORMAL, new CustomThreadFactory("^_^"));
        taskMap = new HashMap<>();
    }

    public static Client newInstance() {
        if (mClient == null) {
            synchronized (Client.class) {
                if (mClient == null)
                    mClient = new Client();
            }
        }
        return mClient;
    }

    public void execute(Runnable task) {
        executorService.execute(task);
    }

    public FutureTask<Integer> submit(Task<Integer> task) {
        executorService.submit(task);
        FutureTask futureTask = new FutureTask(task);
        taskMap.put(task.getTaskName(), futureTask);
        return futureTask;
    }

    public void cancel(String string) {
        FutureTask task = taskMap.get(string);
        if (task != null)
            task.cancel(true);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    class CustomThreadFactory implements ThreadFactory {
        final AtomicInteger mCount = new AtomicInteger(0);
        String name;

        CustomThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("(" + name + ") Im CustomThreadFactory " + mCount.incrementAndGet());
            Log.i("CustomThreadFactory", "╔════════════════════════════════════════════════════════════════════════════════════════════════");
            Log.i("CustomThreadFactory", "║" + "******************Hello Word!**********************");
            Log.i("CustomThreadFactory", "║" + "");
            Log.i("CustomThreadFactory", "║" + "What are you doing, Who are you ?");
            Log.i("CustomThreadFactory", "║" + thread.getName());
            Log.i("CustomThreadFactory", "║" + "I'm executing Task" + r.toString());
            Log.i("CustomThreadFactory", "║" + "");
            Log.i("CustomThreadFactory", "║" + "*******************Bye ^_^ ************************");
            Log.i("CustomThreadFactory", "╚════════════════════════════════════════════════════════════════════════════════════════════════");
            return thread;
        }
    }
}
