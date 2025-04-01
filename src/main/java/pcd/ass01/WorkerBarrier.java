package pcd.ass01;

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
            // Ultimo thread arrivato alla barriera
            count = totalWorkers;
            phase++;
            notifyAll();
        } else {
            // Non è l'ultimo thread, attende che tutti arrivino
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
}