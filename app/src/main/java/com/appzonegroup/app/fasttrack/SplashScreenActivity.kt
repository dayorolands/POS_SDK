package com.appzonegroup.app.fasttrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.localStorage
import com.google.firebase.crashlytics.FirebaseCrashlytics

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }

        val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        firebaseCrashlytics.setUserId(localStorage.agent?.agentCode ?: "guest")
        firebaseCrashlytics.setCustomKey(
            "agent_institution",
            localStorage.institutionCode ?: "none"
        )
        firebaseCrashlytics.setCustomKey("environment", BuildConfig.API_HOST)
        setContentView(R.layout.activity_splashscreen)
        setPolicy()

        checkPermissions()
    }

    private fun loadPage() {
        object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                    if (LocalStorage.GetValueFor(
                            AppConstants.ACTIVATED,
                            baseContext
                        ) == null
                    ) {
                        val intent =
                            Intent(this@SplashScreenActivity, AgentActivationActivity::class.java)
                        startActivity(intent)
                    } else {
                        startService(
                            Intent(
                                this@SplashScreenActivity,
                                LocationChangedService::class.java
                            )
                        )
                        val intent =
                            Intent(this@SplashScreenActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    finish()
                } catch (ex: InterruptedException) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                }
            }
        }.start()
    }

    private fun setPolicy() {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //builder.detectFileUriExposure();
            builder.detectAll()
        }
    }

    private fun checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ), MY_PERMISSIONS_REQUEST
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            loadPage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPage()
            } else {
                Dialogs.getAlertDialog(
                    this,
                    "The app cannot function without the permissions approved."
                ).show()
                //finish();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST = 200
    }
}