package com.jhl.bluetooth.ibridge.Ancs;

class Attribute {
    public byte[] attribute;
    public byte id;
    public int length;

    public Attribute(int length2) {
        this.length = length2;
        this.attribute = new byte[length2];
    }

    public Attribute(byte id2, byte[] attribute2) {
        this.id = id2;
        this.length = attribute2.length;
        this.attribute = new byte[this.length];
        System.arraycopy(attribute2, 0, this.attribute, 0, this.length);
    }

    public String toString() {
        return "".concat("id=" + this.id + ";").concat("length=" + this.length + ";").concat("attribute=" + new String(this.attribute));
    }
}
