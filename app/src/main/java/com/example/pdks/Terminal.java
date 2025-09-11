package com.example.pdks;

public class Terminal {
    private final int imageResId;
    private final String name;
    private final String coordinate;
    private final String distance;

    public Terminal(int imageResId, String name, String coordinate, String distance) {
        this.imageResId = imageResId;
        this.name = name;
        this.coordinate = coordinate;
        this.distance = distance;
    }

    public int getImageResId() { return imageResId; }
    public String getName() { return name; }
    public String getCoordinate() { return coordinate; }
    public String getDistance() { return distance; }
}
