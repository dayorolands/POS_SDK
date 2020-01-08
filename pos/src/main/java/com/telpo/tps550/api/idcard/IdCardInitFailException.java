package com.telpo.tps550.api.idcard;

import com.telpo.tps550.api.TelpoException;

public class IdCardInitFailException extends TelpoException {
    private static final long serialVersionUID = -1629955331916689474L;

    public IdCardInitFailException() {
    }

    public IdCardInitFailException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public IdCardInitFailException(String detailMessage) {
        super(detailMessage);
    }

    public IdCardInitFailException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Failed to init the decode library!";
    }
}
