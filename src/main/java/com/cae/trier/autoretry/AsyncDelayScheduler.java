package com.cae.trier.autoretry;

import com.cae.mapped_exceptions.specifics.InternalMappedException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AsyncDelayScheduler {

    public static final AsyncDelayScheduler SINGLETON = new AsyncDelayScheduler();

    private AsyncDelayScheduler() {}

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(
        Math.max(
            4,
            Runtime.getRuntime().availableProcessors() * 2
        )
    );

    public <O> CompletableFuture<O> scheduleWithDelay(Supplier<O> supplier, int totalDelayInSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(totalDelayInSeconds);
                return supplier.get();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new InternalMappedException(
                        "Thread interrupted during delay",
                        "More details: " + interruptedException
                );
            }
        }, SCHEDULER);
    }

}
