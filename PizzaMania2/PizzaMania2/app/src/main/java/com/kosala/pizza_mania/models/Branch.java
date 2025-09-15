package com.kosala.pizza_mania.models;

public class Branch {
    private String id;   // 🔹 Add this for Firestore doc ID
    private String name;
    private double lat;
    private double lng;

    public Branch() { }

    public Branch(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    // id getter & setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // name getter & setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // lat getter & setter
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    // lng getter & setter
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
}
