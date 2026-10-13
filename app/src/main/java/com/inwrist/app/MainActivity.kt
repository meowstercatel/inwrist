package com.inwrist.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.inwrist.app.data.Parcel
import com.inwrist.app.data.ParcelRepository
import com.inwrist.app.data.ParcelStatus

class MainActivity : Activity() {

    private lateinit var adapter: ParcelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = ParcelAdapter()
        findViewById<WearableRecyclerView>(R.id.parcel_list).apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    private inner class ParcelAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val parcels: List<Parcel>
            get() = ParcelRepository.parcels

        override fun getItemCount(): Int = parcels.size + 2

        override fun getItemViewType(position: Int): Int = when (position) {
            0 -> TYPE_HEADER
            itemCount - 1 -> TYPE_SETTINGS
            else -> TYPE_PARCEL
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layout = if (viewType == TYPE_HEADER) R.layout.item_header else R.layout.item_chip
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            when (getItemViewType(position)) {
                TYPE_HEADER -> (view as TextView).text = getString(R.string.ready_count, ParcelRepository.readyCount())
                TYPE_PARCEL -> bindParcel(view, parcels[position - 1])
                TYPE_SETTINGS -> {
                    bindChip(view, getString(R.string.settings), null, R.drawable.bg_chip_child, R.color.on_surface)
                    view.setOnClickListener {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }
                }
            }
        }

        private fun bindParcel(view: View, parcel: Parcel) {
            val ready = parcel.status == ParcelStatus.READY_TO_PICKUP
            bindChip(
                view, parcel.sender, parcel.status.label,
                if (ready) R.drawable.bg_chip_primary else R.drawable.bg_chip_secondary,
                if (ready) R.color.on_primary else R.color.on_surface
            )
            view.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, ParcelDetailActivity::class.java)
                        .putExtra(ParcelDetailActivity.EXTRA_PARCEL_ID, parcel.id)
                )
            }
        }

        private fun bindChip(view: View, label: String, secondaryLabel: String?, background: Int, textColor: Int) {
            val labelView = view.findViewById<TextView>(R.id.chip_label)
            val secondaryView = view.findViewById<TextView>(R.id.chip_secondary_label)
            val color = getColor(textColor)

            view.setBackgroundResource(background)
            labelView.text = label
            labelView.setTextColor(color)
            if (secondaryLabel == null) {
                secondaryView.visibility = View.GONE
            } else {
                secondaryView.visibility = View.VISIBLE
                secondaryView.text = secondaryLabel
                secondaryView.setTextColor(color)
            }
        }
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_PARCEL = 1
        const val TYPE_SETTINGS = 2
    }
}
