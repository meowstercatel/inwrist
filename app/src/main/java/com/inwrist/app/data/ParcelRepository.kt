package com.inwrist.app.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.util.concurrent.Executors

object ParcelRepository {

    enum class State {
        IDLE, LOADING, LOADED, LOGGED_OUT, ERROR
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    var parcels: List<Parcel> = emptyList()
        private set

    var state: State = State.IDLE
        private set

    fun refresh(context: Context, onDone: () -> Unit) {
        val tokens = TokenStore(context)
        if (!tokens.isLoggedIn) {
            parcels = emptyList()
            state = State.LOGGED_OUT
            onDone()
            return
        }
        state = State.LOADING
        executor.execute {
            var fetched: List<Parcel>? = null
            val next = try {
                fetched = InPostClient(tokens).fetchTrackedParcels()
                State.LOADED
            } catch (e: InPostClient.AuthException) {
                State.LOGGED_OUT
            } catch (e: IOException) {
                State.ERROR
            }
            mainHandler.post {
                if (fetched != null) parcels = fetched
                if (next == State.LOGGED_OUT) parcels = emptyList()
                state = next
                onDone()
            }
        }
    }

    fun getById(id: String): Parcel? = parcels.firstOrNull { it.id == id }

    fun readyCount(): Int = parcels.count { it.status == ParcelStatus.READY_TO_PICKUP }

    fun shortNumber(trackingNumber: String): String {
        if (trackingNumber.length <= 8) return trackingNumber
        return trackingNumber.take(4) + "…" + trackingNumber.takeLast(4)
    }
}
