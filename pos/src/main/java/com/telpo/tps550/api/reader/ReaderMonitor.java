package com.telpo.tps550.api.reader;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.telpo.tps550.api.magnetic.MagneticCard;

public class ReaderMonitor {
    public static final String ACTION_ICC_PRESENT = "com.pos.icc.present";
    public static final String ACTION_MSC = "com.pos.msc";
    public static final String EXTRA_CARD_TYPE = "card_type";
    public static final String EXTRA_IS_PRESENT = "present";
    public static final String EXTRA_MSC_TRACK = "track_data";
    public static final String TAG = "ReaderMonitor";
    /* access modifiers changed from: private */
    public static int cardType = 1;
    /* access modifiers changed from: private */
    public static Context context = null;
    /* access modifiers changed from: private */
    public static boolean iccPresent = false;
    private static boolean isStarted = false;
    /* access modifiers changed from: private */
    public static Object lock = new Object();
    /* access modifiers changed from: private */
    public static Thread monitorThread = null;
    private static String[] mscData;
    private static boolean mscFlag = false;
    private static Thread mscMonitorThread = null;
    /* access modifiers changed from: private */
    public static boolean openFlag = false;
    /* access modifiers changed from: private */
    public static boolean poweronFlag = false;
    /* access modifiers changed from: private */
    public static CardReader reader;
    /* access modifiers changed from: private */
    public static int type;

    public static void setContext(Context c) {
        context = c;
    }

