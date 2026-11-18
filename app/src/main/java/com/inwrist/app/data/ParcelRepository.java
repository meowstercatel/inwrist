package com.inwrist.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ParcelRepository {

    public enum State {
        IDLE, LOADING, LOADED, LOGGED_OUT, ERROR
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static List<Parcel> parcels = new ArrayList<>();
    private static State state = State.IDLE;

    private ParcelRepository() {
    }

    public static void refresh(Context context, Runnable onDone) {
        TokenStore tokens = new TokenStore(context);
        if (!tokens.isLoggedIn()) {
            parcels = new ArrayList<>();
            state = State.LOGGED_OUT;
            onDone.run();
            return;
        }
        state = State.LOADING;
        executor.execute(() -> {
            List<Parcel> result = null;
            State next;
            try {
                result = new InPostClient(tokens).fetchTrackedParcels();
                next = State.LOADED;
            } catch (InPostClient.AuthException e) {
                next = State.LOGGED_OUT;
            } catch (IOException e) {
                next = State.ERROR;
            }
            List<Parcel> fetched = result;
            State finalState = next;
            mainHandler.post(() -> {
                if (fetched != null) parcels = fetched;
                if (finalState == State.LOGGED_OUT) parcels = new ArrayList<>();
                state = finalState;
                onDone.run();
            });
        });
    }

    public static State getState() {
        return state;
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

    public static String shortNumber(String trackingNumber) {
        if (trackingNumber.length() <= 8) return trackingNumber;
        return trackingNumber.substring(0, 4) + "…"
                + trackingNumber.substring(trackingNumber.length() - 4);
    }
}
