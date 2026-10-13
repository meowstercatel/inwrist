package com.inwrist.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.inwrist.app.data.Parcel;
import com.inwrist.app.data.ParcelRepository;
import com.inwrist.app.data.ParcelStatus;

import java.util.List;

public class MainActivity extends Activity {

    private ParcelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WearableRecyclerView list = findViewById(R.id.parcel_list);
        list.setEdgeItemsCenteringEnabled(true);
        list.setLayoutManager(new WearableLinearLayoutManager(this));
        adapter = new ParcelAdapter();
        list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private final class ParcelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_PARCEL = 1;
        private static final int TYPE_SETTINGS = 2;

        private List<Parcel> parcels() {
            return ParcelRepository.getParcels();
        }

        @Override
        public int getItemCount() {
            return parcels().size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            if (position == getItemCount() - 1) return TYPE_SETTINGS;
            return TYPE_PARCEL;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int layout = viewType == TYPE_HEADER ? R.layout.item_header : R.layout.item_chip;
            View view = inflater.inflate(layout, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View view = holder.itemView;
            switch (getItemViewType(position)) {
                case TYPE_HEADER:
                    ((TextView) view).setText(
                            getString(R.string.ready_count, ParcelRepository.readyCount()));
                    break;
                case TYPE_PARCEL:
                    bindParcel(view, parcels().get(position - 1));
                    break;
                case TYPE_SETTINGS:
                    bindChip(view, getString(R.string.settings), null, R.drawable.bg_chip_child,
                            R.color.on_surface);
                    view.setOnClickListener(v ->
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
                    break;
            }
        }

        private void bindParcel(View view, Parcel parcel) {
            boolean ready = parcel.status == ParcelStatus.READY_TO_PICKUP;
            bindChip(view, parcel.sender, parcel.status.label,
                    ready ? R.drawable.bg_chip_primary : R.drawable.bg_chip_secondary,
                    ready ? R.color.on_primary : R.color.on_surface);
            view.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ParcelDetailActivity.class);
                intent.putExtra(ParcelDetailActivity.EXTRA_PARCEL_ID, parcel.id);
                startActivity(intent);
            });
        }

        private void bindChip(View view, String label, String secondaryLabel,
                              int background, int textColor) {
            TextView labelView = view.findViewById(R.id.chip_label);
            TextView secondaryView = view.findViewById(R.id.chip_secondary_label);
            int color = getColor(textColor);

            view.setBackgroundResource(background);
            labelView.setText(label);
            labelView.setTextColor(color);
            if (secondaryLabel == null) {
                secondaryView.setVisibility(View.GONE);
            } else {
                secondaryView.setVisibility(View.VISIBLE);
                secondaryView.setText(secondaryLabel);
                secondaryView.setTextColor(color);
            }
        }
    }
}
