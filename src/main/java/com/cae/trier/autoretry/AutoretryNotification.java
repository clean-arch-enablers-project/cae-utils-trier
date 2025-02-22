package com.cae.trier.autoretry;

public class AutoretryNotification {

    public static AutoretryNotification of(
            Exception cause,
            Integer totalAtThisPoint){
        return new AutoretryNotification(cause, totalAtThisPoint);
    }

    protected AutoretryNotification(
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
