package com.jhl.bluetooth.ibridge.Ancs;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

import java.util.ArrayList;
import java.util.List;

class GetAppAttributesResponse {
    public static byte CommandID = 1;
    public String appIdentifier;
    private List<Attribute> attributes;

    public GetAppAttributesResponse() {
        this.attributes = null;
        this.attributes = new ArrayList();
    }

    public void addAttribute(byte id, byte[] attributeBytes) {
        this.attributes.add(new Attribute(id, attributeBytes));
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

    public static GetAppAttributesResponse parse(byte[] format) {
        GetAppAttributesResponse getAppAttributesResponse = null;
        if (format != null && format.length > 0 && format[0] == CommandID) {
            int appIdentifierLength = 0;
            getAppAttributesResponse = new GetAppAttributesResponse();
            int position = 0 + 1;
            int i = position;
            while (true) {
                int i2 = i + 1;
                if (format[i] == 0) {
                    break;
                }
                appIdentifierLength++;
                i = i2;
            }
            byte[] appIdentifierBytes = new byte[appIdentifierLength];
            System.arraycopy(format, position, appIdentifierBytes, 0, appIdentifierLength);
            getAppAttributesResponse.appIdentifier = new String(appIdentifierBytes);
            int position2 = getAppAttributesResponse.appIdentifier.length() + 1 + 1;
            while (position2 < format.length) {
                byte b = format[position2];
                int position3 = position2 + 1;
                int length = SystemUtils.byteArray2Int(format, position3, 2);
                int position4 = position3 + 2;
                Attribute attribute = new Attribute(length);
                System.arraycopy(format, position4, attribute.attribute, 0, attribute.length);
                position2 = position4 + attribute.length;
                getAppAttributesResponse.getAttributes().add(attribute);
            }
        }
        return getAppAttributesResponse;
    }

    public byte[] build() {
        byte[] format = new byte[(this.appIdentifier.length() + 1 + 1 + getAttributesLength())];
        format[0] = CommandID;
        System.arraycopy(this.appIdentifier.getBytes(), 0, format, 0 + 1, this.appIdentifier.length());
        int position = this.appIdentifier.length() + 1;
        format[position] = 0;
        int position2 = position + 1;
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
        String string = "".concat("CommandID=" + AncsUtils.getCommandIDString(CommandID) + ";").concat("attributes=");
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
