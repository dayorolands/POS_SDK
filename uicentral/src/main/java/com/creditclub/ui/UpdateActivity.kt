package com.creditclub.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.util.appDataStorage
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.android.synthetic.main.content_update.*
import org.koin.android.ext.android.inject

class UpdateActivity : AppCompatActivity() {

    private val middleWareAPI: CreditClubMiddleWareAPI by inject()
    private val fileName by lazy {
        "${getString(R.string.version_management_app_name)}${appDataStorage.latestVersion}.apk"
    }

    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        setSupportActionBar(toolbar)

        defaultState()

        if (!appDataStorage.latestVersion.isNullOrEmpty()) availableState()
        else checkingState()

        primaryButton.setOnClickListener {
            if (appDataStorage.latestVersion.isNullOrEmpty()) {
                if (isProcessing) defaultState() else checkingState()
            } else {
                if (isProcessing) availableState() else updatingState()
            }
        }
    }

    private fun defaultState(status: String = "") {
        isProcessing = false
        primaryButton.text = "Check for update"
        statusTv.text = status
        progressBar.visibility = View.INVISIBLE
    }

    private fun checkingState() {
        isProcessing = true
        primaryButton.text = "Cancel"
        statusTv.text = "Checking for updates..."
        progressBar.visibility = View.VISIBLE
    }

    private fun updatingState() {
        isProcessing = true
        primaryButton.text = "Cancel"
        statusTv.text = "Downloading..."
        progressBar.visibility = View.VISIBLE

        download()
    }

    private fun availableState() {
        isProcessing = false
        primaryButton.text = "Download"
        statusTv.text = "Update to new version"
        progressBar.visibility = View.INVISIBLE
    }

    fun goBack(v: View) {
        onBackPressed()
    }

    private fun download() {
        if (hasStoragePermission) {
//            val file = File(Environment.DIRECTORY_DOWNLOADS, fileName)
////            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            if (file.exists()) {
//                val intent = Intent(Intent.ACTION_VIEW)
//                intent.setDataAndType(
//                    Uri.fromFile(file),
//                    "application/vnd.android.package-archive"
//                )
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//            } else {
            val url = appDataStorage.latestVersionLink
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription("Downloading")
            request.setTitle("POS application update")
            request.allowScanningByMediaScanner()
//            request.setRequiresCharging(false)// Set if charging is required to begin the download
            request.setAllowedOverMetered(true)
            request.setMimeType("application/vnd.android.package-archive")
            request.setAllowedOverRoaming(true)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            statusTv.text = "Check your notifications the see download progress"

            setContentView(R.layout.layout_empty)
//            }
        }
    }

    private val hasStoragePermission: Boolean
        get() = if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                Log.e("Permission error", "You have asked for permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            Log.e("Permission error", "You already have the permission")
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            download()
        }
    }

    companion object {
        //        private val TAG = UpdateActivity::class.java.simpleName
        internal const val PREFS_FILENAME = "Updates"
    }
}
