package com.telpo.tps550.api.iccard;

import com.telpo.tps550.api.TelpoException;

public class ICCardException extends TelpoException {
    private static final long serialVersionUID = 6856857831426865055L;

    public ICCardException() {
    }

    public ICCardException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ICCardException(String detailMessage) {
        super(detailMessage);
    }

    public ICCardException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Exception occur during IC card operation!";
    }
}
