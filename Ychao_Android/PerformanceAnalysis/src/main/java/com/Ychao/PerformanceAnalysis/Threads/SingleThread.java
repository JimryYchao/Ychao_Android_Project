package com.Ychao.PerformanceAnalysis.Threads;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThread extends ThreadPoolBase {

    public SingleThread(){
        super(Executors.newSingleThreadExecutor());
    }

    public void exec(Runnable task){

    }
    public void exec(List<Runnable> tasks) {
        tasks.clear();

        for (int i = 0; i < tasks.size(); i++) {
            addTask(tasks.get(i));
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Runnable _task : tasks) {
                    _task.run();
                }
            }
        };

        exec(r);
    }


    private List<Runnable> tasks;
    private void addTask(Runnable task) {
        if (task != null && !tasks.contains(task)) {
            tasks.add(task);
        }
    }

}
