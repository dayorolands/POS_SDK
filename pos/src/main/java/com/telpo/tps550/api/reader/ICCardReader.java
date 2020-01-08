package com.telpo.tps550.api.reader;

import android.content.Context;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.fingerprint.FingerPrint;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ICCardReader {
    private Context mContext = null;

    public ICCardReader(Context context) {
        this.mContext = context;
    }

    public synchronized void open(int slot) throws TelpoException {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            FingerPrint.iccardPower(1);
        }
        try {
            Class<?> iccard = Class.forName("com.common.sdk.iccard.ICCardServiceManager");
            Object obj = this.mContext.getSystemService("ICCard");
            Method method = iccard.getMethod("open", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(slot)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throwException(e3);
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void close(int slot) throws TelpoException {
        try {
            Class<?> iccard = Class.forName("com.common.sdk.iccard.ICCardServiceManager");
            Object obj = this.mContext.getSystemService("ICCard");
            Method method = iccard.getMethod("close", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(slot)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throwException(e3);
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized int detect(int slot, int timeout) throws TelpoException {
        int result;
        result = 0;
        try {
            Class<?> iccard = Class.forName("com.common.sdk.iccard.ICCardServiceManager");
            Object obj = this.mContext.getSystemService("ICCard");
            Method method = iccard.getMethod("detect", new Class[]{Integer.TYPE, Integer.TYPE});
            result = ((Integer) method.invoke(obj, new Object[]{Integer.valueOf(slot), Integer.valueOf(timeout)})).intValue();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throwException(e3);
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return result;
    }

    public synchronized void power_on(int slot) throws TelpoException {
        try {
            Class<?> iccard = Class.forName("com.common.sdk.iccard.ICCardServiceManager");
            Object obj = this.mContext.getSystemService("ICCard");
            Method method = iccard.getMethod("power_on", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(slot)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throwException(e3);
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String getAtr(int r11) throws com.telpo.tps550.api.TelpoException {
        /*
            r10 = this;
            monitor-enter(r10)
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            java.lang.String r6 = "com.common.sdk.iccard.ICCardServiceManager"
            java.lang.Class r2 = java.lang.Class.forName(r6)     // Catch:{ ClassNotFoundException -> 0x0035 }
            android.content.Context r6 = r10.mContext     // Catch:{ all -> 0x003f }
            java.lang.String r7 = "ICCard"
            java.lang.Object r4 = r6.getSystemService(r7)     // Catch:{ all -> 0x003f }
            java.lang.String r6 = "get_atr"
            r7 = 1
            java.lang.Class[] r7 = new java.lang.Class[r7]     // Catch:{ NoSuchMethodException -> 0x0042 }
            r8 = 0
            java.lang.Class r9 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0042 }
            r7[r8] = r9     // Catch:{ NoSuchMethodException -> 0x0042 }
            java.lang.reflect.Method r3 = r2.getMethod(r6, r7)     // Catch:{ NoSuchMethodException -> 0x0042 }
            r6 = 1
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch:{ IllegalArgumentException -> 0x004c, IllegalAccessException -> 0x0056, InvocationTargetException -> 0x0060 }
            r7 = 0
            java.lang.Integer r8 = java.lang.Integer.valueOf(r11)     // Catch:{ IllegalArgumentException -> 0x004c, IllegalAccessException -> 0x0056, InvocationTargetException -> 0x0060 }
            r6[r7] = r8     // Catch:{ IllegalArgumentException -> 0x004c, IllegalAccessException -> 0x0056, InvocationTargetException -> 0x0060 }
            java.lang.Object r6 = r3.invoke(r4, r6)     // Catch:{ IllegalArgumentException -> 0x004c, IllegalAccessException -> 0x0056, InvocationTargetException -> 0x0060 }
            r0 = r6
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ IllegalArgumentException -> 0x004c, IllegalAccessException -> 0x0056, InvocationTargetException -> 0x0060 }
            r5 = r0
        L_0x0033:
            monitor-exit(r10)
            return r5
        L_0x0035:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x003f }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x003f }
            r6.<init>()     // Catch:{ all -> 0x003f }
            throw r6     // Catch:{ all -> 0x003f }
        L_0x003f:
            r6 = move-exception
            monitor-exit(r10)
            throw r6
        L_0x0042:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x003f }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x003f }
            r6.<init>()     // Catch:{ all -> 0x003f }
            throw r6     // Catch:{ all -> 0x003f }
        L_0x004c:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x003f }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x003f }
            r6.<init>()     // Catch:{ all -> 0x003f }
            throw r6     // Catch:{ all -> 0x003f }
        L_0x0056:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x003f }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x003f }
            r6.<init>()     // Catch:{ all -> 0x003f }
            throw r6     // Catch:{ all -> 0x003f }
        L_0x0060:
            r1 = move-exception
            throwException(r1)     // Catch:{ all -> 0x003f }
            goto L_0x0033
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.reader.ICCardReader.getAtr(int):java.lang.String");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: byte[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized byte[] transmit(int r11, byte[] r12, int r13) throws com.telpo.tps550.api.TelpoException {
        /*
            r10 = this;
            monitor-enter(r10)
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            java.lang.String r6 = "com.common.sdk.iccard.ICCardServiceManager"
            java.lang.Class r2 = java.lang.Class.forName(r6)     // Catch:{ ClassNotFoundException -> 0x0049 }
            android.content.Context r6 = r10.mContext     // Catch:{ all -> 0x0053 }
            java.lang.String r7 = "ICCard"
            java.lang.Object r4 = r6.getSystemService(r7)     // Catch:{ all -> 0x0053 }
            java.lang.String r6 = "transmit"
            r7 = 3
            java.lang.Class[] r7 = new java.lang.Class[r7]     // Catch:{ NoSuchMethodException -> 0x0056 }
            r8 = 0
            java.lang.Class r9 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0056 }
            r7[r8] = r9     // Catch:{ NoSuchMethodException -> 0x0056 }
            r8 = 1
            java.lang.Class<byte[]> r9 = byte[].class
            r7[r8] = r9     // Catch:{ NoSuchMethodException -> 0x0056 }
            r8 = 2
            java.lang.Class r9 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0056 }
            r7[r8] = r9     // Catch:{ NoSuchMethodException -> 0x0056 }
            java.lang.reflect.Method r3 = r2.getMethod(r6, r7)     // Catch:{ NoSuchMethodException -> 0x0056 }
            r6 = 3
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r7 = 0
            java.lang.Integer r8 = java.lang.Integer.valueOf(r11)     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r6[r7] = r8     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r7 = 1
            r6[r7] = r12     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r7 = 2
            java.lang.Integer r8 = java.lang.Integer.valueOf(r13)     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r6[r7] = r8     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            java.lang.Object r6 = r3.invoke(r4, r6)     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r0 = r6
            byte[] r0 = (byte[]) r0     // Catch:{ IllegalArgumentException -> 0x0060, IllegalAccessException -> 0x006a, InvocationTargetException -> 0x0074 }
            r5 = r0
        L_0x0047:
            monitor-exit(r10)
            return r5
        L_0x0049:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0053 }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0053 }
            r6.<init>()     // Catch:{ all -> 0x0053 }
            throw r6     // Catch:{ all -> 0x0053 }
        L_0x0053:
            r6 = move-exception
            monitor-exit(r10)
            throw r6
        L_0x0056:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0053 }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0053 }
            r6.<init>()     // Catch:{ all -> 0x0053 }
            throw r6     // Catch:{ all -> 0x0053 }
        L_0x0060:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0053 }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0053 }
            r6.<init>()     // Catch:{ all -> 0x0053 }
            throw r6     // Catch:{ all -> 0x0053 }
        L_0x006a:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0053 }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0053 }
            r6.<init>()     // Catch:{ all -> 0x0053 }
            throw r6     // Catch:{ all -> 0x0053 }
        L_0x0074:
            r1 = move-exception
            throwException(r1)     // Catch:{ all -> 0x0053 }
            goto L_0x0047
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.reader.ICCardReader.transmit(int, byte[], int):byte[]");
    }

    public synchronized void power_off(int slot) throws TelpoException {
        try {
            Class<?> iccard = Class.forName("com.common.sdk.iccard.ICCardServiceManager");
            Object obj = this.mContext.getSystemService("ICCard");
            Method method = iccard.getMethod("power_off", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(slot)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throwException(e3);
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized int set_mode(int slot, int mode) throws TelpoException {
        int result;
        result = 0;
        try {
            Class<?> iccard = Class.forName("com.common.sdk.iccard.ICCardServiceManager");
            Object obj = this.mContext.getSystemService("ICCard");
            Method method = iccard.getMethod("set_mode", new Class[]{Integer.TYPE, Integer.TYPE});
            result = ((Integer) method.invoke(obj, new Object[]{Integer.valueOf(slot), Integer.valueOf(mode)})).intValue();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throwException(e3);
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
        return result;
    }

    private static synchronized void throwException(InvocationTargetException e) throws TelpoException {
        synchronized (ICCardReader.class) {
            throw new TelpoException();
        }
    }
}
