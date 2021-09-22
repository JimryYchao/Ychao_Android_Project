package com.Ychao.PerformanceAnalysis.Threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledThread {

    private ScheduledExecutorService scheduledThreadPool;

    public ScheduledThread() {
        if (scheduledThreadPool == null) {
            scheduledThreadPool = Executors.newScheduledThreadPool(3);
        }
    }

    public boolean isThreadStart() {
        if (scheduledThreadPool == null) {
            return false;
        } else if (scheduledThreadPool.isShutdown()||scheduledThreadPool.isTerminated()) {
            return  false;
        }
        return true;
    }

    public void execFixedRate(Runnable r, int initialDelay, int period, TimeUnit timeUnit) {
        scheduledThreadPool.scheduleAtFixedRate(r, initialDelay, period, timeUnit);
    }

    public boolean terminateThreadPool() {
        scheduledThreadPool.shutdown();

        if (scheduledThreadPool == null) {
            return false;
        }

        return scheduledThreadPool.isShutdown();
    }



}
