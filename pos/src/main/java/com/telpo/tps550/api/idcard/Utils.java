package com.telpo.tps550.api.idcard;

import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;

public class Utils {
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0042 A[SYNTHETIC, Splitter:B:21:0x0042] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasRootPermission() {
        /*
            r3 = 0
            r1 = 0
            r4 = 1
            java.lang.Runtime r5 = java.lang.Runtime.getRuntime()     // Catch:{ Exception -> 0x0032, all -> 0x003f }
            java.lang.String r6 = "su"
            java.lang.Process r3 = r5.exec(r6)     // Catch:{ Exception -> 0x0032, all -> 0x003f }
            java.io.DataOutputStream r2 = new java.io.DataOutputStream     // Catch:{ Exception -> 0x0032, all -> 0x003f }
            java.io.OutputStream r5 = r3.getOutputStream()     // Catch:{ Exception -> 0x0032, all -> 0x003f }
            r2.<init>(r5)     // Catch:{ Exception -> 0x0032, all -> 0x003f }
            java.lang.String r5 = "exit\n"
            r2.writeBytes(r5)     // Catch:{ Exception -> 0x0051, all -> 0x004e }
            r2.flush()     // Catch:{ Exception -> 0x0051, all -> 0x004e }
            r3.waitFor()     // Catch:{ Exception -> 0x0051, all -> 0x004e }
            int r5 = r3.exitValue()     // Catch:{ Exception -> 0x0051, all -> 0x004e }
            if (r5 == 0) goto L_0x0028
            r4 = 0
        L_0x0028:
            if (r2 == 0) goto L_0x0054
            r2.close()     // Catch:{ Exception -> 0x0049 }
            r3.destroy()     // Catch:{ Exception -> 0x0049 }
            r1 = r2
        L_0x0031:
            return r4
        L_0x0032:
            r0 = move-exception
        L_0x0033:
            r4 = 0
            if (r1 == 0) goto L_0x0031
            r1.close()     // Catch:{ Exception -> 0x003d }
            r3.destroy()     // Catch:{ Exception -> 0x003d }
            goto L_0x0031
        L_0x003d:
            r5 = move-exception
            goto L_0x0031
        L_0x003f:
            r5 = move-exception
        L_0x0040:
            if (r1 == 0) goto L_0x0048
            r1.close()     // Catch:{ Exception -> 0x004c }
            r3.destroy()     // Catch:{ Exception -> 0x004c }
        L_0x0048:
            throw r5
        L_0x0049:
            r5 = move-exception
            r1 = r2
            goto L_0x0031
        L_0x004c:
            r6 = move-exception
            goto L_0x0048
        L_0x004e:
            r5 = move-exception
            r1 = r2
            goto L_0x0040
        L_0x0051:
            r0 = move-exception
            r1 = r2
            goto L_0x0033
        L_0x0054:
            r1 = r2
            goto L_0x0031
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.idcard.Utils.hasRootPermission():boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0067 A[SYNTHETIC, Splitter:B:12:0x0067] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0077 A[SYNTHETIC, Splitter:B:20:0x0077] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean upgradeRootPermission(java.lang.String r8) {
        /*
            r4 = 0
            r2 = 0
            java.lang.String r5 = "initUsbDevice"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.String r7 = "techshino  pkgCodePath = "
            r6.<init>(r7)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.StringBuilder r6 = r6.append(r8)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            android.util.Log.d(r5, r6)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.String r6 = "chmod 0777 "
            r5.<init>(r6)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.StringBuilder r5 = r5.append(r8)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.String r0 = r5.toString()     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.Runtime r5 = java.lang.Runtime.getRuntime()     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.String r6 = "su"
            java.lang.Process r4 = r5.exec(r6)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.io.DataOutputStream r3 = new java.io.DataOutputStream     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.io.OutputStream r5 = r4.getOutputStream()     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            r3.<init>(r5)     // Catch:{ Exception -> 0x0064, all -> 0x0074 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            java.lang.String r6 = java.lang.String.valueOf(r0)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r5.<init>(r6)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            java.lang.String r6 = "\n"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r3.writeBytes(r5)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            java.lang.String r5 = "exit\n"
            r3.writeBytes(r5)     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r3.flush()     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            r4.waitFor()     // Catch:{ Exception -> 0x008b, all -> 0x0088 }
            if (r3 == 0) goto L_0x005e
            r3.close()     // Catch:{ Exception -> 0x0083 }
        L_0x005e:
            r4.destroy()     // Catch:{ Exception -> 0x0083 }
        L_0x0061:
            r5 = 1
            r2 = r3
        L_0x0063:
            return r5
        L_0x0064:
            r1 = move-exception
        L_0x0065:
            if (r2 == 0) goto L_0x006a
            r2.close()     // Catch:{ Exception -> 0x006f }
        L_0x006a:
            r4.destroy()     // Catch:{ Exception -> 0x006f }
        L_0x006d:
            r5 = 0
            goto L_0x0063
        L_0x006f:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x006d
        L_0x0074:
            r5 = move-exception
        L_0x0075:
            if (r2 == 0) goto L_0x007a
            r2.close()     // Catch:{ Exception -> 0x007e }
        L_0x007a:
            r4.destroy()     // Catch:{ Exception -> 0x007e }
        L_0x007d:
            throw r5
        L_0x007e:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x007d
        L_0x0083:
            r1 = move-exception
            r1.printStackTrace()
            goto L_0x0061
        L_0x0088:
            r5 = move-exception
            r2 = r3
            goto L_0x0075
        L_0x008b:
            r1 = move-exception
            r2 = r3
            goto L_0x0065
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.idcard.Utils.upgradeRootPermission(java.lang.String):boolean");
    }

    public static byte[] readSDFile(String fileName) {
        try {
            FileInputStream is = new FileInputStream(new File(Environment.getExternalStorageDirectory(), fileName));
            byte[] b = new byte[is.available()];
            is.read(b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
