package com.cluster.core.config

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager

suspend fun requestBluetoothPermissions(activity: AppCompatActivity): PermissionResult {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PermissionManager.requestPermissions(
            activity = activity,
            requestId = 2000,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        PermissionManager.requestPermissions(
            activity = activity,
            requestId = 1000,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }
}

fun PermissionResult.getBluetoothMessage() = when (this) {
    is PermissionResult.PermissionGranted -> {
        "Bluetooth access granted"
    }
    is PermissionResult.PermissionDenied -> {
        "Bluetooth access is required to use mPOS"
    }
    is PermissionResult.PermissionDeniedPermanently -> {
        "Bluetooth access is required to use mPOS.\n" +
                "Please manually go to settings and enable permission(s)"

    }
    is PermissionResult.ShowRational -> {
        "Bluetooth access is required to use mPOS"
    }
}