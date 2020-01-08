package com.telpo.tps550.api.idcard;

import com.telpo.tps550.api.TelpoException;

public class IdCardNotReadSnException extends TelpoException {
    private static final long serialVersionUID = 1323450920926209145L;

    public IdCardNotReadSnException() {
    }

    public IdCardNotReadSnException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public IdCardNotReadSnException(String detailMessage) {
        super(detailMessage);
    }

    public IdCardNotReadSnException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Id Card SN Read Fail!";
    }
}
