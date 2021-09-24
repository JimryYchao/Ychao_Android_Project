package com.Ychao.PerformanceAnalysis.Threads;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThread extends ThreadPoolBase{

    public CachedThread() {
        super(Executors.newCachedThreadPool());
    }

    public void exec(Runnable task) {
        if (task != null)
            executor.execute(task);
    }

    private List<Runnable> tasks;

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


    private void addTask(Runnable task) {
        if (task != null && !tasks.contains(task)) {
            tasks.add(task);
        }
    }
}
