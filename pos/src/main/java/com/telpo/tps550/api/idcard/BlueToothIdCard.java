package com.telpo.tps550.api.idcard;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.ReaderUtils;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressLint({"NewApi"})
public class BlueToothIdCard {
    /* access modifiers changed from: private */
    public static final UUID CHARACTERISTIC_NOTIFY_UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");
    /* access modifiers changed from: private */
    public static final UUID CHARACTERISTIC_OUTPUT_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");
    private static final byte[] SAM_HEADER = {-86, -86, -86, -106, 105};
    /* access modifiers changed from: private */
    public static final UUID SERVICE_NOTIFY_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    /* access modifiers changed from: private */
    public static final UUID SERVICE_WRITE_UUID = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb");
    private static final int STEP_CHECK = 0;
    private static final int STEP_READ = 2;
    private static final int STEP_SELECT = 1;
    private static final String TAG = "BlueToothIdCard";
    static String[] nation_list = {"汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌孜别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺", "其他", "外国血统中国籍人士"};
    /* access modifiers changed from: private */
    public static BluetoothGattCharacteristic notifyCharacteristic;
    /* access modifiers changed from: private */
    public static byte[] receiveData = null;
    private static byte[] sendData = null;
    /* access modifiers changed from: private */
    public static BluetoothGattCharacteristic writeCharacteristic;
    public int charSendBufferPos = 0;
    private int contentLength;
    private int currentStep = 0;
    private int fplength;
    private BluetoothGatt gatt = null;
    private int imageDatalength;
    public boolean isConnect = false;
    private Context mContext = null;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == 2) {
                gatt.discoverServices();
                Log.i(BlueToothIdCard.TAG, "Connected to GATT server.");
            } else if (newState == 0) {
                Log.i(BlueToothIdCard.TAG, "Disconnected from GATT server.");
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                Log.i(BlueToothIdCard.TAG, "onServicesDiscovered");
                List<BluetoothGattService> services = gatt.getServices();
                BluetoothGattService wirte_service = null;
                BluetoothGattService notify_service = null;
                for (int i = 0; i < services.size(); i++) {
                    BluetoothGattService s = services.get(i);
                    Log.d(BlueToothIdCard.TAG, "service " + i + ":" + s.getUuid().toString());
                    if (BlueToothIdCard.SERVICE_WRITE_UUID.toString().equals(s.getUuid().toString())) {
                        wirte_service = s;
                    }
                    if (BlueToothIdCard.SERVICE_NOTIFY_UUID.toString().equals(s.getUuid().toString())) {
                        notify_service = s;
                    }
                }
                List<BluetoothGattCharacteristic> characteristics = wirte_service.getCharacteristics();
                for (int i2 = 0; i2 < characteristics.size(); i2++) {
                    BluetoothGattCharacteristic c = characteristics.get(i2);
                    Log.d(BlueToothIdCard.TAG, "characteristic " + i2 + ":" + c.getUuid().toString());
                    if (BlueToothIdCard.CHARACTERISTIC_OUTPUT_UUID.toString().equals(c.getUuid().toString())) {
                        BlueToothIdCard.writeCharacteristic = c;
                    }
                }
                List<BluetoothGattCharacteristic> characteristics2 = notify_service.getCharacteristics();
                for (int i3 = 0; i3 < characteristics2.size(); i3++) {
                    BluetoothGattCharacteristic c2 = characteristics2.get(i3);
                    Log.d(BlueToothIdCard.TAG, "characteristic " + i3 + ":" + c2.getUuid().toString());
                    if (BlueToothIdCard.CHARACTERISTIC_NOTIFY_UUID.toString().equals(c2.getUuid().toString())) {
                        BlueToothIdCard.notifyCharacteristic = c2;
                    }
                }
                if (BlueToothIdCard.writeCharacteristic == null || BlueToothIdCard.notifyCharacteristic == null || !BlueToothIdCard.this.isCharacteristicWritable(BlueToothIdCard.writeCharacteristic) || !BlueToothIdCard.this.isCharacteristicNotifiable(BlueToothIdCard.notifyCharacteristic)) {
                    Log.d(BlueToothIdCard.TAG, "未知设备");
                    BlueToothIdCard.this.isConnect = false;
                } else if (gatt.setCharacteristicNotification(BlueToothIdCard.notifyCharacteristic, true)) {
                    Log.d(BlueToothIdCard.TAG, "连接成功");
                    BlueToothIdCard.this.isConnect = true;
                } else {
                    Log.d(BlueToothIdCard.TAG, "连接失败");
                    BlueToothIdCard.this.isConnect = false;
                }
            } else {
                Log.d(BlueToothIdCard.TAG, "连接失败");
                BlueToothIdCard.this.isConnect = false;
            }
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(BlueToothIdCard.TAG, "characteristic uuid = " + characteristic.getUuid().toString());
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] receive = characteristic.getValue();
            Log.d(BlueToothIdCard.TAG, "onCharacteristicChanged------>" + ReaderUtils.byte2HexString(receive));
            if (receive != null && receive.length > 0) {
                BlueToothIdCard.receiveData = ReaderUtils.merge(BlueToothIdCard.receiveData, receive);
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(BlueToothIdCard.TAG, "onCharacteristicWrite------>" + ReaderUtils.byte2HexString(characteristic.getValue()));
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(BlueToothIdCard.TAG, "onDescriptorRead------>" + ReaderUtils.byte2HexString(descriptor.getValue()));
            Log.d(BlueToothIdCard.TAG, "descriptor uuid = " + descriptor.getUuid().toString());
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(BlueToothIdCard.TAG, "onDescriptorWrite------>" + ReaderUtils.byte2HexString(descriptor.getValue()));
            Log.d(BlueToothIdCard.TAG, "descriptor uuid = " + descriptor.getUuid().toString());
        }

        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.d(BlueToothIdCard.TAG, "onReliableWriteCompleted------>" + status);
        }
    };

    public BlueToothIdCard(Context context) {
        this.mContext = context;
    }

    public void connectDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            this.gatt = bluetoothDevice.connectGatt(this.mContext, false, this.mGattCallback);
        }
    }

    private boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & 2) > 0;
    }

    /* access modifiers changed from: private */
    public boolean isCharacteristicWritable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & 8) > 0 || (characteristic.getProperties() & 4) > 0;
    }

    /* access modifiers changed from: private */
    public boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & 16) > 0 || (characteristic.getProperties() & 32) > 0;
    }

    private byte[] packageCommand(byte[] data2send) {
        if (data2send == null || data2send.length <= 0) {
            return null;
        }
        int len = data2send.length;
        byte crc = 0;
        byte[] result = new byte[(len + 4 + 2)];
        int pos = 0 + 1;
        result[0] = 104;
        int pos2 = pos + 1;
        result[pos] = (byte) (len % 255);
        int pos3 = pos2 + 1;
        result[pos2] = (byte) (len / 255);
        result[pos3] = 104;
        int length = data2send.length;
        int i = 0;
        int pos4 = pos3 + 1;
        while (i < length) {
            byte d = data2send[i];
            result[pos4] = d;
            crc = (byte) (crc ^ d);
            i++;
            pos4++;
        }
        int pos5 = pos4 + 1;
        result[pos4] = crc;
        int i2 = pos5 + 1;
        result[pos5] = 22;
        return result;
    }

    private void sendCommand() {
        int Len = 20;
        if (this.charSendBufferPos + 20 > sendData.length) {
            Len = (byte) (sendData.length - this.charSendBufferPos);
        }
        byte[] value1 = new byte[Len];
        for (byte i = 0; i < Len; i = (byte) (i + 1)) {
            value1[i] = sendData[this.charSendBufferPos + i];
        }
        writeCharacteristic.setValue(value1);
        Log.d("idcard demo", "before write");
        this.gatt.writeCharacteristic(writeCharacteristic);
        Log.d("idcard demo", "after write");
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.charSendBufferPos += Len;
        if (this.charSendBufferPos == sendData.length) {
            this.charSendBufferPos = 0;
        }
        this.charSendBufferPos %= sendData.length;
    }

    public IdentityMsg checkIdCard() {
        receiveData = null;
        byte[] data2send = new byte[10];
        data2send[0] = -86;
        data2send[1] = -86;
        data2send[2] = -86;
        data2send[3] = -106;
        data2send[4] = 105;
        data2send[6] = 3;
        data2send[7] = 32;
        data2send[8] = 1;
        data2send[9] = 34;
        if (data2send != null && data2send.length > 0) {
            sendData = packageCommand(data2send);
            if (sendData != null) {
                sendCommand();
                this.currentStep = 0;
            }
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] stepReceiveData = checkReceivedData();
        receiveData = null;
        byte[] data2send2 = new byte[10];
        data2send2[0] = -86;
        data2send2[1] = -86;
        data2send2[2] = -86;
        data2send2[3] = -106;
        data2send2[4] = 105;
        data2send2[6] = 3;
        data2send2[7] = 32;
        data2send2[8] = 2;
        data2send2[9] = 33;
        if (data2send2 != null && data2send2.length > 0) {
            sendData = packageCommand(data2send2);
            if (sendData != null) {
                sendCommand();
                this.currentStep = 1;
            }
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        byte[] stepReceiveData2 = checkReceivedData();
        receiveData = null;
        byte[] data2send3 = new byte[10];
        data2send3[0] = -86;
        data2send3[1] = -86;
        data2send3[2] = -86;
        data2send3[3] = -106;
        data2send3[4] = 105;
        data2send3[6] = 3;
        data2send3[7] = 48;
        data2send3[8] = 16;
        data2send3[9] = 35;
        if (data2send3 != null && data2send3.length > 0) {
            sendData = packageCommand(data2send3);
            if (sendData != null) {
                sendCommand();
                this.currentStep = 2;
            }
        }
        byte[] stepReceiveData3 = checkReceivedData();
        receiveData = null;
        if (stepReceiveData3 != null) {
            Log.e("idcard demo", "stepReceiveData.length is" + stepReceiveData3.length);
        }
        return decodeIdCardBaseInfo(stepReceiveData3);
    }

    public static synchronized byte[] getIdCardImage(IdentityMsg info) throws TelpoException {
        byte[] image;
        synchronized (BlueToothIdCard.class) {
            image = info.getHead_photo();
            if (image == null) {
                throw new IdCardNotCheckException();
            }
        }
        return image;
    }

    public static synchronized byte[] getFringerPrint(IdentityMsg info) throws TelpoException {
        byte[] fringerprint;
        synchronized (BlueToothIdCard.class) {
            byte[] image = getIdCardImage(info);
            if (image == null) {
                throw new IdCardNotCheckException();
            }
            try {
                fringerprint = Arrays.copyOfRange(image, IdCard.READER_VID_BIG, image.length);
                if (fringerprint == null) {
                    throw new IdCardNotCheckException();
                }
            } catch (Exception e) {
                throw new IdCardNotCheckException();
            }
        }
        return fringerprint;
    }

    public static Bitmap decodeIdCardImage(byte[] image) throws TelpoException {
        if (image == null) {
            throw new ImageDecodeException();
        }
        byte[] buf = new byte[WLTService.imgLength];
        if (1 == WLTService.wlt2Bmp(image, buf)) {
            return IDPhotoHelper.Bgr2Bitmap(buf);
        }
        throw new ImageDecodeException();
    }

    private byte[] checkReceivedData() {
        int pos;
        try {
            if (this.currentStep == 2) {
                Thread.sleep(2500);
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        if (receiveData == null) {
            return null;
        }
        if (this.currentStep == 2 && receiveData.length < 1024) {
            return null;
        }
        byte[] answer_code = null;
        switch (this.currentStep) {
            case 0:
                answer_code = new byte[3];
                answer_code[2] = -97;
                break;
            case 1:
                answer_code = new byte[3];
                answer_code[2] = -112;
                break;
            case 2:
                answer_code = new byte[3];
                answer_code[2] = -112;
                break;
        }
        if (answer_code == null) {
            return null;
        }
        int pos2 = 0;
        int i = 0;
        while (true) {
            try {
                pos = pos2;
                if (i >= SAM_HEADER.length) {
                    int pos3 = pos + 2;
                    int i2 = 0;
                    while (true) {
                        pos = pos3;
                        if (i2 >= answer_code.length) {
                            return receiveData;
                        }
                        pos3 = pos + 1;
                        if (receiveData[pos] != answer_code[i2]) {
                            return null;
                        }
                        i2++;
                    }
                } else {
                    pos2 = pos + 1;
                    try {
                        if (receiveData[pos] != SAM_HEADER[i]) {
                            return null;
                        }
                        i++;
                    } catch (Exception e) {
                        return null;
                    }
                }
            } catch (Exception e2) {
//                int i3 = pos;
                return null;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x013a  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x021f  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0267  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x027e  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x02ff  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x034e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.telpo.tps550.api.idcard.IdentityMsg decodeIdCardBaseInfo(byte[] r18) {
        /*
            r17 = this;
            if (r18 != 0) goto L_0x0004
            r6 = 0
        L_0x0003:
            return r6
        L_0x0004:
            r13 = 16
            r0 = r18
            int r14 = r0.length
            int r14 = r14 + -1
            r0 = r18
            byte[] r18 = java.util.Arrays.copyOfRange(r0, r13, r14)
            r0 = r18
            int r7 = r0.length
            java.lang.String r13 = "BlueToothIdCard"
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            java.lang.String r15 = "info len = "
            r14.<init>(r15)
            java.lang.StringBuilder r14 = r14.append(r7)
            java.lang.String r14 = r14.toString()
            android.util.Log.d(r13, r14)
            r13 = 1280(0x500, float:1.794E-42)
            if (r7 == r13) goto L_0x0034
            r13 = 1792(0x700, float:2.511E-42)
            if (r7 == r13) goto L_0x0034
            r13 = 2304(0x900, float:3.229E-42)
            if (r7 != r13) goto L_0x035d
        L_0x0034:
            r13 = 2
            byte r13 = r18[r13]
            char r13 = (char) r13
            int r13 = r13 * 256
            r14 = 3
            byte r14 = r18[r14]
            char r14 = (char) r14
            int r13 = r13 + r14
            r0 = r17
            r0.imageDatalength = r13
            r13 = 0
            byte r13 = r18[r13]
            int r13 = r13 << 8
            r14 = 65280(0xff00, float:9.1477E-41)
            r13 = r13 & r14
            r0 = r17
            r0.contentLength = r13
            r0 = r17
            int r13 = r0.contentLength
            r14 = 1
            byte r14 = r18[r14]
            int r13 = r13 + r14
            r0 = r17
            r0.contentLength = r13
            r13 = 2
            byte r13 = r18[r13]
            int r13 = r13 << 8
            r14 = 65280(0xff00, float:9.1477E-41)
            r13 = r13 & r14
            r0 = r17
            r0.imageDatalength = r13
            r0 = r17
            int r13 = r0.imageDatalength
            r14 = 3
            byte r14 = r18[r14]
            int r13 = r13 + r14
            r0 = r17
            r0.imageDatalength = r13
            r13 = 4
            byte r13 = r18[r13]
            int r13 = r13 << 8
            r14 = 65280(0xff00, float:9.1477E-41)
            r13 = r13 & r14
            r0 = r17
            r0.fplength = r13
            r0 = r17
            int r13 = r0.fplength
            r14 = 5
            byte r14 = r18[r14]
            int r13 = r13 + r14
            r0 = r17
            r0.fplength = r13
            com.telpo.tps550.api.idcard.IdentityMsg r6 = new com.telpo.tps550.api.idcard.IdentityMsg
            r6.<init>()
            r9 = 0
            java.lang.String r10 = new java.lang.String     // Catch:{ UnsupportedEncodingException -> 0x02c7 }
            java.lang.String r13 = "UTF16-LE"
            r0 = r18
            r10.<init>(r0, r13)     // Catch:{ UnsupportedEncodingException -> 0x02c7 }
            java.lang.String r9 = new java.lang.String     // Catch:{ UnsupportedEncodingException -> 0x0360 }
            java.lang.String r13 = "UTF-8"
            byte[] r13 = r10.getBytes(r13)     // Catch:{ UnsupportedEncodingException -> 0x0360 }
            r9.<init>(r13)     // Catch:{ UnsupportedEncodingException -> 0x0360 }
        L_0x00a8:
            r13 = 0
            r14 = 60
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setName(r13)
            r13 = 60
            r14 = 61
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setSex(r13)
            r13 = 61
            r14 = 76
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setNo(r13)
            r13 = 76
            r14 = 79
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setCountry(r13)
            r13 = 79
            r14 = 94
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setCn_name(r13)
            r13 = 94
            r14 = 110(0x6e, float:1.54E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setPeriod(r13)
            r13 = 110(0x6e, float:1.54E-43)
            r14 = 118(0x76, float:1.65E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setBorn(r13)
            r13 = 118(0x76, float:1.65E-43)
            r14 = 120(0x78, float:1.68E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setIdcard_version(r13)
            r13 = 120(0x78, float:1.68E-43)
            r14 = 124(0x7c, float:1.74E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setApartment(r13)
            r13 = 124(0x7c, float:1.74E-43)
            r14 = 125(0x7d, float:1.75E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setCard_type(r13)
            r13 = 125(0x7d, float:1.75E-43)
            r14 = 128(0x80, float:1.794E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setReserve(r13)
            r13 = 256(0x100, float:3.59E-43)
            r0 = r18
            int r14 = r0.length
            r0 = r18
            byte[] r13 = java.util.Arrays.copyOfRange(r0, r13, r14)
            r6.setHead_photo(r13)
            java.lang.String r13 = "I"
            java.lang.String r14 = r6.getCard_type()
            boolean r13 = r13.equals(r14)
            if (r13 != 0) goto L_0x020c
            r13 = 0
            r14 = 15
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setName(r13)
            r13 = 15
            r14 = 16
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setSex(r13)
            r13 = 16
            r14 = 18
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r14 = "  "
            boolean r13 = r13.equals(r14)
            if (r13 != 0) goto L_0x016a
            r13 = 16
            r14 = 18
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setNation(r13)
        L_0x016a:
            r13 = 18
            r14 = 26
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setBorn(r13)
            r13 = 26
            r14 = 61
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r13 = r13.trim()
            r6.setAddress(r13)
            r13 = 61
            r14 = 79
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r13 = r13.trim()
            r6.setNo(r13)
            r13 = 79
            r14 = 94
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r13 = r13.trim()
            r6.setApartment(r13)
            r13 = 94
            r14 = 110(0x6e, float:1.54E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setPeriod(r13)
            r13 = 110(0x6e, float:1.54E-43)
            r14 = 119(0x77, float:1.67E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r14 = "         "
            boolean r13 = r13.equals(r14)
            if (r13 != 0) goto L_0x01c8
            r13 = 110(0x6e, float:1.54E-43)
            r14 = 119(0x77, float:1.67E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setPassNum(r13)
        L_0x01c8:
            r13 = 119(0x77, float:1.67E-43)
            r14 = 121(0x79, float:1.7E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r14 = "  "
            boolean r13 = r13.equals(r14)
            if (r13 != 0) goto L_0x01e3
            r13 = 119(0x77, float:1.67E-43)
            r14 = 121(0x79, float:1.7E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setIssuesNum(r13)
        L_0x01e3:
            r13 = 124(0x7c, float:1.74E-43)
            r14 = 125(0x7d, float:1.75E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            java.lang.String r14 = " "
            boolean r13 = r13.equals(r14)
            if (r13 != 0) goto L_0x01fe
            r13 = 124(0x7c, float:1.74E-43)
            r14 = 125(0x7d, float:1.75E-43)
            java.lang.String r13 = r9.substring(r13, r14)
            r6.setCardSignal(r13)
        L_0x01fe:
            r13 = 256(0x100, float:3.59E-43)
            r0 = r18
            int r14 = r0.length
            r0 = r18
            byte[] r13 = java.util.Arrays.copyOfRange(r0, r13, r14)
            r6.setHead_photo(r13)
        L_0x020c:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r13 = r6.getName()
            java.lang.String r12 = r13.trim()
            int r13 = com.telpo.tps550.api.util.ReaderUtils.count_chinese(r12)
            if (r13 == 0) goto L_0x02ff
            int r13 = r12.length()
            r14 = 4
            if (r13 > r14) goto L_0x02dd
            char[] r14 = r12.toCharArray()
            int r15 = r14.length
            r13 = 0
        L_0x022c:
            if (r13 < r15) goto L_0x02cd
        L_0x022e:
            java.lang.String r13 = r1.toString()
            r6.setName(r13)
        L_0x0235:
            java.lang.String r12 = r6.getNation()
            java.lang.String[] r13 = nation_list     // Catch:{ NumberFormatException -> 0x0348 }
            int r14 = java.lang.Integer.parseInt(r12)     // Catch:{ NumberFormatException -> 0x0348 }
            int r14 = r14 + -1
            r13 = r13[r14]     // Catch:{ NumberFormatException -> 0x0348 }
            r6.setNation(r13)     // Catch:{ NumberFormatException -> 0x0348 }
        L_0x0246:
            java.lang.String r12 = r6.getSex()
            java.lang.String r13 = "1"
            boolean r13 = r13.equals(r12)
            if (r13 == 0) goto L_0x034e
            java.lang.String r13 = "男 / M"
            r6.setSex(r13)
        L_0x0257:
            java.lang.String r13 = r6.getBorn()
            java.lang.String r12 = r13.trim()
            int r13 = r12.length()
            r14 = 8
            if (r13 < r14) goto L_0x026e
            java.lang.String r13 = formatDate(r12)
            r6.setBorn(r13)
        L_0x026e:
            java.lang.String r13 = r6.getPeriod()
            java.lang.String r12 = r13.trim()
            int r13 = r12.length()
            r14 = 16
            if (r13 < r14) goto L_0x02ad
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r14 = 0
            r15 = 8
            java.lang.String r14 = r12.substring(r14, r15)
            java.lang.String r14 = formatDate(r14)
            java.lang.String r14 = java.lang.String.valueOf(r14)
            r13.<init>(r14)
            java.lang.String r14 = " - "
            java.lang.StringBuilder r13 = r13.append(r14)
            r14 = 8
            java.lang.String r14 = r12.substring(r14)
            java.lang.String r14 = formatDate(r14)
            java.lang.StringBuilder r13 = r13.append(r14)
            java.lang.String r13 = r13.toString()
            r6.setPeriod(r13)
        L_0x02ad:
            java.lang.String r13 = "I"
            java.lang.String r14 = r6.getCard_type()
            boolean r13 = r13.equals(r14)
            if (r13 == 0) goto L_0x02be
            java.lang.String r13 = "公安部/Ministry of Public Security"
            r6.setApartment(r13)
        L_0x02be:
            java.lang.String r13 = "BlueToothIdCard"
            java.lang.String r14 = "handle data success"
            android.util.Log.d(r13, r14)
            goto L_0x0003
        L_0x02c7:
            r5 = move-exception
        L_0x02c8:
            r5.printStackTrace()
            goto L_0x00a8
        L_0x02cd:
            char r2 = r14[r13]
            r1.append(r2)
            java.lang.String r16 = " "
            r0 = r16
            r1.append(r0)
            int r13 = r13 + 1
            goto L_0x022c
        L_0x02dd:
            int r13 = r12.length()
            r14 = 14
            if (r13 <= r14) goto L_0x022e
            r13 = 0
            r14 = 14
            java.lang.String r13 = r12.substring(r13, r14)
            r1.append(r13)
            java.lang.String r13 = "\n\t\t\t"
            r1.append(r13)
            r13 = 14
            java.lang.String r13 = r12.substring(r13)
            r1.append(r13)
            goto L_0x022e
        L_0x02ff:
            r8 = 26
            int r13 = r12.length()
            r14 = 26
            if (r13 <= r14) goto L_0x0235
            r13 = 0
            r14 = 26
            java.lang.String r11 = r12.substring(r13, r14)
            java.lang.String r13 = " "
            int r3 = r11.lastIndexOf(r13)
            java.lang.String r13 = ","
            int r4 = r11.lastIndexOf(r13)
            r13 = -1
            if (r3 != r13) goto L_0x0322
            r13 = -1
            if (r4 == r13) goto L_0x0325
        L_0x0322:
            if (r3 <= r4) goto L_0x0346
            r8 = r3
        L_0x0325:
            r13 = 0
            int r14 = r8 + 1
            java.lang.String r13 = r12.substring(r13, r14)
            r1.append(r13)
            java.lang.String r13 = "\n\t\t\t"
            r1.append(r13)
            int r13 = r8 + 1
            java.lang.String r13 = r12.substring(r13)
            r1.append(r13)
            java.lang.String r13 = r1.toString()
            r6.setName(r13)
            goto L_0x0235
        L_0x0346:
            r8 = r4
            goto L_0x0325
        L_0x0348:
            r5 = move-exception
            r5.printStackTrace()
            goto L_0x0246
        L_0x034e:
            java.lang.String r13 = "2"
            boolean r13 = r13.equals(r12)
            if (r13 == 0) goto L_0x0257
            java.lang.String r13 = "女 / F"
            r6.setSex(r13)
            goto L_0x0257
        L_0x035d:
            r6 = 0
            goto L_0x0003
        L_0x0360:
            r5 = move-exception
            r9 = r10
            goto L_0x02c8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.idcard.BlueToothIdCard.decodeIdCardBaseInfo(byte[]):com.telpo.tps550.api.idcard.IdentityMsg");
    }

    private static String formatDate(String date) {
        return date.substring(0, 4) + "." + date.substring(4, 6) + "." + date.substring(6, 8);
    }

    public boolean checkConnect() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.isConnect;
    }

    public String getVersion() throws TelpoException {
        sendData = packageCommand(new byte[]{102, 1, 2, 3, -1, 107, 22});
        sendCommand();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (receiveData == null) {
            return null;
        }
        receiveData = Arrays.copyOfRange(receiveData, 5, 7);
        byte left = receiveData[1];
        byte right = receiveData[0];
        Log.d("!@#", String.valueOf(left) + "." + right);
        return String.valueOf(left) + "." + right;
    }
}
