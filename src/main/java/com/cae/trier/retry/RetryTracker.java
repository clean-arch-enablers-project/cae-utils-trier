package com.cae.trier.retry;

public class RetryTracker {

    public static RetryTracker newOf(RetryPolicy retryPolicy){
        var newTracker = new RetryTracker();
        newTracker.numberOfRetriesTriggered = 0;
        newTracker.maxAmountOfRetriesAllowed = retryPolicy.getMaxAmountOfRetries();
        newTracker.baseTime = retryPolicy.getBaseTime();
        return newTracker;
    }

    private Integer numberOfRetriesTriggered;
    private Integer maxAmountOfRetriesAllowed;
    private Long baseTime;

    public Long trackNewAttemptOn(Exception cause){
        if (this.numberOfRetriesTriggered < this.maxAmountOfRetriesAllowed){
            var delayToRetry = ((this.baseTime) * (Math.pow(2, this.numberOfRetriesTriggered)));
            this.numberOfRetriesTriggered += 1;
            RetryNotifier.SINGLETON.emitNewNotification(RetryNotification.of(cause, this.numberOfRetriesTriggered));
            return (long) delayToRetry;
        }
        throw new NoRetriesLeftException(
                "Couldn't proceed with more retries",
                OnExhaustion.FailureStatus.of(cause, this.numberOfRetriesTriggered)
        );
    }

}
