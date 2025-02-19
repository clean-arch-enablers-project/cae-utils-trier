package com.cae.trier.retries;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class RetryNotifier {

    public static final RetryNotifier SINGLETON = new RetryNotifier();
    private static final ExecutorService EXECUTOR = RetryNotifierThreadPoolProvider.SINGLETON.getExecutor();

    private RetryNotifier(){}

    private final CopyOnWriteArrayList<RetryObserver> interested = new CopyOnWriteArrayList<>();

    public RetryNotifier subscribe(RetryObserver retryObserver){
        this.interested.add(retryObserver);
        return this;
    }

    public void emitNewNotification(RetryNotification notification){
        this.interested.forEach(subscriber -> CompletableFuture.runAsync(() -> subscriber.getNotified(notification), EXECUTOR));
    }

}
