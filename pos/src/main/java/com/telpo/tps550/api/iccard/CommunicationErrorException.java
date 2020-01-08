package com.telpo.tps550.api.iccard;

public class CommunicationErrorException extends ICCardException {
    private static final long serialVersionUID = 4679295633927227471L;

    public CommunicationErrorException() {
    }

    public CommunicationErrorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CommunicationErrorException(String detailMessage) {
        super(detailMessage);
    }

    public CommunicationErrorException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Communition error!";
    }
}
