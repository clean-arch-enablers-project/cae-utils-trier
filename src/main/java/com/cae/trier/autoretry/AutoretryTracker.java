package com.cae.trier.autoretry;

public class AutoretryTracker {

    public static AutoretryTracker newOf(AutoretryPolicy autoretryPolicy){
        var newTracker = new AutoretryTracker();
        newTracker.numberOfRetriesTriggered = 0;
        newTracker.maxAmountOfRetriesAllowed = autoretryPolicy.getMaxAmountOfRetries();
        newTracker.baseTimeInSeconds = autoretryPolicy.getBaseTimeInSeconds();
        return newTracker;
    }

    private Integer numberOfRetriesTriggered;
    private Integer maxAmountOfRetriesAllowed;
    private Integer baseTimeInSeconds;

    public Integer trackNewAttemptOn(Exception cause){
        if (this.numberOfRetriesTriggered < this.maxAmountOfRetriesAllowed){
            var delayToRetry = (int) ((this.baseTimeInSeconds) * (Math.pow(2, this.numberOfRetriesTriggered)));
            this.numberOfRetriesTriggered += 1;
            AutoretryNotifier.SINGLETON.emitNewNotification(AutoretryNotification.of(cause, this.numberOfRetriesTriggered));
            return delayToRetry;
        }
        throw new NoRetriesLeftException(
                "Couldn't proceed with more retries",
                OnExhaustion.FailureStatus.of(cause, this.numberOfRetriesTriggered)
        );
    }

}
