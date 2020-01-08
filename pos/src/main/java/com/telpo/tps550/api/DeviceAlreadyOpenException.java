package com.telpo.tps550.api;

public class DeviceAlreadyOpenException extends TelpoException {
    private static final long serialVersionUID = 775254919822242857L;

    public DeviceAlreadyOpenException() {
    }

    public DeviceAlreadyOpenException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceAlreadyOpenException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceAlreadyOpenException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Device already opened!";
    }
}
