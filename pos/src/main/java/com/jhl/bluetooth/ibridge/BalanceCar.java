package com.jhl.bluetooth.ibridge;

import android.util.Log;

public class BalanceCar {
    public static final byte COMMAND_ID_GET_DEVICE_INFORMATION = 3;
    public static final byte COMMAND_ID_POWER_CONTROL = 0;
    public static final byte COMMAND_ID_SECURITYODE_CONTROL = 1;
    public static final byte COMMAND_ID_SET_MODE = 2;
    public static final byte EVENT_ID_DEVICE_INFORMATION = 3;
    public static final byte EVENT_ID_MODE_CHANGED = 2;
    public static final byte EVENT_ID_POWER_STATE = 0;
    public static final byte EVENT_ID_SECURITYODE_CHANGED = 1;
    public static final byte EVENT_ID_SPEED_CHANGED = 4;
    public static final byte MODE_BEGINNER = 0;
    public static final byte MODE_ENTERTAINMENT = 2;
    public static final byte MODE_PLAYER = 1;
    private static final byte PACKET_START_BYTE = -86;
    public static final byte POWER_STATE_OFF = 0;
    public static final byte POWER_STATE_ON = 1;
    public static final byte SECURITY_MODE_OFF = 0;
    public static final byte SECURITY_MODE_ON = 1;
    public int battery;
    public String deviceId;
    public String deviceName;
    public int error;
    private EventReceiver eventReceiver;
    public boolean inSecurityMode;
    private byte[] leftBuffer = null;
    public int mileage;
    public byte mode;
    public boolean on;
    public int speed;

    public interface EventReceiver {
        void onDeviceInformationGot(BalanceCar balanceCar);

        void onEnterSecurityMode(BalanceCar balanceCar);

        void onLeaveSecurityMode(BalanceCar balanceCar);

        void onModeChanged(BalanceCar balanceCar, byte b);

        void onPowerOff(BalanceCar balanceCar);

        void onPowerOn(BalanceCar balanceCar);

        void onSpeedChanged(BalanceCar balanceCar, int i);

        void sendData(byte[] bArr);
    }

    public void registerEventReceiver(EventReceiver eventReceiver2) {
        this.eventReceiver = eventReceiver2;
    }

    public void dataIn(byte[] data) {
        byte[] buffer;
        if (this.leftBuffer == null || this.leftBuffer.length <= 0) {
            buffer = data;
        } else if (data != null) {
            buffer = new byte[(this.leftBuffer.length + data.length)];
            System.arraycopy(this.leftBuffer, 0, buffer, 0, this.leftBuffer.length);
            System.arraycopy(data, 0, buffer, this.leftBuffer.length, data.length);
        } else {
            buffer = this.leftBuffer;
        }
        int position = 0;
        while (position < buffer.length && buffer[position] != -86) {
            position++;
        }
        if (buffer.length <= position) {
            this.leftBuffer = null;
            if (buffer.length > 0) {
                Log.w("BalanceCar", "PACKET_START_BYTE not found, discard");
            }
        } else if (buffer.length - position <= 2 || buffer.length - position < buffer[position + 1] + 2) {
            this.leftBuffer = new byte[(buffer.length - position)];
            System.arraycopy(buffer, position, this.leftBuffer, 0, buffer.length - position);
        } else {
            int packetLength = buffer[position + 1] + 3;
            parse(buffer, position, packetLength);
            this.leftBuffer = new byte[((buffer.length - packetLength) - position)];
            System.arraycopy(buffer, position + packetLength, this.leftBuffer, 0, (buffer.length - packetLength) - position);
            dataIn(null);
        }
    }

