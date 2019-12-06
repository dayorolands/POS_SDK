package com.jhl.bluetooth.ibridge.Ancs;

import android.util.Log;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

import java.util.ArrayList;
import java.util.List;

class GetAppAttributesCommand {
    public static byte CommandID = 1;
    public String appIdentifier;
    private List<AttributeID> attributeIDs;

    public static GetAppAttributesCommand parse(byte[] format) {
        GetAppAttributesCommand getAppAttributesCommand = null;
        int appIdentifierLength = 0;
        if (format != null && format.length > 0 && format[0] == CommandID) {
            getAppAttributesCommand = new GetAppAttributesCommand();
            int position = 0 + 1;
            int i = position;
            while (true) {
                if (i >= format.length) {
                    break;
                }
                int i2 = i + 1;
                if (format[i] == 0) {
                    i = i2;
                    break;
                }
                appIdentifierLength++;
                i = i2;
            }
            if (i == format.length) {
                Log.i("GetAppAttributesCommand", "bad format:appIdentifier not completed");
                return null;
            }
            byte[] appIdentifierBytes = new byte[appIdentifierLength];
            System.arraycopy(format, position, appIdentifierBytes, 0, appIdentifierLength);
            getAppAttributesCommand.appIdentifier = new String(appIdentifierBytes);
            int position2 = getAppAttributesCommand.appIdentifier.length() + 1 + 1;
            while (position2 < format.length) {
                AttributeID attributeID = new AttributeID();
                attributeID.id = format[position2];
                position2++;
                switch (attributeID.id) {
                    case 1:
                    case 2:
                    case 3:
                        if (position2 + 2 <= format.length) {
                            attributeID.maxLength = SystemUtils.byteArray2Int(format, position2, 2);
                            position2 += 2;
                            break;
                        } else {
                            Log.i("GetAppAttributesCommand", "no max length field found");
                            return null;
                        }
                }
                getAppAttributesCommand.getAttributeIDs().add(attributeID);
            }
        }
        return getAppAttributesCommand;
    }

    public GetAppAttributesCommand() {
        this.appIdentifier = null;
        this.attributeIDs = null;
        this.attributeIDs = new ArrayList();
    }

    public void addAttributeID(byte id, byte maxLength) {
        this.attributeIDs.add(new AttributeID(id, maxLength));
    }

    public List<AttributeID> getAttributeIDs() {
        return this.attributeIDs;
    }

    public byte[] build() {
        byte[] format = new byte[(this.appIdentifier.length() + 1 + 1 + getAttributeIDsLength())];
        format[0] = CommandID;
        System.arraycopy(this.appIdentifier.getBytes(), 0, format, 0 + 1, this.appIdentifier.length());
        int position = this.appIdentifier.length() + 1;
        format[position] = 0;
        int position2 = position + 1;
        for (AttributeID attributeID : this.attributeIDs) {
            format[position2] = attributeID.id;
            position2++;
            switch (attributeID.id) {
                case 1:
                case 2:
                case 3:
                    SystemUtils.int2ByteArray(attributeID.maxLength, format, position2, 2);
                    position2 += 2;
                    break;
            }
        }
        return format;
    }

    public String toString() {
        String string = "".concat("appIdentifier=" + this.appIdentifier + ";").concat("attributeIDs=");
        for (AttributeID attributeID : this.attributeIDs) {
            string = string.concat("<" + attributeID.toString() + ">");
        }
        return string;
    }

    private int getAttributeIDsLength() {
        int attributeIDsLength = 0;
        for (AttributeID attributeID : this.attributeIDs) {
            attributeIDsLength++;
            switch (attributeID.id) {
                case 1:
                case 2:
                case 3:
                    attributeIDsLength += 2;
                    break;
            }
        }
        return attributeIDsLength;
    }
}
