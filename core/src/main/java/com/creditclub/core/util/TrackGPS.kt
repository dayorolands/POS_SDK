package com.creditclub.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.creditclub.core.data.prefs.LocalStorage
import org.koin.core.KoinComponent
import org.koin.core.inject

class TrackGPS(private val mContext: Context) : LocationListener, KoinComponent {
    private val localStorage: LocalStorage by inject()

    private var checkGPS = false
    private var checkNetwork = false
    private var canGetLocation = false

    private var loc: Location? = null

    var latitude: Double = 0.toDouble()
        get() {
            if (loc != null) {
                field = loc!!.latitude
            }
            return field
        }

    var longitude: Double = 0.toDouble()
        get() {
            if (loc != null) {
                field = loc!!.longitude
            }
            return field
        }

    val geolocationString: String?
        get() = if (loc == null) localStorage.lastKnownLocation
        else "$latitude;$longitude"

    private var locationManager: LocationManager? = null

    override fun onLocationChanged(location: Location) {
        loc = location
        localStorage.lastKnownLocation = "${location.latitude};${location.longitude}"
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    init {
        safeRun {
            locationManager =
                mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            checkNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!checkGPS && !checkNetwork) {
                Toast.makeText(mContext, "No Service Provider Available", Toast.LENGTH_SHORT)
                    .show()
            } else {
                this.canGetLocation = true
                if (checkNetwork) {
                    try {
                        locationManager?.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        Log.d("Network", "Network")
                        loc =
                            locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                ?: loc

                        if (loc != null) {
                            latitude = loc!!.latitude
                            longitude = loc!!.longitude
                            localStorage.lastKnownLocation =
                                "${loc?.latitude};${loc?.longitude}"
                        }
                    } catch (e: SecurityException) {

                    }
                }
            }

            if (checkGPS) {
                if (loc == null) {
                    Looper.myLooper() ?: Looper.prepare()
                    if (ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@safeRun
                    }
                    locationManager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    Log.d("GPS Enabled", "GPS Enabled")
                    if (locationManager != null) {
                        loc = locationManager!!
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (loc != null) {
                            latitude = loc!!.latitude
                            longitude = loc!!.longitude
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10
        const val MIN_TIME_BW_UPDATES: Long = 3000
    }
}
