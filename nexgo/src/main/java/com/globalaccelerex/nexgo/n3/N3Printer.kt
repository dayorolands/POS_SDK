package com.globalaccelerex.nexgo.n3

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrintJob
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.PrinterStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

private const val JSON_DATA = """
    {
 "Receipt": [{
 "Bitmap": "filename",
 "letterSpacing": 5,
 "String": [{
 "isMultiline": true,
 "header": {
 "text": "Merchant Name",
 "align": "centre",
 "size": "large",
 "isBold": true
 },
 "body": {
 "text": "Global Accelerex",
 "alignment": "centre",
 "size": "normal",
 "isBold": false
 }
 },
 {
 "isMultiline": false,
 "header": {
 "text": "Reference Number",
 "align": "left",
 "size": "large",
 "isBold": true
 },
 "body": {
 "text": "123456789"
 }
 }
 ]
 },
 {
 "Bitmap": "filename",
 "letterSpacing": 5,
 "String": [{
 "isMultiline": true,
 "header": {
 "text": "Merchant Name",
 "align": "centre",
 "size": "large",
 "isBold": true
 },
 "body": {
 "text": "Allen Tobi",
 "alignment": "centre",
 "size": "normal",
 "isBold": false
 }
 },
 {
 "isMultiline": false,
 "header": {
 "text": "Reference Number",
 "align": "left",
 "size": "large",
 "isBold": true
 },
 "body": {
 "text": "abcd1234"
 }
 }
 ]
 }
 ]
}

"""

class N3Printer(override val context: Context, override val dialogProvider: DialogProvider) :
    PosPrinter {
    private val json = Json(JsonConfiguration.Stable)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun check(): PrinterStatus {
        return PrinterStatus.READY
    }

    override fun printAsync(
        nodes: List<PrintNode>,
        message: String,
        block: ((PrinterStatus) -> Unit)?
    ) {
        mainScope.launch {
            print(JSON_DATA)
            block?.invoke(PrinterStatus.READY)
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean
    ): PrinterStatus {
        print(JSON_DATA)
        return PrinterStatus.READY
    }

    override fun print(nodes: List<PrintNode>): PrinterStatus {
        mainScope.launch {
            print(JSON_DATA)
        }
        return PrinterStatus.READY
    }

    private suspend fun print(jsonString: String): ActivityResult? {
        val intent = Intent(PRINTER_INTENT)
        intent.putExtra("jsonData", jsonString)
        return when (context) {
            is ComponentActivity -> context.getActivityResult(intent)
            is Fragment -> context.getActivityResult(intent)
            else -> null
        }
    }

    @Serializable
    data class PrintObject(@SerialName("Receipt") val printFields: List<PrintField>)

    @Serializable
    data class PrintField(
        @SerialName("Bitmap") val filename: String,
        @SerialName("letterSpacing") val letterSpacing: Int,
        @SerialName("String") val stringFields: List<StringField>
    )

    @Serializable
    data class StringField(
        @SerialName("isMultiline") val isMultiline: Boolean,
        @SerialName("header") val header: TextField,
        @SerialName("body") val body: TextField
    )

    @Serializable
    data class TextField(
        @SerialName("text") val text: String,
        @SerialName("align") val align: String?,
        @SerialName("size") val size: String?,
        @SerialName("isBold") val isBold: Boolean?
    )

    companion object {
        private const val PRINTER_INTENT = "com.globalaccelerex.printer"
    }
}
