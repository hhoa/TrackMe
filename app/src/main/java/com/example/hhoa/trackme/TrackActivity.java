package com.example.hhoa.trackme;

import java.util.ArrayList;

public class TrackActivity {
    private long date;
    private double distance;
    private double speed;
    private long time;

    private ArrayList<Double[]> listLoc;

    public TrackActivity(long date, double distance, double speed, long time, ArrayList<Double[]> listLoc) {
        this.date = date;
        this.distance = distance;
        this.speed = speed;
        this.time = time;
        this.listLoc = listLoc;
    }

    public double getDistance() {
        return distance;
    }

    public double getSpeed() {
        return speed;
    }

    public long getTime() {
        return time;
    }

    public ArrayList<Double[]> getListLoc() {
        return listLoc;
    }

    public long getDate() {
        return date;
    }

    @Override
    public boolean equals(Object v) {
        long objectDate = Long.parseLong(v.toString());
        return objectDate == this.date;
    }
}
