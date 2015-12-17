package com.lob.lapseface.shared.exception;

public class NotSameArrayLengthException extends Exception {

    private static final String MESSAGE = "Arrays must have the SAME length!";

    public NotSameArrayLengthException() {
        super(MESSAGE);
    }
}
