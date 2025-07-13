package com.cae.trier.retry;

public interface RetrySubscriber {

    void receiveAutoretryNotification(RetryNotification retryNotification);

}
