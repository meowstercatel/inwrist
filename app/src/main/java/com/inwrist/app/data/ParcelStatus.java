package com.inwrist.app.data;

public enum ParcelStatus {
    IN_TRANSIT("W drodze"),
    READY_TO_PICKUP("Gotowa do odbioru"),
    PICKED_UP("Odebrana");

    public final String label;

    ParcelStatus(String label) {
        this.label = label;
    }
}
