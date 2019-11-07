package com.creditclub.core.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.creditclub.core.BuildConfig
import com.creditclub.core.util.safeRun
import com.esafirm.imagepicker.model.Image
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 06/11/2019.
 * Appzone Ltd
 */
class CreditClubImage(id: Long, name: String, path: String) : Image(id, name, path) {

    constructor(image: Image) : this(image.id, image.name, image.path)

    val bitmap: Bitmap? by lazy { compressImage() }

    val bitmapString: String? by lazy {
        bitmap ?: return@lazy null

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun compressImage(): Bitmap? {

        val (tempBitmap, error) = safeRun {
            val tempBitmap = BitmapFactory.decodeFile(path)
            val newBitmap = getResizedBitmap(tempBitmap!!)

            val file = File(path)
            val fOut = FileOutputStream(file)
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut)
            fOut.flush()
            fOut.close()

            newBitmap
        }

        if (error != null && BuildConfig.DEBUG) {
            error.printStackTrace()
            Log.e("Image", "Save file error!")
        }

        return tempBitmap
    }

    private fun getResizedBitmap(image: Bitmap): Bitmap {
        var width = image.width
        var height = image.height
        val maxSize = 400

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 0) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}