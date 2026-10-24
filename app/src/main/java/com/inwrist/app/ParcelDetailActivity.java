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

    private String parcelId;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_detail);

        parcelId = getIntent().getStringExtra(EXTRA_PARCEL_ID);
        findViewById(R.id.open_locker).setOnClickListener(v -> {
            message = getString(ParcelRepository.openLocker(parcelId)
                    ? R.string.locker_opened
                    : R.string.locker_open_failed);
            render();
        });
        render();
    }

    private void render() {
        Parcel parcel = parcelId == null ? null : ParcelRepository.getById(parcelId);
        TextView sender = findViewById(R.id.sender);

        if (parcel == null) {
            sender.setText(R.string.parcel_not_found);
            findViewById(R.id.details).setVisibility(View.GONE);
            return;
        }

        boolean ready = parcel.status == ParcelStatus.READY_TO_PICKUP;
        int readyVisibility = ready ? View.VISIBLE : View.GONE;

        sender.setText(parcel.sender);
        this.<TextView>findViewById(R.id.tracking_number)
                .setText(ParcelRepository.shortNumber(parcel.trackingNumber));
        this.<TextView>findViewById(R.id.status).setText(parcel.status.label);

        findViewById(R.id.pickup_section).setVisibility(readyVisibility);
        this.<TextView>findViewById(R.id.pickup_code).setText(parcel.pickupCode);
        this.<TextView>findViewById(R.id.expires_at)
                .setText(getString(R.string.expires_at, parcel.expiresAt));

        this.<TextView>findViewById(R.id.locker)
                .setText(parcel.lockerName + "\n" + parcel.lockerAddress);

        findViewById(R.id.open_locker).setVisibility(readyVisibility);

        TextView messageView = findViewById(R.id.message);
        messageView.setVisibility(message == null ? View.GONE : View.VISIBLE);
        messageView.setText(message);
    }
}
