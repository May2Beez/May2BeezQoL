package com.May2Beez.utils.structs;

public class Rotation {
    public float pitch;
    public float yaw;

    public Rotation(float yaw, float pitch) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float getValue() {
        return Math.abs(this.yaw) + Math.abs(this.pitch);
    }

    @Override
    public String toString() {
        return "pitch=" + pitch +
                ", yaw=" + yaw;
    }
}
