package pcd.ass01;

public class SyncWorkersMonitor {
    private final int totalThreads;
    private int finishedCount = 0;

    public SyncWorkersMonitor(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    public synchronized void workDoneWaitCoordinator() {
        try {
            finishedCount++;
            if (finishedCount == totalThreads) {
                notifyAll();
            }

            while (finishedCount != 0) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void waitWorkers() {
        try {
            while (finishedCount < totalThreads) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void coordinatorDone() {
        finishedCount = 0;
        notifyAll();
    }
}