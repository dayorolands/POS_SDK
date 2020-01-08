package com.telpo.tps550.api.idcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import com.telpo.tps550.api.DeviceNotFoundException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.TimeoutException;

public class TPS900IDCard {
    public static String address = null;
    public static String born = null;
    static IdentityMsg idcardinfo = null;
    static IdentityMsg identityinfo = new IdentityMsg();
    static byte[] imgae = new byte[IdCard.READER_VID_BIG];
    private static Context mContext = null;
    public static String name = null;
    public static String nation = null;
    public static String number = null;
    public static String office = null;
    public static String sex = null;
    public static String term = null;

    private static native Object check_idcard(int i, int[] iArr);

    private static native boolean connect_idcard();

    private static native boolean decode_image(byte[] bArr);

    private static native boolean disconnect_idcard();

    private static native byte[] get_image();

    static {
        System.loadLibrary("idcard900");
    }

    public static synchronized void open(Context context) throws DeviceNotFoundException {
        synchronized (TPS900IDCard.class) {
            if (!connect_idcard()) {
                throw new DeviceNotFoundException();
            }
        }
    }

    public static synchronized IdentityMsg checkIdCard(int timeout) throws TelpoException {
        IdentityMsg info;
        synchronized (TPS900IDCard.class) {
            int[] result = new int[2];
            info = (IdentityMsg) check_idcard(timeout, result);
            Log.d("serial test", "after check_idcard");
            if (info == null) {
                if (result[0] == -5) {
                    disconnect_idcard();
                    throw new DeviceNotFoundException();
                }
                throw new TimeoutException();
            }
        }
        return info;
    }

    public static synchronized byte[] getIdCardImage() throws TelpoException {
        byte[] bArr;
        synchronized (TPS900IDCard.class) {
            bArr = get_image();
        }
        return bArr;
    }

    public static synchronized Bitmap decodeIdCardImage(byte[] image) throws TelpoException {
        Bitmap decodeFile;
        synchronized (TPS900IDCard.class) {
            if (image == null) {
                throw new ImageDecodeException();
            } else if (decode_image(image)) {
                try {
                    decodeFile = BitmapFactory.decodeFile(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + "/tps900/zp.bmp");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ImageDecodeException();
                }
            } else {
                throw new ImageDecodeException();
            }
        }
        return decodeFile;
    }

    public static synchronized void close() {
        synchronized (TPS900IDCard.class) {
            disconnect_idcard();
        }
    }
}