    public static synchronized void startMonitor() {
        synchronized (ReaderMonitor.class) {
            if (context == null) {
                Log.e(TAG, "context null");
            } else {
                monitorThread = new Thread(new Runnable() {
                    Intent intent = null;

                    public void run() {
                        while (!ReaderMonitor.monitorThread.isInterrupted()) {
                            try {
                                Thread.sleep(500);
                                if (!ReaderMonitor.openFlag) {
                                    ReaderMonitor.reader = new SmartCardReader(ReaderMonitor.context);
                                    if (!ReaderMonitor.reader.open()) {
                                        Log.e(ReaderMonitor.TAG, "reader open failed");
                                    } else {
                                        ReaderMonitor.openFlag = true;
                                        ReaderMonitor.reader.switchMode(1);
                                    }
                                }
                                synchronized (ReaderMonitor.lock) {
                                    if (ReaderMonitor.iccPresent) {
                                        if (!ReaderMonitor.reader.isICCPresent()) {
                                            ReaderMonitor.iccPresent = false;
                                            this.intent = new Intent();
                                            this.intent.setAction(ReaderMonitor.ACTION_ICC_PRESENT);
                                            this.intent.putExtra(ReaderMonitor.EXTRA_IS_PRESENT, false);
                                            if (ReaderMonitor.context != null) {
                                                ReaderMonitor.context.sendBroadcast(this.intent);
                                            }
                                            ReaderMonitor.poweronFlag = false;
                                            ReaderMonitor.reader.close();
                                            ReaderMonitor.openFlag = false;
                                        }
                                    } else if (ReaderMonitor.reader.isICCPresent()) {
                                        ReaderMonitor.iccPresent = true;
                                        this.intent = new Intent();
                                        this.intent.setAction(ReaderMonitor.ACTION_ICC_PRESENT);
                                        this.intent.putExtra(ReaderMonitor.EXTRA_IS_PRESENT, true);
                                        if (ReaderMonitor.reader.iccPowerOn()) {
                                            Log.e(ReaderMonitor.TAG, "smart card poweron success");
                                            this.intent.putExtra(ReaderMonitor.EXTRA_CARD_TYPE, 1);
                                            if (ReaderMonitor.context != null) {
                                                ReaderMonitor.context.sendBroadcast(this.intent);
                                            }
                                            ReaderMonitor.poweronFlag = true;
                                            ReaderMonitor.cardType = 1;
                                        } else {
                                            ReaderMonitor.reader.switchMode(3);
                                            if (ReaderMonitor.reader.iccPowerOn()) {
                                                ReaderMonitor.type = ReaderMonitor.reader.getCardType();
                                                this.intent.putExtra(ReaderMonitor.EXTRA_CARD_TYPE, ReaderMonitor.type);
                                                if (ReaderMonitor.context != null) {
                                                    ReaderMonitor.context.sendBroadcast(this.intent);
                                                }
                                                if (ReaderMonitor.type == 3) {
                                                    Log.d(ReaderMonitor.TAG, "card type: SLE4428");
                                                    ReaderMonitor.cardType = 3;
                                                    if (ReaderMonitor.reader.close()) {
                                                        ReaderMonitor.openFlag = false;
                                                        ReaderMonitor.reader = new SLE4428Reader(ReaderMonitor.context);
                                                        if (ReaderMonitor.reader.open()) {
                                                            ReaderMonitor.openFlag = true;
                                                            if (ReaderMonitor.reader.iccPowerOn()) {
                                                                Log.d(ReaderMonitor.TAG, "SLE4428 poweron success");
                                                                ReaderMonitor.poweronFlag = true;
                                                            } else {
                                                                Log.d(ReaderMonitor.TAG, "SLE4428 poweron failed");
                                                            }
                                                        }
                                                    }
                                                } else if (ReaderMonitor.type == 2) {
                                                    Log.d(ReaderMonitor.TAG, "card type: SLE4442");
                                                    ReaderMonitor.cardType = 2;
                                                    if (ReaderMonitor.reader.close()) {
                                                        ReaderMonitor.openFlag = false;
                                                        ReaderMonitor.reader = new SLE4442Reader(ReaderMonitor.context);
                                                        if (ReaderMonitor.reader.open()) {
                                                            ReaderMonitor.openFlag = true;
                                                            if (ReaderMonitor.reader.iccPowerOn()) {
                                                                Log.d(ReaderMonitor.TAG, "SLE4442 poweron success");
                                                                ReaderMonitor.poweronFlag = true;
                                                            } else {
                                                                Log.d(ReaderMonitor.TAG, "SLE4442 poweron failed");
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Log.e(ReaderMonitor.TAG, "card type unknown");
                                                    ReaderMonitor.cardType = -1;
                                                }
                                            } else {
                                                Log.e(ReaderMonitor.TAG, "ICC poweron failed");
                                            }
                                        }
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                });
                monitorThread.start();
                isStarted = true;
            }
        }
    }

    public static synchronized void stopMonitor() {
        synchronized (ReaderMonitor.class) {
            if (monitorThread != null) {
                monitorThread.interrupt();
            }
            if (mscMonitorThread != null) {
                mscMonitorThread.interrupt();
            }
            if (openFlag && reader.close()) {
                openFlag = false;
            }
            MagneticCard.close();
            mscFlag = false;
            poweronFlag = false;
            isStarted = false;
            iccPresent = false;
        }
    }

    public static synchronized boolean isStarted() {
        boolean z;
        synchronized (ReaderMonitor.class) {
            z = isStarted;
        }
        return z;
    }

    public static synchronized boolean isICCPresent() {
        boolean z;
        synchronized (ReaderMonitor.class) {
            z = iccPresent;
        }
        return z;
    }

    public static synchronized byte[] readMainMemory(int addr, int num) {
        byte[] bArr = null;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
            } else {
                synchronized (lock) {
                    if (!poweronFlag) {
                        if (reader.iccPowerOn()) {
                            poweronFlag = true;
                        }
                    }
                    if (cardType == 2) {
                        bArr = ((SLE4442Reader) reader).readMainMemory(addr, num);
                    } else if (cardType == 3) {
                        bArr = ((SLE4428Reader) reader).readMainMemory(addr, num);
                    }
                }
            }
        }
        return bArr;
    }

    public static synchronized boolean updateMainMemory(int addr, byte[] data) {
        boolean z;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
                z = false;
            } else {
                synchronized (lock) {
                    if (cardType == 2) {
                        z = ((SLE4442Reader) reader).updateMainMemory(addr, data);
                    } else if (cardType == 3) {
                        z = ((SLE4428Reader) reader).updateMainMemory(addr, data);
                    } else {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public static synchronized boolean pscVerify(byte[] psc) {
        boolean z = false;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
            } else {
                synchronized (lock) {
                    if (!poweronFlag) {
                        if (reader.iccPowerOn()) {
                            poweronFlag = true;
                        }
                    }
                    if (cardType == 2) {
                        if (!((SLE4442Reader) reader).pscVerify(psc)) {
                            Log.e(TAG, "SLE4442 psc verification failed");
                        }
                    } else if (cardType == 3 && !((SLE4428Reader) reader).pscVerify(psc)) {
                        Log.e(TAG, "SLE4428 psc verification failed");
                    }
                    z = true;
                }
            }
        }
        return z;
    }

    public static synchronized void reset() {
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
            } else {
                synchronized (lock) {
                    if (reader.iccPowerOff()) {
                        poweronFlag = false;
                    }
                    if (reader.iccPowerOn()) {
                        poweronFlag = true;
                    }
                }
            }
        }
    }

    public static synchronized byte[] getUserCode() {
        byte[] bArr;
        synchronized (ReaderMonitor.class) {
            if (cardType == 2 || cardType == 3) {
                bArr = readMainMemory(21, 6);
            } else {
                bArr = null;
            }
        }
        return bArr;
    }

    public static synchronized boolean pscModify(byte[] pscNew) {
        boolean z = false;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
            } else {
                synchronized (lock) {
                    if (cardType == 2) {
                        z = ((SLE4442Reader) reader).pscModify(pscNew);
                    } else if (cardType == 3) {
                        z = ((SLE4428Reader) reader).pscModify(pscNew);
                    }
                }
            }
        }
        return z;
    }

    public static synchronized byte[] transmit(byte[] apdu) {
        byte[] bArr = null;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
            } else {
                synchronized (lock) {
                    if (cardType == 1) {
                        bArr = ((SmartCardReader) reader).transmit(apdu);
                    }
                }
            }
        }
        return bArr;
    }

    public static synchronized int getProtocol() {
        int i = 2;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
            } else {
                synchronized (lock) {
                    if (cardType == 1) {
                        i = ((SmartCardReader) reader).getProtocol();
                    }
                }
            }
        }
        return i;
    }

    public static synchronized String getATRString() {
        String aTRString;
        synchronized (ReaderMonitor.class) {
            if (!openFlag) {
                Log.e(TAG, "reader has not opened");
                aTRString = null;
            } else {
                synchronized (lock) {
                    aTRString = reader.getATRString();
                }
            }
        }
        return aTRString;
    }
}
