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
import androidx.core.app.ActivityCompat
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.*
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.android.synthetic.main.content_update.*
import kotlinx.coroutines.launch

class UpdateActivity : CreditClubActivity() {

    private var latestVersion = appDataStorage.latestVersion
    private val fileName get() = "${getString(R.string.ota_app_name)}${latestVersion?.version}.apk"
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkingState()

        primaryButton.setOnClickListener {
            if (latestVersion?.version.isNullOrEmpty()) {
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

        mainScope.launch {
            val error = (application as CreditClubApplication).getLatestVersion().error

            if (error != null) return@launch dialogProvider.showError<Nothing>(error) {
                onClose {
                    finish()
                }
            }

            latestVersion = appDataStorage.latestVersion

            if (latestVersion?.updateIsAvailable(packageInfo!!.versionName) != true) {
                val message =
                    "Congratulations. You're on the latest version of ${getString(R.string.app_name)}"
                dialogProvider.showSuccess<Nothing>(message) {
                    onClose {
                        finish()
                    }
                }
            } else availableState()
        }
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
        statusTv.text = "A new version (v${latestVersion?.version}) is available for download"
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
            val url = latestVersion?.link
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription("Downloading")
            request.setTitle("${getString(R.string.app_name)} application update")
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
            dialogProvider.showSuccess("Download has started. \nPlease check your notifications to see download progress")
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
}
