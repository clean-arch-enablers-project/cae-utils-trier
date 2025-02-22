package com.cae.trier.autoretry;

public class AutoretryPolicy {

    public static AutoretryPolicy of(Integer maxAmountOfRetries, Integer baseTimeInSeconds){
        return new AutoretryPolicy(maxAmountOfRetries, baseTimeInSeconds);
    }

    private AutoretryPolicy(Integer maxAmountOfRetries, Integer baseTimeInSeconds) {
        this.maxAmountOfRetries = maxAmountOfRetries;
        this.baseTimeInSeconds = baseTimeInSeconds;
        this.autoretryTracker = AutoretryTracker.newOf(this);
    }

    private final Integer maxAmountOfRetries;
    private final Integer baseTimeInSeconds;
    private final AutoretryTracker autoretryTracker;

    public Integer getMaxAmountOfRetries() {
        return this.maxAmountOfRetries;
    }

    public Integer getBaseTimeInSeconds() {
        return this.baseTimeInSeconds;
    }

    public AutoretryTracker getRetryTracker() {
        return this.autoretryTracker;
    }

}
