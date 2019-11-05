package com.appzonegroup.creditclub.pos.printer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.os.Looper
import android.widget.Toast
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.contract.DialogProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.telpo.tps550.api.printer.UsbThermalPrinter
import com.telpo.tps550.api.util.StringUtil
import com.telpo.tps550.api.util.SystemUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 1/14/2019.
 * Appzone Ltd
 */
class PosPrinter(private val context: Context, private val dialogProvider: DialogProvider) :
    UsbThermalPrinter(context) {
    private var lowBattery = false

    @Throws(WriterException::class)
    fun createCode(str: String, type: BarcodeFormat, bmpWidth: Int, bmpHeight: Int): Bitmap {
        val mHashTable = Hashtable<EncodeHintType, String>()
        mHashTable[EncodeHintType.CHARACTER_SET] = "UTF-8"

        val matrix = MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight, mHashTable)
        val width = matrix.width
        val height = matrix.height

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = -0x1000000
                } else {
                    pixels[y * width + x] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private val printReceive: BroadcastReceiver

    fun checkAsync(block: (PrinterStatus) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val status = withContext(Dispatchers.Default) {
                check()
            }

            block(status)
        }
    }

    fun check(): PrinterStatus {
        return try {
            start(0)
            reset()

            if (checkStatus() == 0) PrinterStatus.READY else PrinterStatus.NOT_READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }


    fun printAsync(
        printJob: PrintJob,
        message: String = "Printing...",
        block: ((PrinterStatus) -> Unit)? = null
    ) {
        printAsync(printJob.nodes, message, block)
    }

    fun printAsync(
        vararg nodes: PrintNode,
        message: String = "Printing...",
        block: ((PrinterStatus) -> Unit)? = null
    ) {
        printAsync(nodes.asList(), message, block)
    }

    fun printAsync(
        nodes: List<PrintNode>,
        message: String = "Printing...",
        block: ((PrinterStatus) -> Unit)? = null
    ) {
        GlobalScope.launch(Dispatchers.Main) {

            dialogProvider.showProgressBar(message)

            val status = withContext(Dispatchers.Default) {
                Looper.myLooper() ?: Looper.prepare()
                print(nodes)
            }

            dialogProvider.hideProgressBar()
            block?.invoke(status)

        }
    }

    fun print(printJob: PrintJob): PrinterStatus = print(printJob.nodes)

    fun print(nodes: List<PrintNode>): PrinterStatus {
        for (node in nodes) {
            val status: PrinterStatus = when (node) {
                is TextNode -> print(node)
                is ImageNode -> print(node)
                is WalkPaper -> print(node)
                else -> PrinterStatus.READY
            }

            if (status != PrinterStatus.READY) return status
        }

        return PrinterStatus.READY
    }

    fun print(node: ImageNode): PrinterStatus {
        return try {
            reset()
            setGray(node.printGray)
            setAlgin(node.align.code)
            val bm = BitmapFactory.decodeResource(context.resources, node.drawable)

            if (bm != null) {
                printLogo(bm, false)
                PrinterStatus.READY
            } else {
                PrinterStatus.IMAGE_NOT_FOUND
            }
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    fun print(node: WalkPaper): PrinterStatus {
        return try {
            reset()
            walkPaper(node.walkPaperAfterPrint)
            PrinterStatus.READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    fun print(node: QrCodeNode): PrinterStatus {
        return try {
            reset()
            setGray(node.printGray)
            val bitmap: Bitmap? = createCode(node.text, BarcodeFormat.QR_CODE, 256, 256)
            if (bitmap != null) {
                printLogo(bitmap, true)
            }
            addString(node.text)
            printString()
            walkPaper(20)

            PrinterStatus.READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    fun print(node: BarCodeNode): PrinterStatus {
        return try {
            reset()
            setGray(node.printGray)
            val bitmap: Bitmap? = createCode(node.text, BarcodeFormat.CODE_128, 320, 176)
            if (bitmap != null) {
                printLogo(bitmap, true)
            }
            addString(node.text)
            printString()
            walkPaper(20)

            PrinterStatus.READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

//    fun marker(node: SearchMark): PrinterStatus {
//        return try {
//            reset()
//            searchMark(
//                Integer.parseInt("200"),
//                Integer.parseInt("50")
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            printerStatus(e)
//        }
//    }

    fun print(node: TextNode): PrinterStatus {
        if (node.leftDistance > MAX_LEFT_DISTANCE) {
            Toast.makeText(context, context.getString(R.string.outOfLeft), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.lineDistance > 255) {
            Toast.makeText(context, context.getString(R.string.outOfLine), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.wordFont > 55 || node.wordFont < 15) {
            Toast.makeText(context, context.getString(R.string.outOfFont), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.printGray < 0 || node.printGray > 7) {
            Toast.makeText(context, context.getString(R.string.outOfGray), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        if (node.text.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.empty), Toast.LENGTH_LONG).show()
            return PrinterStatus.READY
        }

        return try {
            reset()
            setAlgin(node.align.code)
            setLeftIndent(node.leftDistance)
            setLineSpace(node.lineDistance)

            if (node.wordFont > 45) setFontSize(node.wordFont)
            else setTextSize(node.wordFont)

            setGray(node.printGray)
            addString(node.text)
            printString()
            walkPaper(node.walkPaperAfterPrint)

            PrinterStatus.READY
        } catch (e: Exception) {
            e.printStackTrace()
            printerStatus(e)
        }
    }

    private fun printerStatus(e: Exception) = when (e.toString()) {
        "com.telpo.tps550.api.NoPaperException" -> PrinterStatus.NO_PAPER
        "com.telpo.tps550.api.OverHeatException" -> PrinterStatus.OVER_HEAT
        "com.telpo.tps550.api.BlackBlockNotFoundException" -> PrinterStatus.NO_BLACK_BLOCK
        else -> PrinterStatus.NOT_READY
    }

    companion object {
        private const val MAX_LEFT_DISTANCE = 255
    }

    init {
        printReceive = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == Intent.ACTION_BATTERY_CHANGED) {
                    val status =
                        intent.getIntExtra(
                            BatteryManager.EXTRA_STATUS,
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING
                        )
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                    //TPS390 can not print,while in low battery,whether is charging or not charging
                    lowBattery =
                        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal) {
                            level * 5 <= scale
                        } else {
                            status != BatteryManager.BATTERY_STATUS_CHARGING && level * 5 <= scale
                        }
                } else if (action == "android.intent.action.BATTERY_CAPACITY_EVENT") {
                    val status = intent.getIntExtra("action", 0)
                    val level = intent.getIntExtra("level", 0)
                    lowBattery = status == 0 && level < 1
                } //Only use for TPS550MTK devices
            }
        }
        val pIntentFilter = IntentFilter()
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT")
//        context.registerReceiver(printReceive, pIntentFilter)
    }
}