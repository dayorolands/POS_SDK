package com.telpo.tps550.api.iccard;

public class AuthorityFailException extends ICCardException {
    private static final long serialVersionUID = 7101656189764787793L;

    public AuthorityFailException() {
    }

    public AuthorityFailException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AuthorityFailException(String detailMessage) {
        super(detailMessage);
    }

    public AuthorityFailException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Failed to authority the special sector!";
    }
}
