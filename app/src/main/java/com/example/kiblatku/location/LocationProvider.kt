package com.example.kiblatku.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*

class LocationProvider(context: Context) {

    private val client =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L // 5 seconds
        ).build()

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onResult: (Double, Double) -> Unit) {

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                onResult(location.latitude, location.longitude)
            }
        }

        client.requestLocationUpdates(
            locationRequest,
            callback as LocationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        callback?.let {
            client.removeLocationUpdates(it)
        }
    }
}
