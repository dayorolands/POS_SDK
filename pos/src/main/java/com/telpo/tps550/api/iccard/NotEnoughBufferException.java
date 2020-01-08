package com.telpo.tps550.api.iccard;

public class NotEnoughBufferException extends ICCardException {
    private static final long serialVersionUID = -2405024364586250998L;

    public NotEnoughBufferException() {
    }

    public NotEnoughBufferException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NotEnoughBufferException(String detailMessage) {
        super(detailMessage);
    }

    public NotEnoughBufferException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "The given buffer is not enough for data to receive!";
    }
}
