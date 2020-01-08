package com.telpo.tps550.api.iccard;

public class RemovedCardException extends ICCardException {
    private static final long serialVersionUID = -3487809352556097894L;

    public RemovedCardException() {
    }

    public RemovedCardException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RemovedCardException(String detailMessage) {
        super(detailMessage);
    }

    public RemovedCardException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Cannot find a valid IC card!";
    }
}
