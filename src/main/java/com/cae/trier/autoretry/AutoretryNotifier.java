package com.cae.trier.autoretry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class AutoretryNotifier {

    public static final AutoretryNotifier SINGLETON = new AutoretryNotifier();
    private static final ExecutorService EXECUTOR = AutoretryNotifierThreadPoolProvider.SINGLETON.getExecutor();

    private AutoretryNotifier(){}

    private final CopyOnWriteArrayList<AutoretryObserver> interested = new CopyOnWriteArrayList<>();

    public AutoretryNotifier subscribe(AutoretryObserver autoretryObserver){
        this.interested.add(autoretryObserver);
        return this;
    }

    public void emitNewNotification(AutoretryNotification notification){
        this.interested.forEach(subscriber -> CompletableFuture.runAsync(() -> subscriber.getNotified(notification), EXECUTOR));
    }

}
