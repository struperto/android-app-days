package com.struperto.androidappdays.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import java.time.Instant

data class LocationContextState(
    val isNearTarget: Boolean,
    val distanceMeters: Int?,
    val lastCheckedAt: Instant,
)

interface LocationContextRepository {
    suspend fun checkProximity(targetLat: Double, targetLon: Double, radiusMeters: Float = 200f): LocationContextState
}

class DeviceLocationContextRepository(
    private val context: Context,
) : LocationContextRepository {
    @SuppressLint("MissingPermission")
    override suspend fun checkProximity(
        targetLat: Double,
        targetLon: Double,
        radiusMeters: Float,
    ): LocationContextState {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return LocationContextState(isNearTarget = false, distanceMeters = null, lastCheckedAt = Instant.now())
        val lastKnown = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (lastKnown == null) {
            return LocationContextState(isNearTarget = false, distanceMeters = null, lastCheckedAt = Instant.now())
        }
        val target = Location("target").apply {
            latitude = targetLat
            longitude = targetLon
        }
        val distance = lastKnown.distanceTo(target)
        return LocationContextState(
            isNearTarget = distance <= radiusMeters,
            distanceMeters = distance.toInt(),
            lastCheckedAt = Instant.now(),
        )
    }
}
