package com.inwrist.app.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParcelRepository {

    private static final List<Parcel> parcels = new ArrayList<>();

    static {
        parcels.add(new Parcel(
                "1",
                "520113014230722029585646",
                "Allegro",
                ParcelStatus.READY_TO_PICKUP,
                "WAW123M",
                "ul. Marszałkowska 10, Warszawa",
                "482913",
                "05.11, 14:30"));
        parcels.add(new Parcel(
                "2",
                "630220114230722011223344",
                "Zalando",
                ParcelStatus.IN_TRANSIT,
                "WAW45A",
                "ul. Puławska 145, Warszawa",
                "------",
                "-"));
        parcels.add(new Parcel(
                "3",
                "610330114230722099887766",
                "Empik",
                ParcelStatus.PICKED_UP,
                "KRA01N",
                "ul. Floriańska 3, Kraków",
                "------",
                "-"));
    }

    private ParcelRepository() {
    }

    public static List<Parcel> getParcels() {
        return Collections.unmodifiableList(parcels);
    }

    public static Parcel getById(String id) {
        for (Parcel parcel : parcels) {
            if (parcel.id.equals(id)) return parcel;
        }
        return null;
    }

    public static int readyCount() {
        int count = 0;
        for (Parcel parcel : parcels) {
            if (parcel.status == ParcelStatus.READY_TO_PICKUP) count++;
        }
        return count;
    }

    public static boolean openLocker(String id) {
        for (int i = 0; i < parcels.size(); i++) {
            Parcel parcel = parcels.get(i);
            if (!parcel.id.equals(id)) continue;
            if (parcel.status != ParcelStatus.READY_TO_PICKUP) return false;
            parcels.set(i, parcel.withStatus(ParcelStatus.PICKED_UP));
            return true;
        }
        return false;
    }

    public static String shortNumber(String trackingNumber) {
        if (trackingNumber.length() <= 8) return trackingNumber;
        return trackingNumber.substring(0, 4) + "…"
                + trackingNumber.substring(trackingNumber.length() - 4);
    }
}
