package com.creditclub.core.util

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class TrackGPS : Service, LocationListener {
    private val mContext: Context

    private var checkGPS = false
    private var checkNetwork = false
    internal var canGetLocation = false

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

    val geolocationString: String get() = "$latitude;$longitude"

    private var locationManager: LocationManager? = null

    private// getting GPS status
    // getting network status
    // First get location from Network Provider
    // Toast.makeText(mContext, "Network", Toast.LENGTH_SHORT).show();
    // if GPS Enabled get lat/long using GPS Services
    //Toast.makeText(mContext, "GPS", Toast.LENGTH_SHORT).show();
    val location: Location?
        get() {
            try {
                locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                checkNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10
                val MIN_TIME_BW_UPDATES: Long = 3000

                if (!checkGPS && !checkNetwork) {
                    Toast.makeText(mContext, "No Service Provider Available", Toast.LENGTH_SHORT).show()
                } else {
                    this.canGetLocation = true
                    if (checkNetwork) {
                        try {
                            locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                            )
                            Log.d("Network", "Network")
                            if (locationManager != null) {
                                loc = locationManager!!
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                            }

                            if (loc != null) {
                                latitude = loc!!.latitude
                                longitude = loc!!.longitude
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


    constructor() {
        mContext = applicationContext
        location
    }

    constructor(mContext: Context) {
        this.mContext = mContext.applicationContext
        location
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

    fun stopUsingGPS() {
        if (locationManager != null) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager!!.removeUpdates(this@TrackGPS)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }
}
