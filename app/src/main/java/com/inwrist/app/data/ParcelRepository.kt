package com.inwrist.app.data

object ParcelRepository {

    private val items = mutableListOf(
        Parcel(
            "1",
            "520113014230722029585646",
            "Allegro",
            ParcelStatus.READY_TO_PICKUP,
            "WAW123M",
            "ul. Marszałkowska 10, Warszawa",
            "482913",
            "05.11, 14:30"
        ),
        Parcel(
            "2",
            "630220114230722011223344",
            "Zalando",
            ParcelStatus.IN_TRANSIT,
            "WAW45A",
            "ul. Puławska 145, Warszawa",
            "------",
            "-"
        ),
        Parcel(
            "3",
            "610330114230722099887766",
            "Empik",
            ParcelStatus.PICKED_UP,
            "KRA01N",
            "ul. Floriańska 3, Kraków",
            "------",
            "-"
        )
    )

    val parcels: List<Parcel>
        get() = items

    fun getById(id: String): Parcel? = items.firstOrNull { it.id == id }

    fun readyCount(): Int = items.count { it.status == ParcelStatus.READY_TO_PICKUP }

    fun openLocker(id: String): Boolean {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0 || items[index].status != ParcelStatus.READY_TO_PICKUP) return false
        items[index] = items[index].copy(status = ParcelStatus.PICKED_UP)
        return true
    }

    fun shortNumber(trackingNumber: String): String {
        if (trackingNumber.length <= 8) return trackingNumber
        return trackingNumber.take(4) + "…" + trackingNumber.takeLast(4)
    }
}
