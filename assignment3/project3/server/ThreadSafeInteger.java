package server;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeInteger {
    private AtomicInteger value;

    public ThreadSafeInteger(int initialValue) {
        this.value = new AtomicInteger(initialValue);
    }

    public int get() {
        return value.get();
    }

    public void set(int newValue) {
        value.set(newValue);
    }

    public int incrementAndGet() {
        return value.incrementAndGet();
    }

    public int decrementAndGet() {
        return value.decrementAndGet();
    }
}
