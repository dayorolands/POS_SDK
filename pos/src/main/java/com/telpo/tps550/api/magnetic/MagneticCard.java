package com.telpo.tps550.api.magnetic;

import android.content.Context;
import android.util.Log;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.ErrorCode;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.TimeoutException;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.io.UnsupportedEncodingException;

public class MagneticCard {
    private static native int check_msr(int i, byte[] bArr);

    private static native void close_msr();

    private static native int open_msr();

    private static native int ready_for_read();

    public static synchronized void open() throws TelpoException {
        synchronized (MagneticCard.class) {
            if (!(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal())) {
                switch (open_msr()) {
                    case ErrorCode.ERR_LOW_POWER:
                        throw new InternalErrorException("Cannot open magnetic stripe card reader!");
                    case ErrorCode.ERR_INVALID_PARAM:
                        throw new DeviceAlreadyOpenException("The magnetic stripe card reader has been opened!");
                }
            }
        }
    }

    public static synchronized void open(Context context) throws TelpoException {
        synchronized (MagneticCard.class) {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                MagneticCard2.open(context);
            } else {
                switch (open_msr()) {
                    case ErrorCode.ERR_LOW_POWER:
                        throw new InternalErrorException("Cannot open magnetic stripe card reader!");
                    case ErrorCode.ERR_INVALID_PARAM:
                        throw new DeviceAlreadyOpenException("The magnetic stripe card reader has been opened!");
                }
            }
        }
    }

    public static synchronized void close() {
        synchronized (MagneticCard.class) {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                try {
                    MagneticCard2.close();
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            } else {
                close_msr();
            }
        }
        return;
    }

    public static synchronized String[] check(int timeout) throws TelpoException {
        String[] strArr;
        synchronized (MagneticCard.class) {
            if (SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900.ordinal() && SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                byte[] result = new byte[256];
                int ret = check_msr(timeout, result);
                Log.d("idcard demo", "after check_msr");
                Log.d("idcard demo", "ret is" + ret);
                switch (ret) {
                    case -4:
                        throw new TimeoutException("Timeout to get magnetic stripe card data!");
                    case ErrorCode.ERR_LOW_POWER:
                        throw new InternalErrorException("Cannot get magnetic stripe card data!");
                    case -1:
                        throw new DeviceNotOpenException("The magnetic stripe card reader has not been opened!");
                    default:
                        Log.d("idcard demo", "after switch");
                        strArr = ParseData(ret, result);
                        break;
                }
            } else if (MagneticCard2.check(timeout) != 0) {
                throw new TimeoutException("Timeout to get magnetic stripe card data!");
            } else {
                strArr = MagneticCard2.read();
            }
        }
        return strArr;
    }

    private static String[] ParseData(int size, byte[] data) throws TelpoException {
        Log.d("idcard demo", "parseData");
        String[] result = new String[3];
        byte len = data[0];
        if (len == 0) {
            result[0] = "";
        } else {
            try {
                result[0] = new String(data, 1, len, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new InternalErrorException();
            }
        }
        int pos = data[0] + 1;
        byte len2 = data[pos];
        if (len2 == 0) {
            result[1] = "";
        } else {
            try {
                result[1] = new String(data, pos + 1, len2, "GBK");
            } catch (UnsupportedEncodingException e2) {
                e2.printStackTrace();
                throw new InternalErrorException();
            }
        }
        int pos2 = pos + data[pos] + 1;
        byte len3 = data[pos2];
        if (len3 == 0) {
            result[2] = "";
        } else {
            try {
                result[2] = new String(data, pos2 + 1, len3, "GBK");
            } catch (UnsupportedEncodingException e3) {
                e3.printStackTrace();
                throw new InternalErrorException();
            }
        }
        return result;
    }

    public static int startReading() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            return 0;
        }
        return ready_for_read();
    }

    static {
        if (SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900.ordinal() && SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            System.loadLibrary("telpo_msr");
        }
    }
}
