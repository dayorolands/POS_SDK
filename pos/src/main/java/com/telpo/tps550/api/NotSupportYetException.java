package com.telpo.tps550.api;

public class NotSupportYetException extends TelpoException {
    private static final long serialVersionUID = 2975865503741011861L;

    public NotSupportYetException() {
    }

    public NotSupportYetException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NotSupportYetException(String detailMessage) {
        super(detailMessage);
    }

    public NotSupportYetException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "The operation or the function is not supported for current SDK/Hardware !";
    }
}
