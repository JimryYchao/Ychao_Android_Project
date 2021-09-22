package com.Ychao.PerformanceAnalysis.Threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThread {


    ExecutorService singleThreadExecutor;
    public SingleThread(){
        if (singleThreadExecutor == null){
            singleThreadExecutor = Executors.newSingleThreadExecutor();
        }
    }






}
