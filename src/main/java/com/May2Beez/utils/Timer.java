package com.May2Beez.utils;

public class Timer {
    public long lastMS = 0L;

    public Timer() {
        reset();
    }

    public boolean hasReached(long milliseconds) {
        return (System.currentTimeMillis() - this.lastMS >= milliseconds);
    }

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }
}