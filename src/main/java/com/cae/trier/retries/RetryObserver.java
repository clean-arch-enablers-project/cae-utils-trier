package com.cae.trier.retries;

public interface RetryObserver {

    void getNotified(RetryNotification retryNotification);

}
