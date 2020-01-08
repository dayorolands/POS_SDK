package com.telpo.tps550.api.printer;

import com.telpo.tps550.api.TelpoException;

public class GateOpenException extends TelpoException {
    private static final long serialVersionUID = 9004308459626928976L;

    public GateOpenException() {
    }

    public GateOpenException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public GateOpenException(String detailMessage) {
        super(detailMessage);
    }

    public GateOpenException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "The gate is opened";
    }
}
