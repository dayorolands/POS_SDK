package com.telpo.tps550.api.printer;

import com.telpo.tps550.api.TelpoException;

public class NoPaperException extends TelpoException {
    private static final long serialVersionUID = 9004308459676928976L;

    public NoPaperException() {
    }

    public NoPaperException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoPaperException(String detailMessage) {
        super(detailMessage);
    }

    public NoPaperException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "The printer paper out!";
    }
}
