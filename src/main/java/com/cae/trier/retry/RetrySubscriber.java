package com.cae.trier.retry;

public interface RetrySubscriber {

    void receiveRetryNotification(RetryNotification retryNotification);

}
