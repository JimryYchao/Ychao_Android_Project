package com.Ychao.PerformanceAnalysis.Threads;

import java.util.concurrent.ExecutorService;

public abstract class ThreadPoolBase {

    protected ExecutorService executor;

    protected ThreadPoolBase(ExecutorService executor){
        this.executor = executor;
    }

    public abstract void exec(Runnable task);

    public boolean isActive(){
        if (executor == null) {
            return false;
        } else if(executor.isTerminated()) {
            return  false;
        }else  if (executor.isShutdown())
            return false;
        return true;
    }
    public void terminateThreadPool() {
        executor.shutdown();
    }
}
