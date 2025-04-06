package pcd.ass01;

public class Latch {
    private int count;
    private final Object lock = new Object();


    public Latch(int count) {
        this.count = count;
    }

    public void countDown() {
        synchronized (lock) {
            if (count > 0) {
                count--;
                if (count == 0) {

                    lock.notifyAll();
                }
            }
        }
    }

    public void await() throws InterruptedException {
        synchronized (lock) {
            while (count > 0) {
                lock.wait();
            }
        }
    }

    public void reset(int count) {
        synchronized (lock) {
            this.count = count;
        }
    }

    /**
     * Returns the current count.
     *
     * @return the current count
     */
    public int getCount() {
        synchronized (lock) {
            return count;
        }
    }
}