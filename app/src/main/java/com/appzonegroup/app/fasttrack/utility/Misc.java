package com.appzonegroup.app.fasttrack.utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import androidx.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.appzonegroup.app.fasttrack.BuildConfig;
import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.dataaccess.DeviceTransactionInformationDAO;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.MainMenuItem;
import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.appzonegroup.app.fasttrack.model.jsonbody.PayBillItemModel;
import com.appzonegroup.app.fasttrack.scheduler.BackgroundThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Joseph on 7/19/2017.
 */
public class Misc {

    private static String HOST = BuildConfig.API_HOST;
    private static String BASE_URL = HOST + "/CreditClubMiddleWareAPI/api";

    public static boolean isValidNumber(String number) {
        return isRegexMatch("^[0-9]{11}$", number);
    }

    private static boolean isRegexMatch(String regex, String string) {
        return string.matches(regex);
    }

    public static String getGUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static Handler setupScheduler() {
        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        return new Handler(backgroundThread.getLooper());
    }

    public static void populateSpinnerWithString(Activity activity, ArrayList<String> data, Spinner spinner) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, data);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        // String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static String toMoneyFormat(double moneyFloat) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String returnVal = formatter.format(moneyFloat).replace("$", "").replace("£", "");
        return returnVal;
    }

    public static String getReportURL(String agentPhoneNumber, String institutionCode, int transactionType,
                                      String fromDate, String toDate, int status, int startIndex, int maxSize) {
        String url = BASE_URL + "/Report/GetTransactions?" + "agentPhoneNumber=%s" + "&institutionCode=%s"
                + "&transactionType=%d" + "&from=%s" + "&to=%s" + "&status=%d" + "&startIndex=%d" + "&maxSize=%d";

        return String.format(url, agentPhoneNumber, institutionCode, transactionType, fromDate, toDate, status,
                startIndex, maxSize);
    }

    public static String getCategoryURL() {
        return BASE_URL + "/PayBills/GetBillerCategories";
    }

    public static String getBillerItemURL(String billerIdField) {
        String url = BASE_URL + "/PayBills/GetPaymentItems";

        return String.format(url, billerIdField);
    }

    public static String payBillItemUrl(PayBillItemModel model) {
        String url = BASE_URL + "/BillsPayment/PayBillItem?" + "agentPhone=%s" + "&agentPIN=%s" + "&institutionCode=%s"
                + "&otp=%s" + "&merchantBillerIdField=%s" + "&billItemID=%s" + "&billCategoryID=%s"
                + "&customerAccountNumber=%s" + "&amount=%s" + "&email=%s" + "&customerPhoneNumber=%s"
                + "&customerID=%s";

        return String.format(url, model.getAgentPhone(), model.getAgentPIN(), model.getInstitutionCode(),
                model.getOtp(), model.getMerchantBillerIdField(), model.getBillItemID(), model.getBillCategoryID(),
                model.getCustomerAccountNumber(), model.getAmount(), model.getEmail(), model.getCustomerPhoneNumber(),
                model.getCustomerID());
    }

    public static String dateToShortString(Date date) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    public static String dateToShortStringDDMMYYYY(Date date) {
        Format formatter = new SimpleDateFormat("dd-MM-yyyy");
        return formatter.format(date);
    }

    public static String dateToLongString(Date date) {
        Format formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS");
        String part = formatter.format(date);
        part = part + "0000 ";
        return part + new SimpleDateFormat("a").format(date);
    }

    public static String dateToLongStringNoMicrosecond(Date date) {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss").format(date);
    }

    public static Date stringToDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static Date serverTimetoDate(String time) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("custom", "unable to parse time");
        }
        return new Date();
    }

    public static Date getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getCurrentDateLongString() {
        return dateToLongString(getCurrentDateTime());
    }

    public static ArrayList<MainMenuItem> getMainMenuItems(Context context) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        mainMenuItems.add(new MainMenuItem(R.drawable.open_account, null));
        mainMenuItems.add(new MainMenuItem(R.drawable.cash_deposit, null));
        mainMenuItems.add(new MainMenuItem(R.drawable.cash_withdrawal, null));
        // mainMenuItems.add(new MainMenuItem(R.drawable.bvn_update, null));
        mainMenuItems.add(new MainMenuItem(R.drawable.loan_request, null));
        // mainMenuItems.add(new MainMenuItem(R.drawable.funds_transfer, null));
        mainMenuItems.add(new MainMenuItem(R.drawable.conditional_cash_transfer, null));
        mainMenuItems.add(new MainMenuItem(R.drawable.bills_payment, null));

        // New Menu items
        mainMenuItems.add(new MainMenuItem(R.drawable.interbank_withdrawal, "InterBank Withdrawal"));
        mainMenuItems.add(new MainMenuItem(R.drawable.intrabank_withdrawal, "Same bank Withdrawal"));

        if (context.getPackageName().toLowerCase().contains("farepay")) {
            // this is for link card... it should be changed to the right image when ready
            mainMenuItems.add(new MainMenuItem(R.drawable.bg_min, "Link Card"));

            // this is for hotlist card... it should be changed to the right image when
            // ready
            mainMenuItems.add(new MainMenuItem(R.drawable.ic_launcher_transparent, "Hotlist card"));
        }

        return mainMenuItems;
    }

    public static ArrayList<MainMenuItem> getCreditClubMainMenu() {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_register, "Register Customer"));
        mainMenuItems.add(new MainMenuItem(R.drawable.deposit, "Deposit"));
        mainMenuItems.add(new MainMenuItem(R.drawable.withdraw, "Withdrawal"));
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_agent_balance, "Agent Balance"));
        mainMenuItems.add(new MainMenuItem(R.drawable.customer_account, "Customer Account"));
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_password, "Change PIN"));
        mainMenuItems.add(new MainMenuItem(R.drawable.loan, "Loan Request"));
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_fingerprint, "BVN Update"));
        mainMenuItems.add(new MainMenuItem(R.drawable.kia_kia_ft, "Kia Kia Funds Transfer"));

        return mainMenuItems;
    }

    public static ArrayList<MainMenuItem> getCashOutMainMenuItems(Context context) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_mail, "Token Cash-Out"));
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_credit_card, "Card cash-Out"));
        mainMenuItems.add(new MainMenuItem(R.drawable.ic_money, "Funds Transfer"));

        // New Menu items
        /*
         * mainMenuItems.add(new MainMenuItem(R.drawable.interbank_withdrawal,
         * "InterBank Withdrawal")); mainMenuItems.add(new
         * MainMenuItem(R.drawable.intrabank_withdrawal, "Same bank Withdrawal"));
         */

        return mainMenuItems;
    }

    /*
     * // Encrypts the string and encode in Base64 public static String
     * encryptText(String plainText) { try { // ---- Use specified 3DES key and IV
     * from other source -------------- byte[] plaintext =
     * plainText.getBytes();//input byte[] tdesKeyData =
     * AppConstants.getEncryptionKey().getBytes();// your encryption key
     *
     * byte[] myIV = new byte[16];// initialization vector
     *
     * Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding"); SecretKeySpec
     * myKey = new SecretKeySpec(tdesKeyData, "DESede"); IvParameterSpec ivspec =
     * new IvParameterSpec(myIV);
     *
     * c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec); byte[] cipherText =
     * c3des.doFinal(plaintext); String encryptedString =
     * Base64.encodeToString(cipherText, Base64.DEFAULT); // return
     * Base64Coder.encodeString(new String(cipherText)); return encryptedString;
     * }catch (Exception ex) { return null; } }
     */

    /**
     * Method returns an array of size 2. Index 0 - Available RAM Index 1 - Total
     * RAM
     *
     * @param context
     * @return
     */
    @NonNull
    public static long[] getRAM(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return new long[]{memInfo.availMem, memInfo.totalMem};
    }

    public static String getTotalMemory() {
        return formatMemorySize(getTotalInternalMemorySize() + getTotalExternalMemorySize());
    }

    public static String getAvailableMemory() {
        return formatMemorySize(getAvailableInternalMemorySize() + getAvailableExternalMemorySize());
    }

    private static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    private static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;// = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
        } else {
            blockSize = stat.getBlockSize();
        }

        long availableBlocks;// = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            availableBlocks = stat.getAvailableBlocks();
        }

        return availableBlocks * blockSize;
    }

    private static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        // long blockSize = stat.getBlockSize();
        long blockSize;// = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
        } else {
            blockSize = stat.getBlockSize();
        }

        long totalBlocks;// = stat.getBlockCount();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            totalBlocks = stat.getBlockCountLong();
        } else {
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }

    private static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
            } else {
                blockSize = stat.getBlockSize();
            }

            long availableBlocks;// = stat.getAvailableBlocks();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                availableBlocks = stat.getAvailableBlocks();
            }

            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    private static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());

            long blockSize = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
            } else {
                blockSize = stat.getBlockSize();
            }

            long totalBlocks;// = stat.getBlockCount();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalBlocks = stat.getBlockCountLong();
            } else {
                totalBlocks = stat.getBlockCount();
            }
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static String formatMemorySize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;

                if (size >= 1024) {
                    suffix = "GB";
                    size /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null)
            resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static int getTransactionMonitorCounter(Context context, String key) {
        String value = LocalStorage.GetValueFor(key, context);
        int count = 0;
        if (value != null)
            count = Integer.parseInt(value);

        LocalStorage.SaveValue(key, String.valueOf(count), context);
        return count;
    }

    public static void increaseTransactionMonitorCounter(Context context, TransactionCountType transactionCountType,
                                                         String sessionID) {
        String key = "";
        switch (transactionCountType) {
            case ERROR_RESPONSE_COUNT:
                key = AppConstants.getErrorResponseCount();
                break;
            case NO_INTERNET_COUNT:
                key = AppConstants.getNoInternetCount();
                break;
            case REQUEST_COUNT:
                key = AppConstants.getRequestCount();
                break;
            case SUCCESS_COUNT:
                key = AppConstants.getSuccessCount();
                break;
            case NO_RESPONSE_COUNT:
            default:
                key = AppConstants.getNoResponseCount();
                break;
        }

        String value = LocalStorage.GetValueFor(key, context);
        int count = 0;
        if (value != null)
            count = Integer.parseInt(value) + 1;

        LocalStorage.SaveValue(key, String.valueOf(count), context);
        DeviceTransactionInformationDAO.Insert(context, sessionID);
    }

    public static void resetTransactionMonitorCounter(Context context) {
        LocalStorage.SaveValue(AppConstants.getRequestCount(), "0", context);
        LocalStorage.SaveValue(AppConstants.getSuccessCount(), "0", context);
        LocalStorage.SaveValue(AppConstants.getErrorResponseCount(), "0", context);
        LocalStorage.SaveValue(AppConstants.getNoInternetCount(), "0", context);
        LocalStorage.SaveValue(AppConstants.getNoResponseCount(), "0", context);
    }

    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    public static void sleep(long seconds) {
        try {
            Thread.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidatePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.trim();
        if (phoneNumber.length() != 11) {
            return false;
        }

        try {
            Long.parseLong(phoneNumber);
        } catch (Exception ex) {
            return false;
        }

        if (phoneNumber.startsWith("080") || phoneNumber.startsWith("081") || phoneNumber.startsWith("070")
                || phoneNumber.startsWith("090")) {
            return true;
        } else {
            return false;
        }

    }

}
