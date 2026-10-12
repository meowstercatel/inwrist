package com.inwrist.app.data

data class Parcel(
    val id: String,
    val trackingNumber: String,
    val sender: String,
    val status: ParcelStatus,
    val lockerName: String,
    val lockerAddress: String,
    val pickupCode: String,
    val expiresAt: String
)
