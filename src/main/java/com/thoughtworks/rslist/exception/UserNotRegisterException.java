package com.thoughtworks.rslist.exception;

public class UserNotRegisterException extends Exception {
    private String error;

    public UserNotRegisterException(String error) {
        this.error = error;
    }

    public String getMessage() {
        return error;
    }
}
