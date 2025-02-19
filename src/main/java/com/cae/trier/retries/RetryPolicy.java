package com.cae.trier.retries;

public class RetryPolicy {

    public static RetryPolicy of(Integer maxAmountOfRetries, Integer baseTimeInSeconds){
        return new RetryPolicy(maxAmountOfRetries, baseTimeInSeconds);
    }

    private RetryPolicy(Integer maxAmountOfRetries, Integer baseTimeInSeconds) {
        this.maxAmountOfRetries = maxAmountOfRetries;
        this.baseTimeInSeconds = baseTimeInSeconds;
        this.retryTracker = RetryTracker.newOf(this);
    }

    private final Integer maxAmountOfRetries;
    private final Integer baseTimeInSeconds;
    private final RetryTracker retryTracker;

    public Integer getMaxAmountOfRetries() {
        return this.maxAmountOfRetries;
    }

    public Integer getBaseTimeInSeconds() {
        return this.baseTimeInSeconds;
    }

    public RetryTracker getRetryTracker() {
        return this.retryTracker;
    }

}
