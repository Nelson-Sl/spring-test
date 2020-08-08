package com.thoughtworks.rslist.exception;

public class RsTradeFailureException extends Exception {
    private String error;

    public RsTradeFailureException(String error) {
        this.error = error;
    }

    public String getMessage() {
        return error;
    }
}
