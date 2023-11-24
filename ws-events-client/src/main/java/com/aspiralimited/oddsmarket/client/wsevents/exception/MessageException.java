package com.aspiralimited.oddsmarket.client.wsevents.exception;

import java.io.IOException;

public class MessageException extends IOException {

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
