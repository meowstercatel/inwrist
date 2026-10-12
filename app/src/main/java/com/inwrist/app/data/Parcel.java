package com.inwrist.app.data;

public final class Parcel {
    public final String id;
    public final String trackingNumber;
    public final String sender;
    public final ParcelStatus status;
    public final String lockerName;
    public final String lockerAddress;
    public final String pickupCode;
    public final String expiresAt;

    public Parcel(String id, String trackingNumber, String sender, ParcelStatus status,
                  String lockerName, String lockerAddress, String pickupCode, String expiresAt) {
        this.id = id;
        this.trackingNumber = trackingNumber;
        this.sender = sender;
        this.status = status;
        this.lockerName = lockerName;
        this.lockerAddress = lockerAddress;
        this.pickupCode = pickupCode;
        this.expiresAt = expiresAt;
    }

    public Parcel withStatus(ParcelStatus newStatus) {
        return new Parcel(id, trackingNumber, sender, newStatus,
                lockerName, lockerAddress, pickupCode, expiresAt);
    }
}
