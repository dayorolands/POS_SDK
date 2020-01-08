package com.telpo.tps550.api.printer;

import com.telpo.tps550.api.TelpoException;

public class OverHeatException extends TelpoException {
    private static final long serialVersionUID = 8044094932145456087L;

    public OverHeatException() {
    }

    public OverHeatException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public OverHeatException(String detailMessage) {
        super(detailMessage);
    }

    public OverHeatException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "The printer is overheating!";
    }
}
