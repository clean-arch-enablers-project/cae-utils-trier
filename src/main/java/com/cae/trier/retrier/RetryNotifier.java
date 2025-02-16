package com.cae.trier.retrier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class RetryNotifier {

    public static final RetryNotifier SINGLETON = new RetryNotifier();
    private static final ExecutorService EXECUTOR = RetryNotifierThreadPoolProvider.SINGLETON.getExecutor();

    private RetryNotifier(){}

    private final CopyOnWriteArrayList<RetryNotificationInterested> interested = new CopyOnWriteArrayList<>();

    public RetryNotifier subscribe(RetryNotificationInterested retryNotificationInterested){
        this.interested.add(retryNotificationInterested);
        return this;
    }

    public void emitNewNotification(RetryNotification notification){
        this.interested.forEach(subscriber -> CompletableFuture.runAsync(() -> subscriber.getNotified(notification), EXECUTOR));
    }

}
