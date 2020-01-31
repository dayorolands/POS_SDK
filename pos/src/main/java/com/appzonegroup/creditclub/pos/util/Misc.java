package com.appzonegroup.creditclub.pos.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import com.appzonegroup.creditclub.pos.BuildConfig;
import com.appzonegroup.creditclub.pos.card.CardMisc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.SecureRandom;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Joseph on 7/19/2017.
 */
public class Misc extends CardMisc {

    private static String BASE_URL = BuildConfig.API_HOST + "/CreditClubMiddleWareAPI/api";

    public static String getVersionName(Activity activity) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getGUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }


    public static void populateSpinnerWithString(Activity activity, ArrayList<String> data, Spinner spinner) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, data);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    public static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("TakePicture", "Directory not created");
        }
        return file;
    }

    public static String toMoneyFormat(double moneyFloat) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String returnVal = formatter.format(moneyFloat).replace("$", "").replace("£", "");
        return returnVal;
    }

    public static String getReportURL(String agentPhoneNumber, String institutionCode, int transactionType,
                                      String fromDate, String toDate, int status, int startIndex, int maxSize) {
        String url = BASE_URL + "/Report/GetTransactions?" +
                "agentPhoneNumber=%s" +
                "&institutionCode=%s" +
                "&transactionType=%d" +
                "&from=%s" +
                "&to=%s" +
                "&status=%d" +
                "&startIndex=%d" +
                "&maxSize=%d";

        return String.format(url, agentPhoneNumber, institutionCode, transactionType,
                fromDate, toDate, status, startIndex, maxSize);
    }

    public static ArrayList<String> getScrambledPINPadText() {
        ArrayList<String> pinPadText = new ArrayList<String>() {
            {
                add("1");
                add("2");
                add("3");
                add("4");
                add("5");
                add("6");
                add("7");
                add("8");
                add("9");
                add("0");
                add("x");
                add("-");
            }
        };

        ArrayList<String> outputList = new ArrayList<>();
        Random random = new SecureRandom();
        while (pinPadText.size() > 3) {
            int index = random.nextInt(pinPadText.size() - 3);

            outputList.add(pinPadText.get(index));
            pinPadText.remove(index);
        }

        //3 items should be left in the list -> 1 number, < and DONE
        outputList.add(pinPadText.get(1));
        pinPadText.remove(1);

        outputList.addAll(pinPadText);

        return outputList;
    }

    public static String getCategoryURL() {

        String url = BASE_URL + "/BillsPayment/GetBillCategories";
        return url;
    }

    public static String getBillersURL(String categoryIdField) {

        String url = BASE_URL + "/BillsPayment/GetBillersByCategory?" +
                "category=%s";

        return String.format(url, categoryIdField);
    }

    public static String getBillerItemURL(String billerIdField) {
        String url = BASE_URL + "/BillsPayment/GetBillerItemsByBiller?" +
                "biller=%s";

        return String.format(url, billerIdField);
    }

    public static String getPendingBeneficiaries(String institutionCode, String agentPhoneNumber) {

        String url = BASE_URL + "/OfflineCashOut/GetPendingBeneficiaries?" +
                "institutionCode=%s" +
                "&agentPhoneNumber=%s";

        return String.format(url, institutionCode, agentPhoneNumber);
    }

    public static String syncBeneficiaryUrl(String institutionCode, String agentPhoneNumber, String trackingReference) {

        String url = BASE_URL + "/OfflineCashOut/UpdatePaidBeneficiary?" +
                "institutionCode=%s" +
                "&agentPhoneNumber=%s" +
                "&offlineBeneficiaryTrackingRef=%s";

        return String.format(url, institutionCode, agentPhoneNumber, trackingReference);
    }


    public static String getAgentsAccountEnquiry(String agentPhoneNumber, String institutionCode, String agentPin) {
        String url = BASE_URL + "/CoreBanking/AgentAccountEnquiry?" +
                "agentPhoneNumber=%s" +
                "&institutionCode=%s" +
                "&agentPIn=%s";

        return String.format(url, agentPhoneNumber, institutionCode, agentPin);
    }

    public static String getCustomerAccountEnquiry(String customerAccountNumber, String institutionCode, String agentsPhoneNo, String pin) {
        String url = BASE_URL + "/CoreBanking/CustomerAccountEnquiry?" +
                "accountNo=%s" +
                "&institutionCode=%s" +
                "&agentPhoneNumber=%s" +
                "&pin=%s";

        return String.format(url, customerAccountNumber, institutionCode, agentsPhoneNo, pin);
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


    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    /*// Encrypts the string and encode in Base64
    public static String encryptText(String plainText) {
        try {
            // ---- Use specified 3DES key and IV from other source --------------
            byte[] plaintext = plainText.getBytes();//input
            byte[] tdesKeyData = AppConstants.getEncryptionKey().getBytes();// your encryption key

            byte[] myIV = new byte[16];// initialization vector

            Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
            IvParameterSpec ivspec = new IvParameterSpec(myIV);

            c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
            byte[] cipherText = c3des.doFinal(plaintext);
            String encryptedString = Base64.encodeToString(cipherText, Base64.DEFAULT);
            // return Base64Coder.encodeString(new String(cipherText));
            return encryptedString;
        }catch (Exception ex)
        {
            return null;
        }
    }
*/

    /**
     * Method returns an array of size 2.
     * Index 0 - Available RAM
     * Index 1 - Total RAM
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

    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
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
        //long blockSize = stat.getBlockSize();
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

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
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

        return phoneNumber.startsWith("080") || phoneNumber.startsWith("081") || phoneNumber.startsWith("070") ||
                phoneNumber.startsWith("090");

    }

    public static byte[] hexStringToByte(String hex) {
        if(hex == null || hex.length()==0){
            return null;
        }
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    public static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
