package com.i54m.betterchatcolors.managers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkerManager implements Manager {

    @Getter
    private static final WorkerManager INSTANCE = new WorkerManager();
    private final ArrayList<Worker> workers = new ArrayList<>();
    private boolean locked = true;
    private Thread mainThread;

    @Override
    public synchronized void start() {
        if (!locked) {
            PLUGIN.getLogger().warning("Worker Manager Already started!");
            return;
        }
        mainThread = Thread.currentThread();
        locked = false;
        PLUGIN.getLogger().info("Started Worker Manager!");
    }

    @Override
    public boolean isStarted() {
        return !locked;
    }

    @Override
    public synchronized void stop() {
        if (locked) {
            PLUGIN.getLogger().warning("Worker Manager Not started!");
            return;
        }
        locked = true;
        try {
            if (!workers.isEmpty()) {
                PLUGIN.getLogger().info("Pausing main thread while workers finish up!");
                mainThread.wait();
                wait();
            }
        } catch (InterruptedException e) {
            PLUGIN.getLogger().severe("Error: main thread was interrupted while waiting for workers to finish!");
            PLUGIN.getLogger().severe("Interrupting workers, this may cause data loss!!");
            for (Worker worker : workers) {
                PLUGIN.getLogger().severe("Interrupting " + worker.getName());
                worker.interrupt();
            }
        }
        workers.clear();
    }

    public synchronized void runWorker(Worker worker) {
        if (locked) {
            PLUGIN.getLogger().warning("Worker Manager Not started!");
            return;
        }
        workers.add(worker);
        worker.setName("Better-ChatColors - Worker Thread #" + (workers.indexOf(worker) + 1));
        worker.start();
    }

    private synchronized void finishedWorker(Worker worker) {
        if (worker.getStatus() == Worker.Status.FINISHED)
            workers.remove(worker);
        if (locked && workers.isEmpty())
            mainThread.notifyAll();
    }


    public static class Worker extends Thread {

        private final Runnable runnable;
        private Status status;

        public Worker(Runnable runnable) {
            this.runnable = runnable;
            this.status = Status.CREATED;
        }

        @Override
        public void run() {
            status = Status.WORKING;
            try {
                runnable.run();
            } catch (Exception e) {
                PLUGIN.getLogger().warning("Worker " + this.getName() + " encountered an exception: " + e.getMessage());
                e.printStackTrace();
            }
            status = Status.FINISHED;
            WorkerManager.getINSTANCE().finishedWorker(this);
        }

        public Status getStatus() {
            return this.status;
        }


        public enum Status {
            CREATED, WORKING, FINISHED
        }
    }

}
