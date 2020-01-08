package com.telpo.tps550.api;

public class TimeoutException extends TelpoException {
    private static final long serialVersionUID = 8323068427519122516L;

    public TimeoutException() {
    }

    public TimeoutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TimeoutException(String detailMessage) {
        super(detailMessage);
    }

    public TimeoutException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Operation timeout!";
    }
}
