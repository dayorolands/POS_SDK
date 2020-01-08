package com.telpo.tps550.api.printer;

import com.telpo.tps550.api.TelpoException;

public class BlackBlockNotFoundException extends TelpoException {
    private static final long serialVersionUID = 1;

    public BlackBlockNotFoundException() {
    }

    public BlackBlockNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BlackBlockNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public BlackBlockNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "The Printer Cannot Find Black Block!";
    }
}
