package com.cae.trier.retries;

public class RetryNotification {

    public static RetryNotification of(
            Exception cause,
            Integer totalAtThisPoint){
        return new RetryNotification(cause, totalAtThisPoint);
    }

    protected RetryNotification(
            Exception cause,
            Integer totalAtThisPoint) {
        this.cause = cause;
        this.totalOfRetriesAtThisPoint = totalAtThisPoint;
    }

    private final Exception cause;
    private final Integer totalOfRetriesAtThisPoint;

    public Exception getCause() {
        return this.cause;
    }

    public Integer getTotalOfRetriesAtThisPoint() {
        return this.totalOfRetriesAtThisPoint;
    }

    @Override
    public String toString() {
        return "Retried up to this point " + this.getTotalOfRetriesAtThisPoint() + " times on the following exception: " + this.getCause();
    }
}
