package com.telpo.tps550.api;

public class DeviceOverFlowException extends TelpoException {
    private static final long serialVersionUID = -2720559549044825415L;

    public DeviceOverFlowException() {
    }

    public DeviceOverFlowException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceOverFlowException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceOverFlowException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Memory or buffer over flow!";
    }
}
