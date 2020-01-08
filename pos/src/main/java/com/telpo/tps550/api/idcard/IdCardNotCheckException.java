package com.telpo.tps550.api.idcard;

import com.telpo.tps550.api.TelpoException;

public class IdCardNotCheckException extends TelpoException {
    private static final long serialVersionUID = 1234509209262091485L;

    public IdCardNotCheckException() {
    }

    public IdCardNotCheckException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public IdCardNotCheckException(String detailMessage) {
        super(detailMessage);
    }

    public IdCardNotCheckException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Id Card infomation has not been checked!";
    }
}
