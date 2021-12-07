package com.creditclub.activity

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import com.cluster.R
import com.cluster.databinding.ActivityUpdateBinding
import com.cluster.ui.dataBinding
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.api.VersionService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.ui.getLatestVersion
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.packageInfo
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class UpdateActivity : CreditClubActivity(R.layout.activity_update) {
    private val binding: ActivityUpdateBinding by dataBinding()
    private val appDataStorage: AppDataStorage by inject()
    private var latestVersion = appDataStorage.latestVersion
    private val fileName get() = "${getString(R.string.ota_app_name)}${latestVersion?.version}.apk"
    private var isProcessing = false
    private val versionService: VersionService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkingState()

        binding.primaryButton.setOnClickListener {
            if (latestVersion?.version.isNullOrEmpty()) {
                if (isProcessing) defaultState() else checkingState()
            } else {
                if (isProcessing) availableState() else updatingState()
            }
        }
    }

    private fun defaultState(status: String = "") {
        isProcessing = false
        binding.primaryButton.text = "Check for update"
        binding.statusTv.text = status
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun checkingState() {
        isProcessing = true
        binding.primaryButton.text = "Cancel"
        binding.statusTv.text = "Checking for updates..."
        binding.progressBar.visibility = View.VISIBLE

        mainScope.launch {
            val error = getLatestVersion(
                versionService = versionService,
                appDataStorage = appDataStorage,
                appConfig = appConfig,
                localStorage = localStorage,
                deviceType = Platform.deviceType,
            ).error

            if (error != null) return@launch dialogProvider.showError(error) {
                onClose {
                    finish()
                }
            }

            latestVersion = appDataStorage.latestVersion

            if (latestVersion?.isNewerThan(packageInfo!!.versionName) != true) {
                val message =
                    "Congratulations. You're on the latest version of ${getString(R.string.app_name)}"
                dialogProvider.showSuccess(message) {
                    onClose {
                        finish()
                    }
                }
            } else availableState()
        }
    }

    private fun updatingState() {
        isProcessing = true
        binding.primaryButton.text = "Cancel"
        binding.statusTv.text = "Downloading..."
        binding.progressBar.visibility = View.VISIBLE

        download()
    }

    private fun availableState() {
        isProcessing = false
        binding.primaryButton.text = "Download"
        binding.statusTv.text =
            "A new version (v${latestVersion?.version}) is available for download"
        binding.progressBar.visibility = View.INVISIBLE
    }

    fun goBack(v: View) {
        onBackPressed()
    }

    private fun download() {
        if (hasStoragePermission) {
            val url = latestVersion?.link
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription("Downloading")
            request.setTitle("${getString(R.string.app_name)} application update")
            request.setAllowedOverMetered(true)
            request.setMimeType("application/vnd.android.package-archive")
            request.setAllowedOverRoaming(true)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

            binding.statusTv.text = "Check your notifications the see download progress"

            setContentView(R.layout.layout_empty)
            val message =
                "Download has started. \nPlease check your notifications to see download progress"
            dialogProvider.showSuccess(message) {
                onClose {
                    finish()
                }
            }
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
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            download()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
