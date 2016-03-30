package com.kon.orientdb.transform.exception;

/**
 * Created by kshevchuk on 9/30/2015.
 */
public class TransformationException extends RuntimeException {
    private static final long serialVersionUID = 1997753363232807009L;

    public TransformationException() {
    }

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(Throwable cause) {
        super(cause);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
