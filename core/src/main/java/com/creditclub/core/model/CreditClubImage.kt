package com.creditclub.core.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
class CreditClubImage(val context: Context, id: Long, name: String, path: String) :
    Image(id, name, path) {

    constructor(context: Context, image: Image) : this(context, image.id, image.name, image.path)

    val bitmap: Bitmap? by lazy { compressImage() }

    val bitmapString: String? by lazy {
        bitmap ?: return@lazy null

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.WEBP, 0, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun compressImage(): Bitmap? {
        val (tempBitmap) = safeRun {
            val inputStream = context.contentResolver.openInputStream(uri)
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