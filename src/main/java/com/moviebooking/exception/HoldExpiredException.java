package com.moviebooking.exception;

public class HoldExpiredException extends RuntimeException {

    public HoldExpiredException(String message) {
        super(message);
    }
}
