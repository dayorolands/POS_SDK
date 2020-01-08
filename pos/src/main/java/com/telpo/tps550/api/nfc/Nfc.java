package com.telpo.tps550.api.nfc;

import android.content.Context;
import android.util.Log;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Nfc {
    private Context mContext = null;
    private boolean openFlag = false;

    public Nfc(Context context) {
        this.mContext = context;
    }

    public synchronized void open() throws TelpoException {
        if (this.openFlag) {
            throw new DeviceAlreadyOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            nfccard.getMethod("open", new Class[0]).invoke(this.mContext.getSystemService("NFC"), new Object[0]);
            this.openFlag = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void close() throws TelpoException {
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            nfccard.getMethod("close", new Class[0]).invoke(this.mContext.getSystemService("NFC"), new Object[0]);
            this.openFlag = false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized byte[] activate(int timeOut) throws TelpoException {
        Object obj;
        Method method;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            obj = this.mContext.getSystemService("NFC");
            method = nfccard.getMethod("activate", new Class[]{Integer.TYPE});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) method.invoke(obj, new Object[]{Integer.valueOf(timeOut)});
    }

    public synchronized byte[] cpu_get_ats() throws TelpoException {
        Class<?> nfccard;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) nfccard.getMethod("cpu_get_ats", new Class[0]).invoke(this.mContext.getSystemService("NFC"), new Object[0]);
    }

    public synchronized byte[] transmit(byte[] sendBuffer, int sendBufferLength) throws TelpoException {
        Object obj;
        Method method;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            obj = this.mContext.getSystemService("NFC");
            method = nfccard.getMethod("cpu_transmit", new Class[]{byte[].class, Integer.TYPE});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) method.invoke(obj, new Object[]{sendBuffer, Integer.valueOf(sendBufferLength)});
    }

    public synchronized byte[] m1_read_block(byte noBlock) throws TelpoException {
        Object obj;
        Method method;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            obj = this.mContext.getSystemService("NFC");
            method = nfccard.getMethod("m1_read_block", new Class[]{Byte.TYPE});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) method.invoke(obj, new Object[]{Byte.valueOf(noBlock)});
    }

    public synchronized void m1_write_block(byte noBlock, byte[] inBuf, int inLen) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("m1_write_block", new Class[]{Byte.TYPE, byte[].class, Integer.TYPE});
            method.invoke(obj, new Object[]{Byte.valueOf(noBlock), inBuf, Integer.valueOf(inLen)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized byte[] m1_read_value(byte noBlock) throws TelpoException {
        Object obj;
        Method method;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            obj = this.mContext.getSystemService("NFC");
            method = nfccard.getMethod("m1_read_value", new Class[]{Byte.TYPE});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) method.invoke(obj, new Object[]{Byte.valueOf(noBlock)});
    }

    public synchronized void m1_write_value(byte noBlock, byte[] inBuf, int inLen) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("m1_write_value", new Class[]{Byte.TYPE, byte[].class, Integer.TYPE});
            method.invoke(obj, new Object[]{Byte.valueOf(noBlock), inBuf, Integer.valueOf(inLen)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void m1_increment(byte srcAddr, byte destAddr, byte[] inBuf, int inLen) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("m1_increment", new Class[]{Byte.TYPE, Byte.TYPE, byte[].class, Integer.TYPE});
            method.invoke(obj, new Object[]{Byte.valueOf(srcAddr), Byte.valueOf(destAddr), inBuf, Integer.valueOf(inLen)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void m1_decrement(byte srcAddr, byte destAddr, byte[] inBuf, int inLen) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("m1_decrement", new Class[]{Byte.TYPE, Byte.TYPE, byte[].class, Integer.TYPE});
            method.invoke(obj, new Object[]{Byte.valueOf(srcAddr), Byte.valueOf(destAddr), inBuf, Integer.valueOf(inLen)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void halt() throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            nfccard.getMethod("halt", new Class[0]).invoke(this.mContext.getSystemService("NFC"), new Object[0]);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void remove(int timeOut) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("remove", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(timeOut)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void m1_authenticate(byte noBlock, byte passwdType, byte[] passwd) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("m1_authenticate", new Class[]{Byte.TYPE, Byte.TYPE, byte[].class});
            method.invoke(obj, new Object[]{Byte.valueOf(noBlock), Byte.valueOf(passwdType), passwd});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized byte[] read_idcard(int timeOut) throws TelpoException {
        Object obj;
        Method method;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            obj = this.mContext.getSystemService("NFC");
            method = nfccard.getMethod("read_idcard", new Class[]{Integer.TYPE});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) method.invoke(obj, new Object[]{Integer.valueOf(timeOut)});
    }

    public synchronized void mp_sl0_personalization(byte[] keyBuf, int keyLen, byte[] dataBuf, int dataLen) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        for (int i = 0; i < keyBuf.length; i++) {
            Log.w("Nfc", "keyBuf = " + keyBuf[i]);
        }
        for (int j = 0; j < dataBuf.length; j++) {
            Log.w("Nfc", "dataBuf = " + dataBuf[j]);
        }
        Log.w("Nfc", "keyBuf len = " + keyBuf.length + "  keyLen = " + keyLen);
        Log.w("Nfc", "dataBuf len = " + dataBuf.length + "  dataLen = " + dataLen);
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("mp_sl0_personalization", new Class[]{byte[].class, Integer.TYPE, byte[].class, Integer.TYPE});
            method.invoke(obj, new Object[]{keyBuf, Integer.valueOf(keyLen), dataBuf, Integer.valueOf(dataLen)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            Exception targetExp = (Exception) e3.getTargetException();
            if (targetExp instanceof TelpoException) {
                throw ((TelpoException) targetExp);
            }
            throw new InternalErrorException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void mp_sl0_commit() throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            nfccard.getMethod("mp_sl0_commit", new Class[0]).invoke(this.mContext.getSystemService("NFC"), new Object[0]);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            Exception targetExp = (Exception) e3.getTargetException();
            if (targetExp instanceof TelpoException) {
                throw ((TelpoException) targetExp);
            }
            throw new InternalErrorException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void mp_sl1_switch_sl3(byte[] aesKey) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            nfccard.getMethod("mp_sl1_switch_sl3", new Class[]{byte[].class}).invoke(obj, new Object[]{aesKey});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            Exception targetExp = (Exception) e3.getTargetException();
            if (targetExp instanceof TelpoException) {
                throw ((TelpoException) targetExp);
            }
            throw new InternalErrorException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void mp_sl3_auth(byte noBlock, byte key, byte[] aesKey) throws TelpoException {
        Log.w("Nfc", "enter in the mp_sl3_auth");
        Log.w("Nfc", "noBlock = " + noBlock);
        Log.w("Nfc", "key = " + key);
        for (int i = 0; i < 16; i++) {
            Log.w("Nfc", "aesKey = " + aesKey[i]);
        }
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("mp_sl3_auth", new Class[]{Byte.TYPE, Byte.TYPE, byte[].class});
            method.invoke(obj, new Object[]{Byte.valueOf(noBlock), Byte.valueOf(key), aesKey});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("Nfc", "the mp_sl3_auth invoke fail");
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            Log.e("Nfc", "the mp_sl3_auth invoke fail 2");
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            Log.e("Nfc", "the mp_sl3_auth invoke fail 3");
            Exception targetExp = (Exception) e3.getTargetException();
            if (targetExp instanceof TelpoException) {
                Log.e("Nfc", "the mp_sl3_auth invoke fail 4");
                throw ((TelpoException) targetExp);
            } else {
                Log.e("Nfc", "the mp_sl3_auth invoke fail 5");
                throw new InternalErrorException();
            }
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            Log.e("Nfc", "the mp_sl3_auth method fail");
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            Log.e("Nfc", "mp_sl3_auth fail");
            throw new InternalErrorException();
        }
    }

    public synchronized byte[] mp_sl3_readblock_plain(byte noBlock) throws TelpoException {
        Object obj;
        Method method;
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            obj = this.mContext.getSystemService("NFC");
            method = nfccard.getMethod("mp_sl3_readblock_plain", new Class[]{Byte.TYPE});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            Exception targetExp = (Exception) e3.getTargetException();
            if (targetExp instanceof TelpoException) {
                throw ((TelpoException) targetExp);
            }
            throw new InternalErrorException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return (byte[]) method.invoke(obj, new Object[]{Byte.valueOf(noBlock)});
    }

    public synchronized void mp_sl3_writeblock_plain(byte noBlock, byte[] inBuf, int inLen) throws TelpoException {
        if (!this.openFlag) {
            throw new DeviceNotOpenException();
        }
        Log.w("Nfc", "enter in the mp_sl3_writeblock_plain");
        Log.w("Nfc", "noBlock = " + noBlock);
        try {
            Class<?> nfccard = Class.forName("com.common.sdk.nfc.NFCServiceManager");
            for (int i = 0; i < inLen; i++) {
                Log.w("Nfc", "inBuf = " + inBuf[i]);
            }
            Object obj = this.mContext.getSystemService("NFC");
            Method method = nfccard.getMethod("mp_sl3_writeblock_plain", new Class[]{Byte.TYPE, byte[].class, Integer.TYPE});
            method.invoke(obj, new Object[]{Byte.valueOf(noBlock), inBuf, Integer.valueOf(inLen)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            Exception targetExp = (Exception) e3.getTargetException();
            if (targetExp instanceof TelpoException) {
                throw ((TelpoException) targetExp);
            }
            throw new InternalErrorException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }
}
