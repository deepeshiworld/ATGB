package com.flock.atgb.exception;

/**
 * Created by B0095829 on 4/1/17.
 */
public class FlockException extends Exception {
    public FlockException(String message) {
        super(message);
    }

    public FlockException(Throwable thr) {
        super(thr);
    }

    public FlockException(String errorMsg, Throwable thr) {
        super(errorMsg, thr);
    }
}
