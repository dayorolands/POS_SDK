package com.jhl.bluetooth.ibridge.Ancs;

class AttributeID {
    public byte id;
    public int maxLength;

    public AttributeID() {
    }

    public AttributeID(byte id2, int maxLength2) {
        this.id = id2;
        this.maxLength = maxLength2;
    }

    public String toString() {
        return "".concat("id=" + this.id + ";").concat("maxLength=" + this.maxLength);
    }
}