    private boolean parse(byte[] packet, int start, int length) {
        int position = start + 1;
        byte sum = 0;
        for (int i = 0; i < getUnsignedByte(packet[position]) + 1; i++) {
            sum = (byte) (packet[position + i] + sum);
        }
        if (sum != packet[length - 1]) {
            Log.w("BalanceCar", "Check sum fail");
            return false;
        }
        int position2 = position + 1;
        byte eventId = packet[position2];
        int position3 = position2 + 1;
        switch (eventId) {
            case 0:
                this.on = packet[position3] == 1;
                position3++;
                if (!this.on) {
                    this.eventReceiver.onPowerOff(this);
                    break;
                } else {
                    this.eventReceiver.onPowerOn(this);
                    break;
                }
            case 1:
                this.inSecurityMode = packet[position3] == 1;
                position3++;
                if (!this.inSecurityMode) {
                    this.eventReceiver.onLeaveSecurityMode(this);
                    break;
                } else {
                    this.eventReceiver.onEnterSecurityMode(this);
                    break;
                }
            case 2:
                this.mode = packet[position3];
                position3++;
                this.eventReceiver.onModeChanged(this, this.mode);
                break;
            case 3:
                int lengthOfDeviceId = getUnsignedByte(packet[position3]);
                int position4 = position3 + 1;
                byte[] deviceIdBytes = new byte[lengthOfDeviceId];
                System.arraycopy(packet, position4, deviceIdBytes, 0, lengthOfDeviceId);
                this.deviceId = new String(deviceIdBytes);
                int position5 = position4 + lengthOfDeviceId;
                int lengthOfDeviceName = getUnsignedByte(packet[position5]);
                int position6 = position5 + 1;
                byte[] deviceNameBytes = new byte[lengthOfDeviceName];
                System.arraycopy(packet, position6, deviceNameBytes, 0, lengthOfDeviceName);
                this.deviceName = new String(deviceNameBytes);
                int position7 = position6 + lengthOfDeviceName;
                this.battery = packet[position7];
                int position8 = position7 + 1;
                this.mileage = getUnsignedByte(packet[position8]);
                int position9 = position8 + 1;
                this.mileage += getUnsignedByte(packet[position9]) << 8;
                int position10 = position9 + 1;
                this.speed = getUnsignedByte(packet[position10]);
                int position11 = position10 + 1;
                this.speed += getUnsignedByte(packet[position11]) << 8;
                int position12 = position11 + 1;
                this.mode = packet[position12];
                int position13 = position12 + 1;
                this.on = packet[position13] == 1;
                int position14 = position13 + 1;
                this.inSecurityMode = packet[position14] == 1;
                position3 = position14 + 1;
                this.eventReceiver.onDeviceInformationGot(this);
                break;
            case 4:
                this.speed = packet[position3];
                int position15 = position3 + 1;
                this.speed += packet[position15] << 8;
                position3 = position15 + 1;
                this.eventReceiver.onSpeedChanged(this, this.speed);
                break;
        }
        byte b = packet[position3];
        int position16 = position3 + 1;
        return true;
    }

    public void turnOn() {
        byte[] format = new byte[5];
        format[0] = PACKET_START_BYTE;
        int position = 0 + 1;
        format[position] = 2;
        int position2 = position + 1;
        format[position2] = 0;
        int position3 = position2 + 1;
        format[position3] = 1;
        int position4 = position3 + 1;
        format[position4] = 0;
        for (int i = 1; i < position4; i++) {
            format[position4] = (byte) (format[position4] + format[i]);
        }
        this.eventReceiver.sendData(format);
    }

    public void turnOff() {
        byte[] format = new byte[5];
        format[0] = PACKET_START_BYTE;
        int position = 0 + 1;
        format[position] = 2;
        int position2 = position + 1;
        format[position2] = 0;
        int position3 = position2 + 1;
        format[position3] = 1;
        int position4 = position3 + 1;
        format[position4] = 0;
        for (int i = 1; i < position4; i++) {
            format[position4] = (byte) (format[position4] + format[i]);
        }
        this.eventReceiver.sendData(format);
    }

    public void setMode(byte mode2) {
        byte[] format = new byte[5];
        format[0] = PACKET_START_BYTE;
        int position = 0 + 1;
        format[position] = 2;
        int position2 = position + 1;
        format[position2] = 2;
        int position3 = position2 + 1;
        format[position3] = mode2;
        int position4 = position3 + 1;
        format[position4] = 0;
        for (int i = 1; i < position4; i++) {
            format[position4] = (byte) (format[position4] + format[i]);
        }
        this.eventReceiver.sendData(format);
    }

    public void enterSecurityMode() {
        byte[] format = new byte[5];
        format[0] = PACKET_START_BYTE;
        int position = 0 + 1;
        format[position] = 2;
        int position2 = position + 1;
        format[position2] = 1;
        int position3 = position2 + 1;
        format[position3] = 1;
        int position4 = position3 + 1;
        format[position4] = 0;
        for (int i = 1; i < position4; i++) {
            format[position4] = (byte) (format[position4] + format[i]);
        }
        this.eventReceiver.sendData(format);
    }

    public void leaveSecurityMode() {
        byte[] format = new byte[5];
        format[0] = PACKET_START_BYTE;
        int position = 0 + 1;
        format[position] = 2;
        int position2 = position + 1;
        format[position2] = 1;
        int position3 = position2 + 1;
        format[position3] = 0;
        int position4 = position3 + 1;
        format[position4] = 0;
        for (int i = 1; i < position4; i++) {
            format[position4] = (byte) (format[position4] + format[i]);
        }
        this.eventReceiver.sendData(format);
    }

    public void getDeviceInformation() {
        byte[] format = new byte[4];
        format[0] = PACKET_START_BYTE;
        int position = 0 + 1;
        format[position] = 1;
        int position2 = position + 1;
        format[position2] = 3;
        int position3 = position2 + 1;
        format[position3] = 0;
        for (int i = 1; i < position3; i++) {
            format[position3] = (byte) (format[position3] + format[i]);
        }
        this.eventReceiver.sendData(format);
    }

    private int getUnsignedByte(byte data) {
        return data & 255;
    }
}
