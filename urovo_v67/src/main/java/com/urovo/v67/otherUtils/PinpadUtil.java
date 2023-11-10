package com.urovo.v67.otherUtils;

import android.device.SEManager;
import android.util.Log;

import com.urovo.sdk.pinpad.PinPadProviderImpl;
import com.urovo.sdk.pinpad.utils.Constant;
import com.urovo.sdk.utils.BytesUtil;
import com.urovo.sdk.utils.Funs;

public class PinpadUtil {


    public static String getPinData(int keyIndex, String Pan, byte[] data, String pinKey) {

        String pinBlock = "";
        try {
            byte[] key = Funs.StrToHexByte(pinKey);
//            if (pinKey.length() == 16) {
//                data = _3DES.des_Decrypt(key, data);
//            } else if (pinKey.length() == 32) {
//                data = _3DES.ThreeDes_Decrypt16(key, data);
//            }
            PinPadProviderImpl.getInstance().calculateDes(Constant.DesMode.DEC, Constant.Algorithm.ECB, Constant.KeyType.PIN_KEY, keyIndex, data, data);
            Log.e("pinBlock：", Funs.bytesToHexString(data));

            String panStr = Pan.substring(0, Pan.length() - 1);
            panStr = panStr.substring(panStr.length() - 12);
            panStr = "0000" + panStr;
            Log.e("panStr：", panStr);
            byte[] panBuff2 = Funs.StrToHexByte(panStr);

            do_xor_urovo(panBuff2, data, 8);

            Log.e("pinBlock 2：", Funs.bytesToHexString(panBuff2));
            pinBlock = Funs.bytesToHexString(panBuff2);
            int pinLen = Integer.parseInt(pinBlock.substring(0, 2), 16);
            Log.e("Pin length：", pinLen + "");
            pinBlock = pinBlock.substring(2, 2 + pinLen);

            Log.e("clear pin：", pinBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pinBlock;
    }

    public static String getPinData_SM4(int keyIndex, String Pan, byte[] data, String pinKey) {

        String pinBlock = "";
        try {
            byte[] key = BytesUtil.hexString2Bytes(pinKey);
//            data = SM4.SM4DecryptECB(data, data.length, key);
            PinPadProviderImpl.getInstance().calculateDes(Constant.DesMode.DEC, Constant.Algorithm.SM4, Constant.KeyType.PIN_KEY, keyIndex, data, data);
            Log.e("pinBlock：", BytesUtil.bytes2HexString(data));

            String panStr = Pan.substring(0, Pan.length() - 1);
            panStr = panStr.substring(panStr.length() - 12);
            panStr = BytesUtil.FormatWithZero(panStr, "00000000000000000000000000000000");

            Log.e("panStr：", panStr);
            byte[] panBuff2 = BytesUtil.hexString2Bytes(panStr);

            do_xor_urovo(panBuff2, data, 16);

            Log.e("pinBlock 2：", BytesUtil.bytes2HexString(panBuff2));
            pinBlock = BytesUtil.bytes2HexString(panBuff2);
            int pinLen = Integer.parseInt(pinBlock.substring(0, 2), 16);
            Log.e("Pin length：", pinLen + "");
            pinBlock = pinBlock.substring(2, 2 + pinLen);

            Log.e("clear pin：", pinBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pinBlock;
    }

    public static String getPinData_AES(String Pan, byte[] data, String pinKey) {

        String pinBlock = "";
        try {
            byte[] key = BytesUtil.hexString2Bytes(pinKey);
            //   data = SM4.SM4DecryptECB(data, data.length, key);
            int iRet = new SEManager().decryptData(2, 1, 7, new byte[16], 16,
                    0x00, data, data.length, data, new byte[1]);

            Log.e("decryptData：", "" + iRet);
            Log.e("pinBlock：", BytesUtil.bytes2HexString(data));

            String panStr = BytesUtil.FormatWithZero(Pan, "00000000000000000000000000000000");

//            String panStr = Pan.substring(0, Pan.length()-1);
//            panStr = panStr.substring(panStr.length()-12);
//            panStr = "0000"+panStr;
            Log.e("panStr：", panStr);
            byte[] panBuff2 = BytesUtil.hexString2Bytes(panStr);

            do_xor_urovo(panBuff2, data, 16);

            Log.e("pinBlcok：", BytesUtil.bytes2HexString(panBuff2));
            pinBlock = BytesUtil.bytes2HexString(panBuff2);
            int pinLen = Integer.parseInt(pinBlock.substring(0, 2), 16);
            Log.e("Pin length：", pinLen + "");
            pinBlock = pinBlock.substring(2, 2 + pinLen);

            Log.e("clear pin：", pinBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pinBlock;
    }

    public static void do_xor_urovo(byte[] src1, byte[] src2, int num) {
        int i;
        for (i = 0; i < num; i++) {
            src1[i] ^= src2[i];
        }
    }
}
