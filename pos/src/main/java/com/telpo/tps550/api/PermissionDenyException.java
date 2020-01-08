package com.telpo.tps550.api;

public class PermissionDenyException extends TelpoException {
    private static final long serialVersionUID = 1149990948139378861L;

    public PermissionDenyException() {
    }

    public PermissionDenyException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PermissionDenyException(String detailMessage) {
        super(detailMessage);
    }

    public PermissionDenyException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Permission Deny!";
    }
}
