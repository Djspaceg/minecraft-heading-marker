package com.djspaceg.headingmarker;

public class Waypoint {
    public double x;
    public double y;
    public double z;
    public String dimension;
    public boolean active;

    public Waypoint(double x, double y, double z, String dimension) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.active = true;
    }
}
