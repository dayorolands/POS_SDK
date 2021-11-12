package com.creditclub.pos.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.extension.isSuccessful
import com.appzonegroup.creditclub.pos.extension.processingCode3
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jpos.iso.ISOMsg

class CallHomeWorker(
    context: Context,
    params: WorkerParameters,
    private val socketHelper: IsoSocketHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val isoMsg = ISOMsg().apply {
            mti = "0800"
            processingCode3 = "9D0000"
        }

        val result = socketHelper.send(isoMsg)
        if (result.data?.isSuccessful == true) Result.success() else Result.failure()
    }
}