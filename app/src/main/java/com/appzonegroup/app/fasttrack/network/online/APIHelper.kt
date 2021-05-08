package com.appzonegroup.app.fasttrack.network.online

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.appzonegroup.app.fasttrack.model.TransactionCountType
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.data.CreditClubClient
import com.creditclub.core.data.Encryption
import com.creditclub.core.data.api.BankOneService
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.SafeRunResult
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.core.context.GlobalContext
import java.io.File
import java.util.concurrent.TimeoutException

class APIHelper @JvmOverloads constructor(
    private val ctx: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val koin = GlobalContext.get().koin
    private val localStorage: LocalStorage by koin.inject()
    private val client: CreditClubClient by koin.inject()

    fun interface VolleyCallback<T> {
        fun onCompleted(e: Exception?, result: T?, status: Boolean)
    }

    fun attemptValidation(
        pNumber: String,
        sessionId: String,
        activationCode: String,
        location: String,
        state: Boolean,
        callback: VolleyCallback<String>
    ) {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val url = BankOneService.UrlGenerator.operationInit(
            pNumber,
            sessionId,
            activationCode,
            location,
            state,
            localStorage.institutionCode
        )

        handleRequest(url) { (response, error) ->
            callback.onCompleted(error, response, error == null)
        }
    }

    fun attemptActivation(
        pNumber: String,
        sessionId: String,
        activationCode: String,
        location: String,
        state: Boolean,
        callback: VolleyCallback<String>
    ) {

        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val url = BankOneService.UrlGenerator.operationActivation(
            pNumber,
            sessionId,
            activationCode,
            location,
            state,
            localStorage.institutionCode
        )

        handleRequest(url) { (response, error) ->
            callback.onCompleted(error, response, error == null)
        }
    }

    fun getNextOperation(
        pNumber: String,
        sessionId: String,
        next: String,
        location: String,
        callback: VolleyCallback<String>
    ) {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)

        val url = BankOneService.UrlGenerator.operationNext(
            pNumber,
            sessionId,
            next,
            location,
            localStorage.institutionCode
        )

        handleRequest(url) { (response, error) ->
            if (response == null)
                Misc.increaseTransactionMonitorCounter(
                    ctx,
                    TransactionCountType.ERROR_RESPONSE_COUNT,
                    sessionId
                )
            if (error != null && error.cause is TimeoutException) {
                Misc.increaseTransactionMonitorCounter(
                    ctx,
                    TransactionCountType.NO_RESPONSE_COUNT,
                    sessionId
                )
            }
            callback.onCompleted(error, response, error == null)
        }
    }

    suspend fun getNextOperationImage(
        pNumber: String,
        sessionId: String,
        image: File,
        location: String,
        isFullImage: Boolean,
    ): SafeRunResult<String?> {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)

        val mimeTypeMap = MimeTypeMap.getSingleton()
        val mimeType = mimeTypeMap.getMimeTypeFromExtension(
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(image).toString())
        )
        val requestFile: RequestBody = image
            .asRequestBody((mimeType ?: "image/jpeg").toMediaTypeOrNull())

        val body =
            MultipartBody.Part.createFormData("file", image.name, requestFile)

        return safeRunIO {
            client.bankOneService.operationNextImage(
                pNumber,
                sessionId,
                Encryption.encrypt(location),
                Encryption.encrypt(localStorage.institutionCode),
                isFullImage,
                body
            )
        }
    }

    fun continueNextOperation(
        pNumber: String,
        sessionId: String?,
        next: String,
        location: String,
        callback: VolleyCallback<String>
    ) {
        //Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId);
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val url = BankOneService.UrlGenerator.operationContinue(
            pNumber,
            sessionId ?: "nothing",
            next,
            location,
            localStorage.institutionCode
        )
        handleRequest(url) { (response, error) ->
            if (response == null) {
                Misc.increaseTransactionMonitorCounter(
                    ctx,
                    TransactionCountType.ERROR_RESPONSE_COUNT,
                    sessionId
                )
            }
            if (error != null && error.cause is TimeoutException) {
                Misc.increaseTransactionMonitorCounter(
                    ctx,
                    TransactionCountType.NO_RESPONSE_COUNT,
                    sessionId
                )
            }
            callback.onCompleted(null, response, true)
        }
    }

    private inline fun handleRequest(
        url: String,
        crossinline block: (SafeRunResult<String?>) -> Unit
    ) {
        scope.launch {
            val result = safeRunIO {
                client.bankOneService.operationGet(url)
            }

            block(result)
        }
    }
}
