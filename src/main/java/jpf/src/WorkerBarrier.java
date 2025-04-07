package jpf.src;

public class WorkerBarrier {
    private final int totalWorkers;
    private int count;
    private int phase = 0;

    public WorkerBarrier(int totalWorkers) {
        this.totalWorkers = totalWorkers;
        this.count = totalWorkers;
    }

    public synchronized void await() {
        int workerPhase = phase;
        count--;

        if (count == 0) {
            // Last thread to arrive, reset the barrier
            count = totalWorkers;
            phase++;
            notifyAll();
        } else {
            // Not the last thread, wait for others
            while (phase == workerPhase) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public synchronized void reset() {
        count = totalWorkers;
        phase++;
        notifyAll();
    }
}