package com.inwrist.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.inwrist.app.data.ParcelRepository
import com.inwrist.app.data.ParcelStatus

class ParcelDetailActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcel_detail)

        val parcel = intent.getStringExtra(EXTRA_PARCEL_ID)?.let { ParcelRepository.getById(it) }
        val sender = findViewById<TextView>(R.id.sender)

        if (parcel == null) {
            sender.setText(R.string.parcel_not_found)
            findViewById<View>(R.id.details).visibility = View.GONE
            return
        }

        val ready = parcel.status == ParcelStatus.READY_TO_PICKUP

        sender.text = parcel.sender
        findViewById<TextView>(R.id.tracking_number).text =
            ParcelRepository.shortNumber(parcel.trackingNumber)
        findViewById<TextView>(R.id.status).text = parcel.status.label

        findViewById<View>(R.id.pickup_section).visibility = if (ready) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.pickup_code).text = parcel.pickupCode
        findViewById<TextView>(R.id.expires_at).text = getString(R.string.expires_at, parcel.expiresAt)

        findViewById<TextView>(R.id.locker).text = "${parcel.lockerName}\n${parcel.lockerAddress}"
    }

    companion object {
        const val EXTRA_PARCEL_ID = "parcel_id"
    }
}
