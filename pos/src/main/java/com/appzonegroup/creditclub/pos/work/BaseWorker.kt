package com.appzonegroup.creditclub.pos.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import org.koin.core.KoinComponent
import org.koin.core.inject


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/02/2020.
 * Appzone Ltd
 */
abstract class BaseWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    val posDatabase: PosDatabase by inject()
}