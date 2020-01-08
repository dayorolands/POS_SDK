package com.telpo.tps550.api;

public class DeviceNotFoundException extends TelpoException {
    private static final long serialVersionUID = 1;

    public DeviceNotFoundException() {
    }

    public DeviceNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Cannot find device!";
    }
}
