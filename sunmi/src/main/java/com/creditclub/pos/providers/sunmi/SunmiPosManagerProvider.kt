package com.creditclub.pos.providers.sunmi

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import com.appzonegroup.creditclub.pos.provider.sunmi.SunmiPosManager
import com.creditclub.pos.PosProviders

class SunmiPosManagerProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        PosProviders.registerFirst(context!!, SunmiPosManager)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun attachInfo(
        context: Context,
        providerInfo: ProviderInfo?
    ) {
        if (providerInfo == null) {
            throw NullPointerException("SunmiPosManagerProvider ProviderInfo cannot be null.")
        }
        val packageName = "com.creditclub.pos.providers.sunmi"
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        check("$packageName.SunmiPosManagerProvider" != providerInfo.authority) {
            ("Incorrect provider authority in manifest. Most likely due to a "
                    + "missing applicationId variable in application\'s build.gradle.")
        }
        super.attachInfo(context, providerInfo)
    }
}