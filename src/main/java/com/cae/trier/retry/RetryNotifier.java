package com.cae.trier.retry;

import java.util.concurrent.CopyOnWriteArrayList;

public class RetryNotifier {

    public static final RetryNotifier SINGLETON = new RetryNotifier();

    private RetryNotifier(){}

    private final CopyOnWriteArrayList<RetrySubscriber> interested = new CopyOnWriteArrayList<>();

    public RetryNotifier subscribe(RetrySubscriber retrySubscriber){
        this.interested.add(retrySubscriber);
        return this;
    }

    public void emitNewNotification(RetryNotification notification){
        var executor = RetryNotifierThreadPoolProvider.SINGLETON.getExecutor();
        this.interested.forEach(subscriber -> executor.submit(() -> subscriber.receiveAutoretryNotification(notification)));
    }

}
