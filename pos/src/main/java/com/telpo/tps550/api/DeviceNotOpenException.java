package com.telpo.tps550.api;

public class DeviceNotOpenException extends TelpoException {
    private static final long serialVersionUID = -3693748800173552918L;

    public DeviceNotOpenException() {
    }

    public DeviceNotOpenException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceNotOpenException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceNotOpenException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Device not open!";
    }
}
