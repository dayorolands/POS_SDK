package com.telpo.tps550.api;

public class InternalErrorException extends TelpoException {
    private static final long serialVersionUID = 8612451431999960519L;

    public InternalErrorException() {
    }

    public InternalErrorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InternalErrorException(String detailMessage) {
        super(detailMessage);
    }

    public InternalErrorException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Unexpected error occur!";
    }
}
