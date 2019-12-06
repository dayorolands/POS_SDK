package com.jhl.bluetooth.ibridge.Ancs;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

import java.util.ArrayList;
import java.util.List;

class GetNotificationAttributesCommand {
    public static byte CommandID = 0;
    private List<AttributeID> attributeIDs;
    public int notificationUID;

    public static GetNotificationAttributesCommand parse(byte[] format) {
        GetNotificationAttributesCommand getNotificationAttributesCommand = null;
        if (format != null && format.length > 0 && format[0] == CommandID) {
            getNotificationAttributesCommand = new GetNotificationAttributesCommand();
            int position = 0 + 1;
            getNotificationAttributesCommand.notificationUID = SystemUtils.byteArray2Int(format, position, 4);
            int position2 = position + 4;
            while (position2 < format.length) {
                AttributeID attributeID = new AttributeID();
                attributeID.id = format[position2];
                position2++;
                switch (attributeID.id) {
                    case 1:
                    case 2:
                    case 3:
                        attributeID.maxLength = SystemUtils.byteArray2Int(format, position2, 2);
                        position2 += 2;
                        break;
                }
                getNotificationAttributesCommand.getAttributeIDs().add(attributeID);
            }
        }
        return getNotificationAttributesCommand;
    }

    public GetNotificationAttributesCommand() {
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
        byte[] format = new byte[(getAttributeIDsLength() + 5)];
        format[0] = CommandID;
        int position = 0 + 1;
        SystemUtils.int2ByteArray(this.notificationUID, format, position, 4);
        int position2 = position + 4;
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
        String string = "".concat("CommandID=" + AncsUtils.getCommandIDString(CommandID) + ";").concat("notificationUID=" + this.notificationUID + ";").concat("attributeIDs=");
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
