package com.cluster.network.online

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.cluster.core.data.CreditClubClient
import com.cluster.core.data.Encryption
import com.cluster.core.data.api.BankOneService
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.SafeRunResult
import com.cluster.core.util.safeRunIO
import com.cluster.model.TransactionCountType
import com.cluster.utility.Misc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeoutException

class APIHelper(
    private val ctx: Context,
    private val scope: CoroutineScope,
    private val localStorage: LocalStorage,
    private val client: CreditClubClient,
) {
    fun interface Callback<T> {
        fun onCompleted(e: Exception?, result: T?, status: Boolean)
    }

    fun attemptValidation(
        pNumber: String,
        sessionId: String,
        activationCode: String,
        location: String,
        state: Boolean,
        callback: Callback<String>,
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
        callback: Callback<String>,
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
        sessionId: String?,
        next: String,
        location: String,
        callback: Callback<String>,
    ) {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)

        val url = BankOneService.UrlGenerator.operationNext(
            pNumber,
            sessionId ?: "nothing",
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

    suspend fun getNextOperation(
        pNumber: String,
        sessionId: String?,
        next: String,
        location: String,
    ): SafeRunResult<String?> {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)

        val url = BankOneService.UrlGenerator.operationNext(
            pNumber,
            sessionId ?: "nothing",
            next,
            location,
            localStorage.institutionCode
        )
        val result = safeRunIO {
            client.bankOneService.operationGet(url)
        }
        if (result.data == null)
            Misc.increaseTransactionMonitorCounter(
                ctx,
                TransactionCountType.ERROR_RESPONSE_COUNT,
                sessionId
            )
        if (result.error != null && result.error!!.cause is TimeoutException) {
            Misc.increaseTransactionMonitorCounter(
                ctx,
                TransactionCountType.NO_RESPONSE_COUNT,
                sessionId
            )
        }

        return result
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

        val body = MultipartBody.Part.createFormData("file", image.name, requestFile)

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

    suspend fun continueNextOperation(
        pNumber: String,
        sessionId: String?,
        next: String,
        location: String,
    ): SafeRunResult<String?> {
        //Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId);
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val url = BankOneService.UrlGenerator.operationContinue(
            pNumber,
            sessionId ?: "nothing",
            next,
            location,
            localStorage.institutionCode
        )
        val result = safeRunIO {
            client.bankOneService.operationGet(url)
        }
        if (result.data == null) {
            Misc.increaseTransactionMonitorCounter(
                ctx,
                TransactionCountType.ERROR_RESPONSE_COUNT,
                sessionId
            )
        }
        if (result.error != null && result.error!!.cause is TimeoutException) {
            Misc.increaseTransactionMonitorCounter(
                ctx,
                TransactionCountType.NO_RESPONSE_COUNT,
                sessionId
            )
        }

        return result
    }

    private inline fun handleRequest(
        url: String,
        crossinline block: (SafeRunResult<String?>) -> Unit,
    ) {
        scope.launch {
            val result = safeRunIO {
                client.bankOneService.operationGet(url)
            }

            block(result)
        }
    }
}
