package com.creditclub.core.util

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.creditclub.core.data.prefs.LocalStorage
import org.koin.core.KoinComponent
import org.koin.core.inject

class TrackGPS(mContext: Context) : LocationListener, KoinComponent {
    private val localStorage: LocalStorage by inject()
    private val mContext: Context = mContext.applicationContext

    private var checkGPS = false
    private var checkNetwork = false
    internal var canGetLocation = false

    private var loc: Location? = null

    init {
        location
    }

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

    private val location: Location?
        get() {
            try {
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
                        try {
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
                        } catch (e: SecurityException) {

                        }

                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return loc
        }

    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("GPS Not Enabled")
        alertDialog.setMessage("Do you wants to turn On GPS")

        alertDialog.setPositiveButton("Yes") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }

        alertDialog.setNegativeButton("No") { dialog, which -> dialog.cancel() }

        alertDialog.show()
    }

    override fun onLocationChanged(location: Location?) {
        loc = location ?: loc
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    companion object {
        const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10
        const val MIN_TIME_BW_UPDATES: Long = 3000
    }
}
