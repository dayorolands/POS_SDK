package com.globalaccelerex.nexgo.n3

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.printer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class N3Printer(override val context: Context, override val dialogProvider: DialogProvider) :
    PosPrinter {
    private val json = Json(
        JsonConfiguration.Stable.copy(
            isLenient = true,
            ignoreUnknownKeys = true,
            serializeSpecialFloatingPointValues = true,
            useArrayPolymorphism = true
        )
    )
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
            block?.invoke(print(object : PrintJob {
                override val nodes: List<PrintNode>
                    get() = nodes
            }))
        }
    }

    override suspend fun print(
        printJob: PrintJob,
        message: String,
        retryOnFail: Boolean
    ): PrinterStatus {
        val stringFields = mutableListOf<StringField>()
        for (node in printJob.nodes) {
            if (node is TextNode) {
                val align = when (node.align) {
                    Alignment.LEFT -> "left"
                    Alignment.MIDDLE -> "center"
                    Alignment.RIGHT -> "right"
                }
                stringFields.add(
                    StringField(
                        false,
                        TextField(node.text, align, "medium", false),
                        TextField("", align, "medium", false)
                    )
                )
            }
        }

        val printFields = listOf(PrintField("", 1, stringFields))
        val printObject = PrintObject(printFields)

        print(printObject)
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

    private suspend fun print(printObject: PrintObject): ActivityResult? {
        return print(json.stringify(PrintObject.serializer(), printObject))
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
