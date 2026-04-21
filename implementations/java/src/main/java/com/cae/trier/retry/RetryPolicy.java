package com.cae.trier.retry;

import java.util.concurrent.TimeUnit;

public class RetryPolicy {

    public static RetryPolicy of(Integer maxAmountOfRetries, Long baseTime, TimeUnit timeUnit){
        return new RetryPolicy(maxAmountOfRetries, baseTime, timeUnit);
    }

    private RetryPolicy(Integer maxAmountOfRetries, Long baseTime, TimeUnit timeUnit) {
        this.maxAmountOfRetries = maxAmountOfRetries;
        this.baseTime = baseTime;
        this.timeUnit = timeUnit;
        this.retryTracker = RetryTracker.newOf(this);
    }

    private final Integer maxAmountOfRetries;
    private final Long baseTime;
    private final TimeUnit timeUnit;
    private final RetryTracker retryTracker;

    public Integer getMaxAmountOfRetries() {
        return this.maxAmountOfRetries;
    }

    public Long getBaseTime() {
        return this.baseTime;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public RetryTracker getRetryTracker() {
        return this.retryTracker;
    }

}
