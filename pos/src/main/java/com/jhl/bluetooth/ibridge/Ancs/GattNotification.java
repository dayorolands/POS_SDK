package com.jhl.bluetooth.ibridge.Ancs;

import java.util.ArrayList;
import java.util.List;

class GattNotification {
    List<Attribute> attributes = new ArrayList();
    public byte categoryCount = 0;
    public byte categoryID = 0;
    public byte eventFlags = 0;
    public byte eventID = 0;
    public int notificationUID = 0;

    public void addAttribute(byte id, byte[] attributeBytes) {
        if (attributeBytes != null) {
            this.attributes.add(new Attribute(id, attributeBytes));
        }
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
        String string = "".concat("notificationUID=" + this.notificationUID + ";").concat("eventID=" + AncsUtils.getEventIDString(this.eventID) + ";").concat("eventFlags=" + AncsUtils.getEventFlags(this.eventFlags) + ";").concat("categoryID=" + AncsUtils.getCategoryIDString(this.categoryID) + ";").concat("categoryCount=" + this.categoryCount + ";").concat("attributes=");
        for (Attribute attribute : this.attributes) {
            string = string.concat("<" + attribute.toString() + ">");
        }
        return string;
    }
}
