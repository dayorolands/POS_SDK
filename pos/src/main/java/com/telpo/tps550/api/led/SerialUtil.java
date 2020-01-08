package com.telpo.tps550.api.led;

import android.os.Handler;
import android.util.Log;
import android_serialport_api.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SerialUtil {
    private static final String Tag = "SerialUtil";
    /* access modifiers changed from: private */
    public final Handler handler;
    /* access modifiers changed from: private */
    public InputStream inputStream;
    private OutputStream outputStream;
    /* access modifiers changed from: private */
    public ByteBuffer rcvBuffer = ByteBuffer.allocate(4096);
    private ReadThread readThread;
    private SerialPort serialPort;

    public SerialUtil(Handler hd) {
        this.handler = hd;
    }

    private class ReadThread extends Thread {
        private ReadThread() {
        }

        /* synthetic */ ReadThread(SerialUtil serialUtil, ReadThread readThread) {
            this();
        }

        /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
            java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
            	at java.base/jdk.internal.util.Preconditions.outOfBounds(Unknown Source)
            	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Unknown Source)
            	at java.base/jdk.internal.util.Preconditions.checkIndex(Unknown Source)
            	at java.base/java.util.Objects.checkIndex(Unknown Source)
            	at java.base/java.util.ArrayList.get(Unknown Source)
            	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
            	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMaker.processLoop(RegionMaker.java:225)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:106)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
            	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
            	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
            	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
            */
        public synchronized void run() {
            /*
                r7 = this;
                monitor-enter(r7)
                r4 = 1024(0x400, float:1.435E-42)
                byte[] r0 = new byte[r4]     // Catch:{ all -> 0x0086 }
                java.lang.String r4 = "SerialUtil"
                java.lang.String r5 = "Read thread start!"
                android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0086 }
                super.run()     // Catch:{ all -> 0x0086 }
            L_0x000f:
                boolean r4 = r7.isInterrupted()     // Catch:{ Exception -> 0x007a }
                if (r4 == 0) goto L_0x001e
            L_0x0015:
                java.lang.String r4 = "SerialUtil"
                java.lang.String r5 = "Read thread end!"
                android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0086 }
                monitor-exit(r7)
                return
            L_0x001e:
                r4 = 200(0xc8, double:9.9E-322)
                sleep(r4)     // Catch:{ Exception -> 0x007a }
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ Exception -> 0x007a }
                java.io.InputStream r4 = r4.inputStream     // Catch:{ Exception -> 0x007a }
                int r4 = r4.available()     // Catch:{ Exception -> 0x007a }
                if (r4 <= 0) goto L_0x000f
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ Exception -> 0x007a }
                java.io.InputStream r4 = r4.inputStream     // Catch:{ Exception -> 0x007a }
                int r3 = r4.read(r0)     // Catch:{ Exception -> 0x007a }
                if (r3 <= 0) goto L_0x000f
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ Exception -> 0x007a }
                java.nio.ByteBuffer r5 = r4.rcvBuffer     // Catch:{ Exception -> 0x007a }
                monitor-enter(r5)     // Catch:{ Exception -> 0x007a }
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ all -> 0x008b }
                java.nio.ByteBuffer r4 = r4.rcvBuffer     // Catch:{ all -> 0x008b }
                boolean r4 = r4.hasRemaining()     // Catch:{ all -> 0x008b }
                if (r4 == 0) goto L_0x006e
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ all -> 0x008b }
                java.nio.ByteBuffer r4 = r4.rcvBuffer     // Catch:{ all -> 0x008b }
                int r4 = r4.remaining()     // Catch:{ all -> 0x008b }
                if (r4 >= r3) goto L_0x0089
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ all -> 0x008b }
                java.nio.ByteBuffer r4 = r4.rcvBuffer     // Catch:{ all -> 0x008b }
                int r1 = r4.remaining()     // Catch:{ all -> 0x008b }
            L_0x0064:
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ all -> 0x008b }
                java.nio.ByteBuffer r4 = r4.rcvBuffer     // Catch:{ all -> 0x008b }
                r6 = 0
                r4.put(r0, r6, r1)     // Catch:{ all -> 0x008b }
            L_0x006e:
                monitor-exit(r5)     // Catch:{ all -> 0x008b }
                com.telpo.tps550.api.led.SerialUtil r4 = com.telpo.tps550.api.led.SerialUtil.this     // Catch:{ Exception -> 0x007a }
                android.os.Handler r4 = r4.handler     // Catch:{ Exception -> 0x007a }
                r5 = 1
                r4.sendEmptyMessage(r5)     // Catch:{ Exception -> 0x007a }
                goto L_0x000f
            L_0x007a:
                r2 = move-exception
                java.lang.String r4 = "SerialUtil"
                java.lang.String r5 = "Read thread end for IOException!"
                android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0086 }
                r2.printStackTrace()     // Catch:{ all -> 0x0086 }
                goto L_0x0015
            L_0x0086:
                r4 = move-exception
                monitor-exit(r7)
                throw r4
            L_0x0089:
                r1 = r3
                goto L_0x0064
            L_0x008b:
                r4 = move-exception
                monitor-exit(r5)     // Catch:{ all -> 0x008b }
                throw r4     // Catch:{ Exception -> 0x007a }
            */
            throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.led.SerialUtil.ReadThread.run():void");
        }
    }

    public void open(String path, int baudrate) throws SecurityException, IOException {
        if (this.serialPort == null) {
            Log.i(Tag, path);
            this.serialPort = new SerialPort(true,"/system/xbin/su",new File(path), baudrate, 0);
            if (this.serialPort != null) {
                if (this.readThread == null) {
                    this.readThread = new ReadThread(this, (ReadThread) null);
                    this.readThread.start();
                }
                this.inputStream = this.serialPort.getInputStream();
                this.outputStream = this.serialPort.getOutputStream();
                Log.d(Tag, "Open serial port OK!");
            }
        }
    }

    public void close() {
        if (this.readThread != null) {
            this.readThread.interrupt();
            this.readThread = null;
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                Log.d(Tag, "inputStream close failed!");
                e.printStackTrace();
            }
            this.inputStream = null;
        }
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e2) {
                Log.d(Tag, "outputStream close failed!");
                e2.printStackTrace();
            }
            this.outputStream = null;
        }
        if (this.serialPort != null) {
            this.serialPort.close();
            this.serialPort = null;
        }
        Log.d(Tag, "close serial port OK!");
    }

    public void send(byte[] data) {
        if (this.outputStream != null) {
            int len = data.length;
            int i = 0;
            while (i < len) {
                try {
                    this.outputStream.write(data[i]);
                    this.outputStream.flush();
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public int get(byte[] buf) {
        int count = 0;
        synchronized (this.rcvBuffer) {
            this.rcvBuffer.flip();
            if (this.rcvBuffer.hasRemaining()) {
                count = this.rcvBuffer.remaining() < buf.length ? this.rcvBuffer.remaining() : buf.length;
                this.rcvBuffer.get(buf, 0, count);
            }
            this.rcvBuffer.compact();
        }
        return count;
    }

    public boolean isOpen() {
        if (this.serialPort == null) {
            return false;
        }
        return true;
    }
}
