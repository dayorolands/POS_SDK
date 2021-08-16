package com.creditclub.core.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import com.creditclub.core.serializer.CreditClubImageSerializer
import com.creditclub.core.util.safeRun
import com.esafirm.imagepicker.model.Image
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 06/11/2019.
 * Appzone Ltd
 */
@Serializable(with = CreditClubImageSerializer::class)
class CreditClubImage(context: Context, private val image: Image) {

    val bitmap: Bitmap? by lazy { context.compressImage() }
    val path get() = image.path
    val id get() = image.id
    val name get() = image.name
    val uri get() = image.uri

    val bitmapString: String? by lazy {
        bitmap ?: return@lazy null

        val byteArrayOutputStream = ByteArrayOutputStream()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap?.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 10, byteArrayOutputStream)
        } else {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)
        }
        val byteArray = byteArrayOutputStream.toByteArray()

        Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun Context.compressImage(): Bitmap? {
        val (tempBitmap) = safeRun {
            val inputStream = contentResolver.openInputStream(image.uri)
            val tempBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            getResizedBitmap(tempBitmap!!)
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