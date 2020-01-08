package com.telpo.tps550.api.printer;

import android.content.Context;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UsbThermalPrinter {
    public static final int ALGIN_LEFT = 0;
    public static final int ALGIN_MIDDLE = 1;
    public static final int ALGIN_RIGHT = 2;
    public static final int DIRECTION_BACK = 1;
    public static final int DIRECTION_FORWORD = 0;
    public static final int STATUS_NO_PAPER = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OVER_FLOW = 3;
    public static final int STATUS_OVER_HEAT = 2;
    public static final int STATUS_UNKNOWN = 4;
    public static final int WALK_DOTLINE = 0;
    public static final int WALK_LINE = 1;
    private Context mContext = null;

    public UsbThermalPrinter(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0067, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0095, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x009e, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x009f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00a8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00a9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00aa, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00b2, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00b4, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00bc, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00bd, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00be, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0034, B:40:0x006d, B:45:0x007c, B:48:0x0088] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void start(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 != r5) goto L_0x006b
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "start"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0048 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0048 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
        L_0x0039:
            monitor-exit(r8)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0066:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        L_0x006b:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x009f }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "start"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00a9 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00a9 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00a9 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00a9 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0095, IllegalAccessException -> 0x00b3, InvocationTargetException -> 0x00bd }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x0095, IllegalAccessException -> 0x00b3, InvocationTargetException -> 0x00bd }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0095, IllegalAccessException -> 0x00b3, InvocationTargetException -> 0x00bd }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0095, IllegalAccessException -> 0x00b3, InvocationTargetException -> 0x00bd }
            goto L_0x0039
        L_0x0095:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x009f:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00a9:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00b3:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00bd:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.start(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0067, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0089, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0092, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0093, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0094, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009e, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00a8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0034, B:40:0x006d, B:45:0x007c, B:48:0x0083] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void reset() throws com.telpo.tps550.api.TelpoException {
        /*
            r6 = this;
            monitor-enter(r6)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 != r5) goto L_0x006b
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "reset"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0048 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0048 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
        L_0x0039:
            monitor-exit(r6)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0066:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        L_0x006b:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0093 }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "reset"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x009d }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x009d }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            goto L_0x0039
        L_0x0089:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0093:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x009d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00a7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00b1:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.reset():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void walkPaper(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "walkPaper"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "walkPaper"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.walkPaper(int):void");
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:10:0x0022=Splitter:B:10:0x0022, B:38:0x005d=Splitter:B:38:0x005d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void stop() {
        /*
            r6 = this;
            monitor-enter(r6)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0040 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0040 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0040 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0040 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0040 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0040 }
            if (r4 != r5) goto L_0x0057
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
        L_0x0022:
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0040 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0040 }
            java.lang.String r4 = "stop"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0043 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0043 }
        L_0x0033:
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0048, IllegalAccessException -> 0x004d, InvocationTargetException -> 0x0052 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0048, IllegalAccessException -> 0x004d, InvocationTargetException -> 0x0052 }
        L_0x0039:
            monitor-exit(r6)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0022
        L_0x0040:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0043:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0033
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0039
        L_0x004d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0039
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0039
        L_0x0057:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x007a }
        L_0x005d:
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0040 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0040 }
            java.lang.String r4 = "stop"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x007f }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x007f }
        L_0x006e:
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0075, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x0089 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0075, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x0089 }
            goto L_0x0039
        L_0x0075:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0039
        L_0x007a:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x005d
        L_0x007f:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x006e
        L_0x0084:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0039
        L_0x0089:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0040 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.stop():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0050, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0059, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005b, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0063, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0064, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0065, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006f, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0098, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00a1, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a3, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ab, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00ac, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ad, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00b5, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b6, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00b7, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00bf, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00c1, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001f, B:13:0x002e, B:16:0x0035, B:40:0x0075, B:45:0x0084, B:48:0x008b] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int checkStatus() throws com.telpo.tps550.api.TelpoException {
        /*
            r7 = this;
            monitor-enter(r7)
            r4 = 0
            r2 = 0
            r3 = 0
            r1 = 0
            int r5 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r6 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x004d }
            int r6 = r6.ordinal()     // Catch:{ all -> 0x004d }
            if (r5 == r6) goto L_0x001d
            int r5 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r6 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x004d }
            int r6 = r6.ordinal()     // Catch:{ all -> 0x004d }
            if (r5 != r6) goto L_0x0073
        L_0x001d:
            java.lang.String r5 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r4 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x0043 }
            android.content.Context r5 = r7.mContext     // Catch:{ all -> 0x004d }
            java.lang.String r6 = "ThermalPrinter"
            java.lang.Object r3 = r5.getSystemService(r6)     // Catch:{ all -> 0x004d }
            java.lang.String r5 = "checkStatus"
            r6 = 0
            java.lang.Class[] r6 = new java.lang.Class[r6]     // Catch:{ NoSuchMethodException -> 0x0050 }
            java.lang.reflect.Method r2 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x0050 }
            r5 = 0
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
            java.lang.Object r5 = r2.invoke(r3, r5)     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
            java.lang.Integer r5 = (java.lang.Integer) r5     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
            int r1 = r5.intValue()     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
        L_0x0041:
            monitor-exit(r7)
            return r1
        L_0x0043:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x004d:
            r5 = move-exception
            monitor-exit(r7)
            throw r5
        L_0x0050:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x005a:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x0064:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x006e:
            r0 = move-exception
            r7.throwException(r0)     // Catch:{ all -> 0x004d }
            goto L_0x0041
        L_0x0073:
            java.lang.String r5 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r4 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x0098 }
            android.content.Context r5 = r7.mContext     // Catch:{ all -> 0x004d }
            java.lang.String r6 = "UsbPrinter"
            java.lang.Object r3 = r5.getSystemService(r6)     // Catch:{ all -> 0x004d }
            java.lang.String r5 = "checkStatus"
            r6 = 0
            java.lang.Class[] r6 = new java.lang.Class[r6]     // Catch:{ NoSuchMethodException -> 0x00a2 }
            java.lang.reflect.Method r2 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x00a2 }
            r5 = 0
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ IllegalArgumentException -> 0x00ac, IllegalAccessException -> 0x00b6, InvocationTargetException -> 0x00c0 }
            java.lang.Object r5 = r2.invoke(r3, r5)     // Catch:{ IllegalArgumentException -> 0x00ac, IllegalAccessException -> 0x00b6, InvocationTargetException -> 0x00c0 }
            java.lang.Integer r5 = (java.lang.Integer) r5     // Catch:{ IllegalArgumentException -> 0x00ac, IllegalAccessException -> 0x00b6, InvocationTargetException -> 0x00c0 }
            int r1 = r5.intValue()     // Catch:{ IllegalArgumentException -> 0x00ac, IllegalAccessException -> 0x00b6, InvocationTargetException -> 0x00c0 }
            goto L_0x0041
        L_0x0098:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x00a2:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x00ac:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x00b6:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r5.<init>()     // Catch:{ all -> 0x004d }
            throw r5     // Catch:{ all -> 0x004d }
        L_0x00c0:
            r0 = move-exception
            r7.throwException(r0)     // Catch:{ all -> 0x004d }
            goto L_0x0041
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.checkStatus():int");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0053, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0060, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0069, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006b, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0073, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0074, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0075, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007f, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00c2, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00c3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00c4, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00cc, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00cd, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ce, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00d6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00d7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00e0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00e1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00e2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x003e, B:40:0x0085, B:45:0x0094, B:48:0x00a5] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void enlargeFontSize(int r9, int r10) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x005d }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x005d }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x005d }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x005d }
            if (r4 != r5) goto L_0x0083
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0053 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x005d }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x005d }
            java.lang.String r4 = "enlargeFontSize"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0060 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0060 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0060 }
            r6 = 1
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0060 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0060 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0060 }
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x006a, IllegalAccessException -> 0x0074, InvocationTargetException -> 0x007e }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x006a, IllegalAccessException -> 0x0074, InvocationTargetException -> 0x007e }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x006a, IllegalAccessException -> 0x0074, InvocationTargetException -> 0x007e }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x006a, IllegalAccessException -> 0x0074, InvocationTargetException -> 0x007e }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x006a, IllegalAccessException -> 0x0074, InvocationTargetException -> 0x007e }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x006a, IllegalAccessException -> 0x0074, InvocationTargetException -> 0x007e }
        L_0x0051:
            monitor-exit(r8)
            return
        L_0x0053:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x005d:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0060:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x006a:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x0074:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x007e:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x005d }
            goto L_0x0051
        L_0x0083:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00c3 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x005d }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x005d }
            java.lang.String r4 = "enlargeFontSize"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00cd }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00cd }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00cd }
            r6 = 1
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00cd }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00cd }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00cd }
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00b9, IllegalAccessException -> 0x00d7, InvocationTargetException -> 0x00e1 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00b9, IllegalAccessException -> 0x00d7, InvocationTargetException -> 0x00e1 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00b9, IllegalAccessException -> 0x00d7, InvocationTargetException -> 0x00e1 }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x00b9, IllegalAccessException -> 0x00d7, InvocationTargetException -> 0x00e1 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00b9, IllegalAccessException -> 0x00d7, InvocationTargetException -> 0x00e1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00b9, IllegalAccessException -> 0x00d7, InvocationTargetException -> 0x00e1 }
            goto L_0x0051
        L_0x00b9:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x00c3:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x00cd:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x00d7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x005d }
            r4.<init>()     // Catch:{ all -> 0x005d }
            throw r4     // Catch:{ all -> 0x005d }
        L_0x00e1:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x005d }
            goto L_0x0051
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.enlargeFontSize(int, int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setFontSize(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setFontSize"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setFontSize"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setFontSize(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        r0.printStackTrace();
        r3 = (java.lang.Exception) r0.getTargetException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007e, code lost:
        if ((r3 instanceof com.telpo.tps550.api.TelpoException) != false) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0082, code lost:
        throw ((com.telpo.tps550.api.TelpoException) r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ad, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00c0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00c1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c2, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00ca, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00cb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00cc, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00d4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00d5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00d6, code lost:
        r0.printStackTrace();
        r3 = (java.lang.Exception) r0.getTargetException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00e1, code lost:
        if ((r3 instanceof com.telpo.tps550.api.TelpoException) != false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00e5, code lost:
        throw ((com.telpo.tps550.api.TelpoException) r3);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:43:0x0085, B:48:0x0094, B:51:0x00a0] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setTextSize(int r10) throws com.telpo.tps550.api.TelpoException {
        /*
            r9 = this;
            monitor-enter(r9)
            r4 = 0
            r1 = 0
            r2 = 0
            int r5 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r6 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r6 = r6.ordinal()     // Catch:{ all -> 0x0051 }
            if (r5 == r6) goto L_0x001c
            int r5 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r6 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r6 = r6.ordinal()     // Catch:{ all -> 0x0051 }
            if (r5 != r6) goto L_0x0083
        L_0x001c:
            java.lang.String r5 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r4 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r5 = r9.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r6 = "ThermalPrinter"
            java.lang.Object r2 = r5.getSystemService(r6)     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "setTextSize"
            r6 = 1
            java.lang.Class[] r6 = new java.lang.Class[r6]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r7 = 0
            java.lang.Class r8 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6[r7] = r8     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5 = 1
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r6 = 0
            java.lang.Integer r7 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5[r6] = r7     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r5)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r9)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r5 = move-exception
            monitor-exit(r9)
            throw r5
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            java.lang.Throwable r3 = r0.getTargetException()     // Catch:{ all -> 0x0051 }
            java.lang.Exception r3 = (java.lang.Exception) r3     // Catch:{ all -> 0x0051 }
            boolean r5 = r3 instanceof com.telpo.tps550.api.TelpoException     // Catch:{ all -> 0x0051 }
            if (r5 == 0) goto L_0x0045
            com.telpo.tps550.api.TelpoException r3 = (com.telpo.tps550.api.TelpoException) r3     // Catch:{ all -> 0x0051 }
            throw r3     // Catch:{ all -> 0x0051 }
        L_0x0083:
            java.lang.String r5 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r4 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x00b7 }
            android.content.Context r5 = r9.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r6 = "UsbPrinter"
            java.lang.Object r2 = r5.getSystemService(r6)     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "setTextSize"
            r6 = 1
            java.lang.Class[] r6 = new java.lang.Class[r6]     // Catch:{ NoSuchMethodException -> 0x00c1 }
            r7 = 0
            java.lang.Class r8 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00c1 }
            r6[r7] = r8     // Catch:{ NoSuchMethodException -> 0x00c1 }
            java.lang.reflect.Method r1 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x00c1 }
            r5 = 1
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            r6 = 0
            java.lang.Integer r7 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            r5[r6] = r7     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            r1.invoke(r2, r5)     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            goto L_0x0045
        L_0x00ad:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00b7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00c1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00cb:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00d5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            java.lang.Throwable r3 = r0.getTargetException()     // Catch:{ all -> 0x0051 }
            java.lang.Exception r3 = (java.lang.Exception) r3     // Catch:{ all -> 0x0051 }
            boolean r5 = r3 instanceof com.telpo.tps550.api.TelpoException     // Catch:{ all -> 0x0051 }
            if (r5 == 0) goto L_0x0045
            com.telpo.tps550.api.TelpoException r3 = (com.telpo.tps550.api.TelpoException) r3     // Catch:{ all -> 0x0051 }
            throw r3     // Catch:{ all -> 0x0051 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setTextSize(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        r0.printStackTrace();
        r3 = (java.lang.Exception) r0.getTargetException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007e, code lost:
        if ((r3 instanceof com.telpo.tps550.api.TelpoException) != false) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0082, code lost:
        throw ((com.telpo.tps550.api.TelpoException) r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ad, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00c0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00c1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c2, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00ca, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00cb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00cc, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00d4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00d5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00d6, code lost:
        r0.printStackTrace();
        r3 = (java.lang.Exception) r0.getTargetException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00e1, code lost:
        if ((r3 instanceof com.telpo.tps550.api.TelpoException) != false) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00e5, code lost:
        throw ((com.telpo.tps550.api.TelpoException) r3);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:43:0x0085, B:48:0x0094, B:51:0x00a0] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setUnderline(boolean r10) throws com.telpo.tps550.api.TelpoException {
        /*
            r9 = this;
            monitor-enter(r9)
            r4 = 0
            r1 = 0
            r2 = 0
            int r5 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r6 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r6 = r6.ordinal()     // Catch:{ all -> 0x0051 }
            if (r5 == r6) goto L_0x001c
            int r5 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r6 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r6 = r6.ordinal()     // Catch:{ all -> 0x0051 }
            if (r5 != r6) goto L_0x0083
        L_0x001c:
            java.lang.String r5 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r4 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r5 = r9.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r6 = "ThermalPrinter"
            java.lang.Object r2 = r5.getSystemService(r6)     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "setUnderline"
            r6 = 1
            java.lang.Class[] r6 = new java.lang.Class[r6]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r7 = 0
            java.lang.Class r8 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6[r7] = r8     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5 = 1
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r6 = 0
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5[r6] = r7     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r5)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r9)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r5 = move-exception
            monitor-exit(r9)
            throw r5
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            java.lang.Throwable r3 = r0.getTargetException()     // Catch:{ all -> 0x0051 }
            java.lang.Exception r3 = (java.lang.Exception) r3     // Catch:{ all -> 0x0051 }
            boolean r5 = r3 instanceof com.telpo.tps550.api.TelpoException     // Catch:{ all -> 0x0051 }
            if (r5 == 0) goto L_0x0045
            com.telpo.tps550.api.TelpoException r3 = (com.telpo.tps550.api.TelpoException) r3     // Catch:{ all -> 0x0051 }
            throw r3     // Catch:{ all -> 0x0051 }
        L_0x0083:
            java.lang.String r5 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r4 = java.lang.Class.forName(r5)     // Catch:{ ClassNotFoundException -> 0x00b7 }
            android.content.Context r5 = r9.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r6 = "UsbPrinter"
            java.lang.Object r2 = r5.getSystemService(r6)     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "setUnderline"
            r6 = 1
            java.lang.Class[] r6 = new java.lang.Class[r6]     // Catch:{ NoSuchMethodException -> 0x00c1 }
            r7 = 0
            java.lang.Class r8 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x00c1 }
            r6[r7] = r8     // Catch:{ NoSuchMethodException -> 0x00c1 }
            java.lang.reflect.Method r1 = r4.getMethod(r5, r6)     // Catch:{ NoSuchMethodException -> 0x00c1 }
            r5 = 1
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            r6 = 0
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            r5[r6] = r7     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            r1.invoke(r2, r5)     // Catch:{ IllegalArgumentException -> 0x00ad, IllegalAccessException -> 0x00cb, InvocationTargetException -> 0x00d5 }
            goto L_0x0045
        L_0x00ad:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00b7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00c1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00cb:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r5 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r5.<init>()     // Catch:{ all -> 0x0051 }
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x00d5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            java.lang.Throwable r3 = r0.getTargetException()     // Catch:{ all -> 0x0051 }
            java.lang.Exception r3 = (java.lang.Exception) r3     // Catch:{ all -> 0x0051 }
            boolean r5 = r3 instanceof com.telpo.tps550.api.TelpoException     // Catch:{ all -> 0x0051 }
            if (r5 == 0) goto L_0x0045
            com.telpo.tps550.api.TelpoException r3 = (com.telpo.tps550.api.TelpoException) r3     // Catch:{ all -> 0x0051 }
            throw r3     // Catch:{ all -> 0x0051 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setUnderline(boolean):void");
    }

    public synchronized void setMonoSpace(boolean isMonoSpace) throws TelpoException {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                Class<?> thermalPrinter = Class.forName("com.common.sdk.thermalprinter.ThermalPrinterServiceManager");
                Object obj = this.mContext.getSystemService("ThermalPrinter");
                Method method = thermalPrinter.getMethod("setMonoSpace", new Class[]{Boolean.TYPE});
                method.invoke(obj, new Object[]{Boolean.valueOf(isMonoSpace)});
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
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
                throw new InternalErrorException();
            } catch (ClassNotFoundException e5) {
                e5.printStackTrace();
                throw new InternalErrorException();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setHighlight(boolean r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setHighlight"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setHighlight"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setHighlight(boolean):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setGray(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setGray"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setGray"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setGray(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setAlgin(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setAlgin"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setAlgin"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setAlgin(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0050, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0059, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005b, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0063, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0064, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0065, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006f, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0099, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00a2, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00a3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a4, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ac, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00ad, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ae, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00b6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00b8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00c2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0075, B:45:0x0084, B:48:0x0090] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void addString(java.lang.String r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x004d }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x004d }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x004d }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x004d }
            if (r4 != r5) goto L_0x0073
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0043 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x004d }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x004d }
            java.lang.String r4 = "addString"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0050 }
            r6 = 0
            java.lang.Class<java.lang.String> r7 = java.lang.String.class
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0050 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0050 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
            r5 = 0
            r4[r5] = r9     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005a, IllegalAccessException -> 0x0064, InvocationTargetException -> 0x006e }
        L_0x0041:
            monitor-exit(r8)
            return
        L_0x0043:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x004d:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0050:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x005a:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x0064:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x006e:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x004d }
            goto L_0x0041
        L_0x0073:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00a3 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x004d }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x004d }
            java.lang.String r4 = "addString"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00ad }
            r6 = 0
            java.lang.Class<java.lang.String> r7 = java.lang.String.class
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00ad }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00ad }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0099, IllegalAccessException -> 0x00b7, InvocationTargetException -> 0x00c1 }
            r5 = 0
            r4[r5] = r9     // Catch:{ IllegalArgumentException -> 0x0099, IllegalAccessException -> 0x00b7, InvocationTargetException -> 0x00c1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0099, IllegalAccessException -> 0x00b7, InvocationTargetException -> 0x00c1 }
            goto L_0x0041
        L_0x0099:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x00a3:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x00ad:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x00b7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x004d }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004d }
            r4.<init>()     // Catch:{ all -> 0x004d }
            throw r4     // Catch:{ all -> 0x004d }
        L_0x00c1:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x004d }
            goto L_0x0041
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.addString(java.lang.String):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0058, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0067, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0070, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0071, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0079, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007b, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ba, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00bb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00bc, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00c4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ce, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00cf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00da, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x003e, B:40:0x0081, B:45:0x0090, B:48:0x00a1] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void addStringOffset(int r9, java.lang.String r10) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0059 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0059 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0059 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0059 }
            if (r4 != r5) goto L_0x007f
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x004f }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0059 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0059 }
            java.lang.String r4 = "addStringOffset"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x005c }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x005c }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x005c }
            r6 = 1
            java.lang.Class<java.lang.String> r7 = java.lang.String.class
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x005c }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x005c }
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r5 = 1
            r4[r5] = r10     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
        L_0x004d:
            monitor-exit(r8)
            return
        L_0x004f:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x0059:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x0066:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x0070:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x007a:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0059 }
            goto L_0x004d
        L_0x007f:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00bb }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0059 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0059 }
            java.lang.String r4 = "addStringOffset"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r6 = 1
            java.lang.Class<java.lang.String> r7 = java.lang.String.class
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00c5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r5 = 1
            r4[r5] = r10     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            goto L_0x004d
        L_0x00b1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00bb:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00c5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00cf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00d9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0059 }
            goto L_0x004d
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.addStringOffset(int, java.lang.String):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0067, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0089, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0092, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0093, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0094, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009e, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00a8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0034, B:40:0x006d, B:45:0x007c, B:48:0x0083] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void endLine() throws com.telpo.tps550.api.TelpoException {
        /*
            r6 = this;
            monitor-enter(r6)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 != r5) goto L_0x006b
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "endLine"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0048 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0048 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
        L_0x0039:
            monitor-exit(r6)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0066:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        L_0x006b:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0093 }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "endLine"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x009d }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x009d }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            goto L_0x0039
        L_0x0089:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0093:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x009d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00a7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00b1:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.endLine():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0067, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0089, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0092, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0093, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0094, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009e, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00a8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0034, B:40:0x006d, B:45:0x007c, B:48:0x0083] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void clearString() throws com.telpo.tps550.api.TelpoException {
        /*
            r6 = this;
            monitor-enter(r6)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 != r5) goto L_0x006b
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "clearString"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0048 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0048 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
        L_0x0039:
            monitor-exit(r6)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0066:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        L_0x006b:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0093 }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "clearString"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x009d }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x009d }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            goto L_0x0039
        L_0x0089:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0093:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x009d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00a7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00b1:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.clearString():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0067, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0089, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0092, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0093, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0094, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009e, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00a8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0034, B:40:0x006d, B:45:0x007c, B:48:0x0083] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void printString() throws com.telpo.tps550.api.TelpoException {
        /*
            r6 = this;
            monitor-enter(r6)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 != r5) goto L_0x006b
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "printString"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0048 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0048 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
        L_0x0039:
            monitor-exit(r6)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0066:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        L_0x006b:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0093 }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "printString"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x009d }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x009d }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            goto L_0x0039
        L_0x0089:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0093:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x009d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00a7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00b1:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.printString():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0068, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0075, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0076, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0077, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x007f, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0080, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0081, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0089, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x008b, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00da, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00db, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00dc, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ee, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ef, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00f8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00fa, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0043, B:40:0x0091, B:45:0x00a0, B:48:0x00b6] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void printStringAndWalk(int r9, int r10, int r11) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0069 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0069 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0069 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0069 }
            if (r4 != r5) goto L_0x008f
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x005f }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0069 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0069 }
            java.lang.String r4 = "printStringAndWalk"
            r5 = 3
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x006c }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x006c }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x006c }
            r6 = 1
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x006c }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x006c }
            r6 = 2
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x006c }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x006c }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x006c }
            r4 = 3
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r5 = 2
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0076, IllegalAccessException -> 0x0080, InvocationTargetException -> 0x008a }
        L_0x005d:
            monitor-exit(r8)
            return
        L_0x005f:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x0069:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x006c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x0076:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x0080:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x008a:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0069 }
            goto L_0x005d
        L_0x008f:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00db }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0069 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0069 }
            java.lang.String r4 = "printStringAndWalk"
            r5 = 3
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r6 = 1
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r6 = 2
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00e5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00e5 }
            r4 = 3
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r5 = 2
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00d1, IllegalAccessException -> 0x00ef, InvocationTargetException -> 0x00f9 }
            goto L_0x005d
        L_0x00d1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x00db:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x00e5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x00ef:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0069 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0069 }
            r4.<init>()     // Catch:{ all -> 0x0069 }
            throw r4     // Catch:{ all -> 0x0069 }
        L_0x00f9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0069 }
            goto L_0x005d
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.printStringAndWalk(int, int, int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setLineSpace(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setLineSpace"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setLineSpace"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setLineSpace(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setLeftIndent(int r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setLeftIndent"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setLeftIndent"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setLeftIndent(int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0058, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0067, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0070, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0071, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0079, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007b, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00ba, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00bb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00bc, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00c4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ce, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00cf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00da, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x003e, B:40:0x0081, B:45:0x0090, B:48:0x00a1] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void printLogo(android.graphics.Bitmap r9, boolean r10) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0059 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0059 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0059 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0059 }
            if (r4 != r5) goto L_0x007f
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x004f }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0059 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0059 }
            java.lang.String r4 = "printLogo"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x005c }
            r6 = 0
            java.lang.Class<android.graphics.Bitmap> r7 = android.graphics.Bitmap.class
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x005c }
            r6 = 1
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x005c }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x005c }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x005c }
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r5 = 0
            r4[r5] = r9     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r5 = 1
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0066, IllegalAccessException -> 0x0070, InvocationTargetException -> 0x007a }
        L_0x004d:
            monitor-exit(r8)
            return
        L_0x004f:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x0059:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x0066:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x0070:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x007a:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0059 }
            goto L_0x004d
        L_0x007f:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00bb }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0059 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0059 }
            java.lang.String r4 = "printLogo"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r6 = 0
            java.lang.Class<android.graphics.Bitmap> r7 = android.graphics.Bitmap.class
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r6 = 1
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00c5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00c5 }
            r4 = 2
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r5 = 0
            r4[r5] = r9     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r5 = 1
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00b1, IllegalAccessException -> 0x00cf, InvocationTargetException -> 0x00d9 }
            goto L_0x004d
        L_0x00b1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00bb:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00c5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00cf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0059 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0059 }
            r4.<init>()     // Catch:{ all -> 0x0059 }
            throw r4     // Catch:{ all -> 0x0059 }
        L_0x00d9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0059 }
            goto L_0x004d
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.printLogo(android.graphics.Bitmap, boolean):void");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v11, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v22, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.lang.String} */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0041, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004a, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0057, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0058, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0059, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0061, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0062, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0063, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006d, code lost:
        throwException(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0094, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x009d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x009e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x009f, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00a7, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00a8, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00a9, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00b1, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b2, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00b3, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00bb, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00bc, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00bd, code lost:
        throwException(r1);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001f, B:13:0x002e, B:16:0x0035, B:40:0x0073, B:45:0x0082, B:48:0x0089] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String getVersion() throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r4 = 0
            r2 = 0
            r3 = 0
            r5 = 0
            int r6 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r7 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x004b }
            int r7 = r7.ordinal()     // Catch:{ all -> 0x004b }
            if (r6 == r7) goto L_0x001d
            int r6 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r7 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x004b }
            int r7 = r7.ordinal()     // Catch:{ all -> 0x004b }
            if (r6 != r7) goto L_0x0071
        L_0x001d:
            java.lang.String r6 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r4 = java.lang.Class.forName(r6)     // Catch:{ ClassNotFoundException -> 0x0041 }
            android.content.Context r6 = r8.mContext     // Catch:{ all -> 0x004b }
            java.lang.String r7 = "ThermalPrinter"
            java.lang.Object r3 = r6.getSystemService(r7)     // Catch:{ all -> 0x004b }
            java.lang.String r6 = "getVersion"
            r7 = 0
            java.lang.Class[] r7 = new java.lang.Class[r7]     // Catch:{ NoSuchMethodException -> 0x004e }
            java.lang.reflect.Method r2 = r4.getMethod(r6, r7)     // Catch:{ NoSuchMethodException -> 0x004e }
            r6 = 0
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch:{ IllegalArgumentException -> 0x0058, IllegalAccessException -> 0x0062, InvocationTargetException -> 0x006c }
            java.lang.Object r6 = r2.invoke(r3, r6)     // Catch:{ IllegalArgumentException -> 0x0058, IllegalAccessException -> 0x0062, InvocationTargetException -> 0x006c }
            r0 = r6
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ IllegalArgumentException -> 0x0058, IllegalAccessException -> 0x0062, InvocationTargetException -> 0x006c }
            r5 = r0
        L_0x003f:
            monitor-exit(r8)
            return r5
        L_0x0041:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x004b:
            r6 = move-exception
            monitor-exit(r8)
            throw r6
        L_0x004e:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x0058:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x0062:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x006c:
            r1 = move-exception
            r8.throwException(r1)     // Catch:{ all -> 0x004b }
            goto L_0x003f
        L_0x0071:
            java.lang.String r6 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r4 = java.lang.Class.forName(r6)     // Catch:{ ClassNotFoundException -> 0x0094 }
            android.content.Context r6 = r8.mContext     // Catch:{ all -> 0x004b }
            java.lang.String r7 = "UsbPrinter"
            java.lang.Object r3 = r6.getSystemService(r7)     // Catch:{ all -> 0x004b }
            java.lang.String r6 = "getVersion"
            r7 = 0
            java.lang.Class[] r7 = new java.lang.Class[r7]     // Catch:{ NoSuchMethodException -> 0x009e }
            java.lang.reflect.Method r2 = r4.getMethod(r6, r7)     // Catch:{ NoSuchMethodException -> 0x009e }
            r6 = 0
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x00b2, InvocationTargetException -> 0x00bc }
            java.lang.Object r6 = r2.invoke(r3, r6)     // Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x00b2, InvocationTargetException -> 0x00bc }
            r0 = r6
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ IllegalArgumentException -> 0x00a8, IllegalAccessException -> 0x00b2, InvocationTargetException -> 0x00bc }
            r5 = r0
            goto L_0x003f
        L_0x0094:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x009e:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x00a8:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x00b2:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x004b }
            com.telpo.tps550.api.InternalErrorException r6 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x004b }
            r6.<init>()     // Catch:{ all -> 0x004b }
            throw r6     // Catch:{ all -> 0x004b }
        L_0x00bc:
            r1 = move-exception
            r8.throwException(r1)     // Catch:{ all -> 0x004b }
            goto L_0x003f
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.getVersion():java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0060, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0069, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0076, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0077, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0078, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0080, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0081, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0082, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x008a, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x008c, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00dc, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00dd, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00de, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f2, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00fa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00fb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00fc, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0043, B:40:0x0092, B:45:0x00a1, B:48:0x00b7] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void searchMark(int r9, int r10) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x006a }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x006a }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x006a }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x006a }
            if (r4 != r5) goto L_0x0090
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0060 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x006a }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x006a }
            java.lang.String r4 = "searchMark"
            r5 = 3
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x006d }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x006d }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x006d }
            r6 = 1
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x006d }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x006d }
            r6 = 2
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x006d }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x006d }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x006d }
            r4 = 3
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r5 = 0
            r6 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r5 = 2
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0077, IllegalAccessException -> 0x0081, InvocationTargetException -> 0x008b }
        L_0x005e:
            monitor-exit(r8)
            return
        L_0x0060:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x006a:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x006d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x0077:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x0081:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x008b:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x006a }
            goto L_0x005e
        L_0x0090:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00dd }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x006a }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x006a }
            java.lang.String r4 = "searchMark"
            r5 = 3
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r6 = 0
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r6 = 1
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r6 = 2
            java.lang.Class r7 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00e7 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00e7 }
            r4 = 3
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r5 = 0
            r6 = 0
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r5 = 2
            java.lang.Integer r6 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00d3, IllegalAccessException -> 0x00f1, InvocationTargetException -> 0x00fb }
            goto L_0x005e
        L_0x00d3:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x00dd:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x00e7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x00f1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x006a }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006a }
            r4.<init>()     // Catch:{ all -> 0x006a }
            throw r4     // Catch:{ all -> 0x006a }
        L_0x00fb:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x006a }
            goto L_0x005e
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.searchMark(int, int):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0044, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0048, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0051, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0053, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005b, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005d, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0065, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0067, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0089, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0092, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0093, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0094, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x009c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x009d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009e, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a6, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00a7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00a8, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b0, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b2, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0034, B:40:0x006d, B:45:0x007c, B:48:0x0083] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void paperCut() throws com.telpo.tps550.api.TelpoException {
        /*
            r6 = this;
            monitor-enter(r6)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0045 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0045 }
            if (r4 != r5) goto L_0x006b
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x003b }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "paperCut"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0048 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0048 }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0052, IllegalAccessException -> 0x005c, InvocationTargetException -> 0x0066 }
        L_0x0039:
            monitor-exit(r6)
            return
        L_0x003b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0045:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0048:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0052:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x005c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0066:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        L_0x006b:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0093 }
            android.content.Context r4 = r6.mContext     // Catch:{ all -> 0x0045 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0045 }
            java.lang.String r4 = "paperCut"
            r5 = 0
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x009d }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x009d }
            r4 = 0
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x0089, IllegalAccessException -> 0x00a7, InvocationTargetException -> 0x00b1 }
            goto L_0x0039
        L_0x0089:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x0093:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x009d:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00a7:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0045 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0045 }
            r4.<init>()     // Catch:{ all -> 0x0045 }
            throw r4     // Catch:{ all -> 0x0045 }
        L_0x00b1:
            r0 = move-exception
            r6.throwException(r0)     // Catch:{ all -> 0x0045 }
            goto L_0x0039
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.paperCut():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setBold(boolean r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setBold"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setBold"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.setBold(boolean):void");
    }

    public int measureText(String text) throws TelpoException {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                Class<?> thermalPrinter = Class.forName("com.common.sdk.thermalprinter.ThermalPrinterServiceManager");
                Object obj = this.mContext.getSystemService("ThermalPrinter");
                try {
                    try {
                        return ((Integer) thermalPrinter.getMethod("measureText", new Class[]{String.class}).invoke(obj, new Object[]{text})).intValue();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        throw new InternalErrorException();
                    } catch (IllegalAccessException e2) {
                        e2.printStackTrace();
                        throw new InternalErrorException();
                    } catch (InvocationTargetException e3) {
                        throwException(e3);
                        return -1;
                    }
                } catch (NoSuchMethodException e4) {
                    e4.printStackTrace();
                    throw new InternalErrorException();
                }
            } catch (ClassNotFoundException e5) {
                e5.printStackTrace();
                throw new InternalErrorException();
            }
        } else {
            try {
                Class<?> thermalPrinter2 = Class.forName("com.common.sdk.printer.UsbPrinterManager");
                Object obj2 = this.mContext.getSystemService("UsbPrinter");
                try {
                    try {
                        return ((Integer) thermalPrinter2.getMethod("measureText", new Class[]{String.class}).invoke(obj2, new Object[]{text})).intValue();
                    } catch (IllegalArgumentException e6) {
                        e6.printStackTrace();
                        throw new InternalErrorException();
                    } catch (IllegalAccessException e7) {
                        e7.printStackTrace();
                        throw new InternalErrorException();
                    } catch (InvocationTargetException e8) {
                        throwException(e8);
                        return -1;
                    }
                } catch (NoSuchMethodException e9) {
                    e9.printStackTrace();
                    throw new InternalErrorException();
                }
            } catch (ClassNotFoundException e10) {
                e10.printStackTrace();
                throw new InternalErrorException();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0050, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0054, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005f, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0067, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0068, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0072, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0073, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00aa, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00ab, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ac, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00be, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c0, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c8, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ca, code lost:
        throwException(r0);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:8:0x001e, B:13:0x002d, B:16:0x0039, B:40:0x0079, B:45:0x0088, B:48:0x0094] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void autoBreakSet(boolean r9) throws com.telpo.tps550.api.TelpoException {
        /*
            r8 = this;
            monitor-enter(r8)
            r3 = 0
            r1 = 0
            r2 = 0
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 == r5) goto L_0x001c
            int r4 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r5 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900MB     // Catch:{ all -> 0x0051 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x0051 }
            if (r4 != r5) goto L_0x0077
        L_0x001c:
            java.lang.String r4 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x0047 }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "ThermalPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setAutoBreak"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x0054 }
            r6 = 0
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x0054 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x0054 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x0054 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r5 = 0
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x005e, IllegalAccessException -> 0x0068, InvocationTargetException -> 0x0072 }
        L_0x0045:
            monitor-exit(r8)
            return
        L_0x0047:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0054:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x005e:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0068:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x0072:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        L_0x0077:
            java.lang.String r4 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r3 = java.lang.Class.forName(r4)     // Catch:{ ClassNotFoundException -> 0x00ab }
            android.content.Context r4 = r8.mContext     // Catch:{ all -> 0x0051 }
            java.lang.String r5 = "UsbPrinter"
            java.lang.Object r2 = r4.getSystemService(r5)     // Catch:{ all -> 0x0051 }
            java.lang.String r4 = "setAutoBreak"
            r5 = 1
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r6 = 0
            java.lang.Class r7 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r5[r6] = r7     // Catch:{ NoSuchMethodException -> 0x00b5 }
            java.lang.reflect.Method r1 = r3.getMethod(r4, r5)     // Catch:{ NoSuchMethodException -> 0x00b5 }
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r5 = 0
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r9)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r4[r5] = r6     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            r1.invoke(r2, r4)     // Catch:{ IllegalArgumentException -> 0x00a1, IllegalAccessException -> 0x00bf, InvocationTargetException -> 0x00c9 }
            goto L_0x0045
        L_0x00a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00ab:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00b5:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0051 }
            com.telpo.tps550.api.InternalErrorException r4 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x0051 }
            r4.<init>()     // Catch:{ all -> 0x0051 }
            throw r4     // Catch:{ all -> 0x0051 }
        L_0x00c9:
            r0 = move-exception
            r8.throwException(r0)     // Catch:{ all -> 0x0051 }
            goto L_0x0045
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.autoBreakSet(boolean):void");
    }

    public synchronized void setThripleHeight(boolean isThripleHeight) throws TelpoException {
        try {
            Class<?> thermalPrinter = Class.forName("com.common.sdk.printer.UsbPrinterManager");
            Object obj = this.mContext.getSystemService("UsbPrinter");
            Method method = thermalPrinter.getMethod("setTripleHeight", new Class[]{Boolean.TYPE});
            method.invoke(obj, new Object[]{Boolean.valueOf(isThripleHeight)});
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

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0063, code lost:
        r11 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r11.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006c, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0070, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r1.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0079, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007a, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007b, code lost:
        r8.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0083, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0084, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0085, code lost:
        r9.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008d, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x008e, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008f, code lost:
        throwException(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d8, code lost:
        r12 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r12.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e1, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e2, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00e3, code lost:
        r7.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00eb, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00ec, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ed, code lost:
        r6.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f5, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00f6, code lost:
        r13 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f7, code lost:
        r13.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00ff, code lost:
        throw new com.telpo.tps550.api.InternalErrorException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0100, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0101, code lost:
        throwException(r5);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:6:0x0012, B:12:0x0023, B:15:0x0042, B:39:0x0097, B:45:0x00a8, B:48:0x00c1] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void printLogoRaw(byte[] r19, int r20, int r21) throws com.telpo.tps550.api.TelpoException {
        /*
            r18 = this;
            monitor-enter(r18)
            r4 = 0
            r2 = 0
            r3 = 0
            int r14 = com.telpo.tps550.api.util.SystemUtil.getDeviceType()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.util.StringUtil$DeviceModelEnum r15 = com.telpo.tps550.api.util.StringUtil.DeviceModelEnum.TPS900     // Catch:{ all -> 0x006d }
            int r15 = r15.ordinal()     // Catch:{ all -> 0x006d }
            if (r14 != r15) goto L_0x0095
            java.lang.String r14 = "com.common.sdk.thermalprinter.ThermalPrinterServiceManager"
            java.lang.Class r4 = java.lang.Class.forName(r14)     // Catch:{ ClassNotFoundException -> 0x0063 }
            r0 = r18
            android.content.Context r14 = r0.mContext     // Catch:{ all -> 0x006d }
            java.lang.String r15 = "ThermalPrinter"
            java.lang.Object r3 = r14.getSystemService(r15)     // Catch:{ all -> 0x006d }
            java.lang.String r14 = "printLogoRaw"
            r15 = 4
            java.lang.Class[] r15 = new java.lang.Class[r15]     // Catch:{ NoSuchMethodException -> 0x0070 }
            r16 = 0
            java.lang.Class<byte[]> r17 = byte[].class
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x0070 }
            r16 = 1
            java.lang.Class r17 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0070 }
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x0070 }
            r16 = 2
            java.lang.Class r17 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x0070 }
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x0070 }
            r16 = 3
            java.lang.Class r17 = java.lang.Boolean.TYPE     // Catch:{ NoSuchMethodException -> 0x0070 }
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x0070 }
            java.lang.reflect.Method r2 = r4.getMethod(r14, r15)     // Catch:{ NoSuchMethodException -> 0x0070 }
            r14 = 4
            java.lang.Object[] r14 = new java.lang.Object[r14]     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r15 = 0
            r14[r15] = r19     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r15 = 1
            java.lang.Integer r16 = java.lang.Integer.valueOf(r20)     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r14[r15] = r16     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r15 = 2
            java.lang.Integer r16 = java.lang.Integer.valueOf(r21)     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r14[r15] = r16     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r15 = 3
            r16 = 0
            java.lang.Boolean r16 = java.lang.Boolean.valueOf(r16)     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r14[r15] = r16     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
            r2.invoke(r3, r14)     // Catch:{ IllegalArgumentException -> 0x007a, IllegalAccessException -> 0x0084, InvocationTargetException -> 0x008e }
        L_0x0061:
            monitor-exit(r18)
            return
        L_0x0063:
            r11 = move-exception
            r11.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x006d:
            r14 = move-exception
            monitor-exit(r18)
            throw r14
        L_0x0070:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x007a:
            r8 = move-exception
            r8.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x0084:
            r9 = move-exception
            r9.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x008e:
            r10 = move-exception
            r0 = r18
            r0.throwException(r10)     // Catch:{ all -> 0x006d }
            goto L_0x0061
        L_0x0095:
            java.lang.String r14 = "com.common.sdk.printer.UsbPrinterManager"
            java.lang.Class r4 = java.lang.Class.forName(r14)     // Catch:{ ClassNotFoundException -> 0x00e2 }
            r0 = r18
            android.content.Context r14 = r0.mContext     // Catch:{ all -> 0x006d }
            java.lang.String r15 = "UsbPrinter"
            java.lang.Object r3 = r14.getSystemService(r15)     // Catch:{ all -> 0x006d }
            java.lang.String r14 = "printLogoRaw"
            r15 = 3
            java.lang.Class[] r15 = new java.lang.Class[r15]     // Catch:{ NoSuchMethodException -> 0x00ec }
            r16 = 0
            java.lang.Class<byte[]> r17 = byte[].class
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x00ec }
            r16 = 1
            java.lang.Class r17 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00ec }
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x00ec }
            r16 = 2
            java.lang.Class r17 = java.lang.Integer.TYPE     // Catch:{ NoSuchMethodException -> 0x00ec }
            r15[r16] = r17     // Catch:{ NoSuchMethodException -> 0x00ec }
            java.lang.reflect.Method r2 = r4.getMethod(r14, r15)     // Catch:{ NoSuchMethodException -> 0x00ec }
            r14 = 3
            java.lang.Object[] r14 = new java.lang.Object[r14]     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            r15 = 0
            r14[r15] = r19     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            r15 = 1
            java.lang.Integer r16 = java.lang.Integer.valueOf(r20)     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            r14[r15] = r16     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            r15 = 2
            java.lang.Integer r16 = java.lang.Integer.valueOf(r21)     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            r14[r15] = r16     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            r2.invoke(r3, r14)     // Catch:{ IllegalArgumentException -> 0x00d8, IllegalAccessException -> 0x00f6, InvocationTargetException -> 0x0100 }
            goto L_0x0061
        L_0x00d8:
            r12 = move-exception
            r12.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x00e2:
            r7 = move-exception
            r7.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x00ec:
            r6 = move-exception
            r6.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x00f6:
            r13 = move-exception
            r13.printStackTrace()     // Catch:{ all -> 0x006d }
            com.telpo.tps550.api.InternalErrorException r14 = new com.telpo.tps550.api.InternalErrorException     // Catch:{ all -> 0x006d }
            r14.<init>()     // Catch:{ all -> 0x006d }
            throw r14     // Catch:{ all -> 0x006d }
        L_0x0100:
            r5 = move-exception
            r0 = r18
            r0.throwException(r5)     // Catch:{ all -> 0x006d }
            goto L_0x0061
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.UsbThermalPrinter.printLogoRaw(byte[], int, int):void");
    }

    private void throwException(InvocationTargetException e) throws TelpoException {
        if (e.getTargetException().toString().contains("NoPaper")) {
            throw new NoPaperException();
        } else if (e.getTargetException().toString().contains("OverHeat")) {
            throw new OverHeatException();
        } else if (e.getTargetException().toString().contains("BlackBlock")) {
            throw new BlackBlockNotFoundException();
        } else {
            throw new TelpoException();
        }
    }
}
