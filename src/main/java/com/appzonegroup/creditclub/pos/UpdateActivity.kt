package com.appzonegroup.creditclub.pos

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.databinding.ActivityUpdateBinding
import kotlinx.android.synthetic.main.activity_update.*
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log


class UpdateActivity : PosActivity() {
    private val prefs by lazy { getSharedPreferences(PREFS_FILENAME, 0) }
    private var latestVersion: String = ""
        get() {
            if (field.isEmpty()) field = prefs.getString(
                "LATEST_VERSION",
                "${BuildConfig.API_HOST}/CreditClubClient/HttpJavaClient/FastTrack/FastTrack-v3.apk"
            ) ?: ""
            return field
        }
        set(value) {
            prefs.edit().putString("LATEST_VERSION", value).apply()
            field = value
        }
    private val fileName get() = latestVersion.split("/").last()

    private var latestVersionIsDownloaded: String = ""
        get() {
            if (field.isEmpty()) field = prefs.getString("LATEST_VERSION_IS_DOWNLOADED", "") ?: ""
            return field
        }
        set(value) {
            prefs.edit().putString("LATEST_VERSION_IS_DOWNLOADED", value).apply()
            field = value
        }

    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityUpdateBinding>(this, R.layout.activity_update)
        defaultState()
        if (latestVersion.isNotEmpty()) availableState()
        else checkingState()

        update_primary_button.setOnClickListener {
            if (latestVersion.isEmpty()) {
                if (isProcessing) defaultState() else checkingState()
            } else {
                if (isProcessing) availableState() else updatingState()
            }
        }
    }

    private fun defaultState() {
        isProcessing = false
        update_primary_button.text = "Check for update"
        update_primary_message.text = ""
        update_primary_progress.visibility = View.INVISIBLE
    }

    private fun checkingState() {
        isProcessing = true
        update_primary_button.text = "Cancel"
        update_primary_message.text = "Checking for updates..."
        update_primary_progress.visibility = View.VISIBLE
    }

    private fun updatingState() {
        isProcessing = true
        update_primary_button.text = "Cancel"
        update_primary_message.text = "Downloading..."
        update_primary_progress.visibility = View.VISIBLE

        download()
    }

    private fun availableState() {
        isProcessing = false
        update_primary_button.text = "Download"
        update_primary_message.text = "Update to new version"
        update_primary_progress.visibility = View.INVISIBLE
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
            val url = latestVersion
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

            showSuccess("Check your notifications the see download progress") {
                onClose {
                    finish()
                }
            }
            setContentView(R.layout.empty)
//            }
        }
    }

    private val hasStoragePermission: Boolean
        get() = if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error", "You have permission")
                true
            } else {
                Log.e("Permission error", "You have asked for permission")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else {
            Log.e("Permission error", "You already have the permission")
            true
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
