package com.jhl.bluetooth.ibridge.Ancs;

import android.util.Log;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

import java.util.ArrayList;
import java.util.List;

class GetNotificationAttributesResponse {
    public static byte CommandID = 0;
    private List<Attribute> attributes;
    public int notificationUID;

    public GetNotificationAttributesResponse() {
        this.attributes = null;
        this.attributes = new ArrayList();
    }

    public void addAttribute(byte id, byte[] attributeBytes) {
        Log.i("addAttribute", "id=" + id + "attributeBytes length=" + attributeBytes.length);
        Attribute attribute = new Attribute(id, attributeBytes);
        Log.i("addAttribute", "attribute=" + attribute.toString());
        this.attributes.add(attribute);
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    public byte[] getAttribute(byte id) {
        byte[] attributeData = null;
        for (Attribute attribute : this.attributes) {
            if (attribute.id == id) {
                attributeData = attribute.attribute;
            }
        }
        return attributeData;
    }

    public static GetNotificationAttributesResponse parse(byte[] format) {
        GetNotificationAttributesResponse getNotificationAttributesResponse = null;
        if (format != null && format.length > 0 && format[0] == CommandID) {
            getNotificationAttributesResponse = new GetNotificationAttributesResponse();
            int position = 0 + 1;
            getNotificationAttributesResponse.notificationUID = SystemUtils.byteArray2Int(format, position, 4);
            int position2 = position + 4;
            while (position2 < format.length) {
                byte b = format[position2];
                int position3 = position2 + 1;
                int length = SystemUtils.byteArray2Int(format, position3, 2);
                int position4 = position3 + 2;
                Attribute attribute = new Attribute(length);
                System.arraycopy(format, position4, attribute.attribute, 0, attribute.length);
                position2 = position4 + attribute.length;
                getNotificationAttributesResponse.getAttributes().add(attribute);
            }
        }
        return getNotificationAttributesResponse;
    }

    public byte[] build() {
        byte[] format = new byte[(getAttributesLength() + 5)];
        format[0] = CommandID;
        int position = 0 + 1;
        SystemUtils.int2ByteArray(this.notificationUID, format, position, 4);
        int position2 = position + 4;
        for (Attribute attribute : this.attributes) {
            format[position2] = attribute.id;
            int position3 = position2 + 1;
            SystemUtils.int2ByteArray(attribute.length, format, position3, 2);
            int position4 = position3 + 2;
            System.arraycopy(attribute.attribute, 0, format, position4, attribute.length);
            position2 = position4 + attribute.length;
        }
        return format;
    }

    public String toString() {
        String string = "".concat("CommandID=" + AncsUtils.getCommandIDString(CommandID) + ";").concat("notificationUID=" + this.notificationUID + ";").concat("attributes=");
        for (Attribute attribute : this.attributes) {
            string = string.concat("<" + attribute.toString() + ">");
        }
        return string;
    }

    private int getAttributesLength() {
        int attributesLength = 0;
        for (Attribute attribute : this.attributes) {
            attributesLength = attributesLength + 1 + 2 + attribute.length;
        }
        return attributesLength;
    }
}
