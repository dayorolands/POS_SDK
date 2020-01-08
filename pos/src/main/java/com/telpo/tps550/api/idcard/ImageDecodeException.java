package com.telpo.tps550.api.idcard;

import com.telpo.tps550.api.TelpoException;

public class ImageDecodeException extends TelpoException {
    private static final long serialVersionUID = 7316042461528468647L;

    public ImageDecodeException() {
    }

    public ImageDecodeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ImageDecodeException(String detailMessage) {
        super(detailMessage);
    }

    public ImageDecodeException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Failed to decode the image data!";
    }
}
