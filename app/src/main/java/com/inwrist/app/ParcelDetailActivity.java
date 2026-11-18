package com.inwrist.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inwrist.app.data.Parcel;
import com.inwrist.app.data.ParcelRepository;
import com.inwrist.app.data.ParcelStatus;

public class ParcelDetailActivity extends Activity {

    public static final String EXTRA_PARCEL_ID = "parcel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_detail);

        String parcelId = getIntent().getStringExtra(EXTRA_PARCEL_ID);
        Parcel parcel = parcelId == null ? null : ParcelRepository.getById(parcelId);
        TextView sender = findViewById(R.id.sender);

        if (parcel == null) {
            sender.setText(R.string.parcel_not_found);
            findViewById(R.id.details).setVisibility(View.GONE);
            return;
        }

        boolean ready = parcel.status == ParcelStatus.READY_TO_PICKUP;

        sender.setText(parcel.sender);
        this.<TextView>findViewById(R.id.tracking_number)
                .setText(ParcelRepository.shortNumber(parcel.trackingNumber));
        this.<TextView>findViewById(R.id.status).setText(parcel.status.label);

        findViewById(R.id.pickup_section).setVisibility(ready ? View.VISIBLE : View.GONE);
        this.<TextView>findViewById(R.id.pickup_code).setText(parcel.pickupCode);
        this.<TextView>findViewById(R.id.expires_at)
                .setText(getString(R.string.expires_at, parcel.expiresAt));

        this.<TextView>findViewById(R.id.locker)
                .setText(parcel.lockerName + "\n" + parcel.lockerAddress);
    }
}
