package com.telpo.tps550.api.iccard;

public class NoAuthorityCardException extends ICCardException {
    private static final long serialVersionUID = 7101656189764787793L;

    public NoAuthorityCardException() {
    }

    public NoAuthorityCardException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoAuthorityCardException(String detailMessage) {
        super(detailMessage);
    }

    public NoAuthorityCardException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Current sector has not been authorized!";
    }
}
