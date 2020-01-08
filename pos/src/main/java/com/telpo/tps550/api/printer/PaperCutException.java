package com.telpo.tps550.api.printer;

import com.telpo.tps550.api.TelpoException;

public class PaperCutException extends TelpoException {
    private static final long serialVersionUID = 9014308459626928976L;

    public PaperCutException() {
    }

    public PaperCutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PaperCutException(String detailMessage) {
        super(detailMessage);
    }

    public PaperCutException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "A error has happened when cutting paper";
    }
}
