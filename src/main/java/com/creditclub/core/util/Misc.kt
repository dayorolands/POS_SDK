package com.creditclub.core.util

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.creditclub.core.BuildConfig

import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.util.*

object Misc {

    private val BASE_URL = BuildConfig.API_HOST + "/CreditClubMiddleWareAPI/api"

    val guid: String
        get() = UUID.randomUUID().toString().substring(0, 8)

    // 3 items should be left in the list -> 1 number, < and DONE
    val scrambledPINPadText: ArrayList<String>
        get() {
            val pinPadText = object : ArrayList<String>() {
                init {
                    add("1")
                    add("2")
                    add("3")
                    add("4")
                    add("5")
                    add("6")
                    add("7")
                    add("8")
                    add("9")
                    add("0")
                    add("x")
                    add("-")
                }
            }

            val outputList = ArrayList<String>()
            val random = Random()
            while (pinPadText.size > 3) {
                val index = random.nextInt(pinPadText.size - 3)

                outputList.add(pinPadText[index])
                pinPadText.removeAt(index)
            }
            outputList.add(pinPadText[1])
            pinPadText.removeAt(1)

            outputList.addAll(pinPadText)

            return outputList
        }

    val categoryURL: String
        get() = "$BASE_URL/PayBills/GetBillerCategories"

    val currentDateTime: Date
        get() {
            val calendar = Calendar.getInstance()
            return calendar.time
        }

    val currentDateLongString: String
        get() = currentDateTime.longString

//    val creditClubMainMenu: ArrayList<MainMenuItem>
//        get() {
//            val mainMenuItems = ArrayList<MainMenuItem>()
//            mainMenuItems.add(MainMenuItem(R.drawable.ic_register, "Register Customer"))
//            mainMenuItems.add(MainMenuItem(R.drawable.deposit, "Deposit"))
//            mainMenuItems.add(MainMenuItem(R.drawable.withdraw, "Withdrawal"))
//            mainMenuItems.add(MainMenuItem(R.drawable.ic_agent_balance, "Agent Balance"))
//            mainMenuItems.add(MainMenuItem(R.drawable.customer_account, "Customer Account"))
//            mainMenuItems.add(MainMenuItem(R.drawable.ic_password, "Change PIN"))
//            mainMenuItems.add(MainMenuItem(R.drawable.loan, "Loan Request"))
//            mainMenuItems.add(MainMenuItem(R.drawable.ic_fingerprint, "BVN Update"))
//            mainMenuItems.add(MainMenuItem(R.drawable.kia_kia_ft, "Kia Kia Funds Transfer"))
//
//            return mainMenuItems
//        }

    val totalMemory: String
        get() = formatMemorySize(totalInternalMemorySize + totalExternalMemorySize)

    val availableMemory: String
        get() = formatMemorySize(availableInternalMemorySize + availableExternalMemorySize)

    private// = 0;
    // = 0;
    val availableInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stat.blockSizeLong
            } else {
                stat.blockSize.toLong()
            }

            val availableBlocks: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stat.availableBlocksLong
            } else {
                stat.availableBlocks.toLong()
            }

            return availableBlocks * blockSize
        }

    private// long blockSize = stat.getBlockSize();
    // = 0;
    // = stat.getBlockCount();
    val totalInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stat.blockSizeLong
            } else {
                stat.blockSize.toLong()
            }

            val totalBlocks: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stat.blockCountLong
            } else {
                stat.blockCount.toLong()
            }
            return totalBlocks * blockSize
        }

    private// = stat.getAvailableBlocks();
    val availableExternalMemorySize: Long
        get() {
            if (externalMemoryAvailable()) {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)
                val blockSize: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    stat.blockSizeLong
                } else {
                    stat.blockSize.toLong()
                }

                val availableBlocks: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    stat.availableBlocksLong
                } else {
                    stat.availableBlocks.toLong()
                }

                return availableBlocks * blockSize
            } else {
                return -1
            }
        }

    private// = stat.getBlockCount();
    val totalExternalMemorySize: Long
        get() {
            if (externalMemoryAvailable()) {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)

                val blockSize: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    stat.blockSizeLong
                } else {
                    stat.blockSize.toLong()
                }

                val totalBlocks: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    stat.blockCountLong
                } else {
                    stat.blockCount.toLong()
                }

                return totalBlocks * blockSize
            } else {
                return 0
            }
        }

    val randomString: String
        get() = UUID.randomUUID().toString()

    fun isValidNumber(number: String): Boolean {
        return isRegexMatch("^[0-9]{11}$", number)
    }

    private fun isRegexMatch(regex: String, string: String): Boolean {
        return string.matches(regex.toRegex())
    }

//    fun setupScheduler(): Handler {
//        val backgroundThread = BackgroundThread()
//        backgroundThread.start()
//        return Handler(backgroundThread.getLooper())
//    }

    fun populateSpinnerWithString(activity: Activity, data: ArrayList<String>, spinner: Spinner) {
        val arrayAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, data)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
    }

    fun getAlbumStorageDir(albumName: String): File {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.e("TakePicture", "Directory not created")
        }
        return file
    }

    fun bitmapToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        // String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun toMoneyFormat(moneyFloat: Double): String {
        val formatter = NumberFormat.getCurrencyInstance()
        return formatter.format(moneyFloat).replace("$", "").replace("£", "")
    }

    fun getPendingBeneficiaries(institutionCode: String, agentPhoneNumber: String): String {

        val url = (BASE_URL + "/OfflineCashOut/GetPendingBeneficiaries?" + "institutionCode=%s"
                + "&agentPhoneNumber=%s")

        return String.format(url, institutionCode, agentPhoneNumber)
    }

    fun syncBeneficiaryUrl(institutionCode: String, agentPhoneNumber: String, trackingReference: String): String {

        val url = (BASE_URL + "/OfflineCashOut/UpdatePaidBeneficiary?" + "institutionCode=%s" + "&agentPhoneNumber=%s"
                + "&offlineBeneficiaryTrackingRef=%s")

        return String.format(url, institutionCode, agentPhoneNumber, trackingReference)
    }

    fun getAgentsAccountEnquiry(agentPhoneNumber: String, institutionCode: String, agentPin: String): String {
        val url = (BASE_URL + "/CoreBanking/AgentAccountEnquiry?" + "agentPhoneNumber=%s" + "&institutionCode=%s"
                + "&agentPIn=%s")

        return String.format(url, agentPhoneNumber, institutionCode, agentPin)
    }

    fun getCustomerAccountEnquiry(
        customerAccountNumber: String, institutionCode: String,
        agentsPhoneNo: String, pin: String
    ): String {
        val url = (BASE_URL + "/CoreBanking/CustomerAccountEnquiry?" + "accountNo=%s" + "&institutionCode=%s"
                + "&agentPhoneNumber=%s" + "&pin=%s")

        return String.format(url, customerAccountNumber, institutionCode, agentsPhoneNo, pin)
    }

    private fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun formatMemorySize(size: Long): String {
        var size = size
        var suffix: String? = null

        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024

                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024
                }
            }
        }

        val resultBuffer = StringBuilder(size.toString())

        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }

        if (suffix != null)
            resultBuffer.append(suffix)
        return resultBuffer.toString()
    }


    fun sleep(seconds: Long) {
        try {
            Thread.sleep(seconds)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
