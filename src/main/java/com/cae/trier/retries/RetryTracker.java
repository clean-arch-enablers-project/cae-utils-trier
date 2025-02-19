package com.cae.trier.retries;

public class RetryTracker {

    public static RetryTracker newOf(RetryPolicy retryPolicy){
        var newTracker = new RetryTracker();
        newTracker.numberOfRetriesTriggered = 0;
        newTracker.maxAmountOfRetriesAllowed = retryPolicy.getMaxAmountOfRetries();
        newTracker.baseTimeInSeconds = retryPolicy.getBaseTimeInSeconds();
        return newTracker;
    }

    private Integer numberOfRetriesTriggered;
    private Integer maxAmountOfRetriesAllowed;
    private Integer baseTimeInSeconds;

    public Integer trackNewAttemptOn(Exception cause){
        if (this.numberOfRetriesTriggered < this.maxAmountOfRetriesAllowed){
            var delayToRetry = (int) ((this.baseTimeInSeconds) * (Math.pow(2, this.numberOfRetriesTriggered)));
            this.numberOfRetriesTriggered += 1;
            RetryNotifier.SINGLETON.emitNewNotification(RetryNotification.of(cause, this.numberOfRetriesTriggered));
            return delayToRetry;
        }
        throw new NoRetriesLeftException(
                "Couldn't proceed with more retries",
                OnExhaustion.FailureStatus.of(cause, this.numberOfRetriesTriggered)
        );
    }

}
