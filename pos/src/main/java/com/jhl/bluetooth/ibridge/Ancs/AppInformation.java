package com.jhl.bluetooth.ibridge.Ancs;

import java.util.ArrayList;
import java.util.List;

class AppInformation {
    public String appIdentifier;
    private List<Attribute> attributes = new ArrayList();
    public String displayName;
    public String negativeString;
    public String positiveString;

    AppInformation() {
    }

    public void addAttribute(byte id, byte[] attributeBytes) {
        this.attributes.add(new Attribute(id, attributeBytes));
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    public Attribute getAttribute(byte attributeID) {
        Attribute attributeFound = null;
        for (Attribute attribute : this.attributes) {
            if (attribute.id == attributeID) {
                attributeFound = attribute;
            }
        }
        return attributeFound;
    }

    public String toString() {
        String string = "".concat("appIdentifier=" + this.appIdentifier + ";").concat("attributes=");
        for (Attribute attribute : this.attributes) {
            string = string.concat("<" + attribute.toString() + ">");
        }
        return string;
    }
}
