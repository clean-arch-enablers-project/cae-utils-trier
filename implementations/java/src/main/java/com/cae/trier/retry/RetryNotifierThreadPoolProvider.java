package com.cae.trier.retry;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryNotifierThreadPoolProvider {

    public static final RetryNotifierThreadPoolProvider SINGLETON = new RetryNotifierThreadPoolProvider();

    private RetryNotifierThreadPoolProvider(){
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::shutdown, "RetryNotifier-ShutdownHook"));
    }

    private void shutdown() {
        if (this.executor != null) {
            this.executor.shutdown();
            try {
                if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    this.executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    protected ExecutorService executor;
    protected Integer minSize;
    protected Integer maxSize;
    protected Long keepAliveTimeForIdleThreadsInSeconds;
    protected Integer queueCapacity;
    protected String poolName;


    public RetryNotifierThreadPoolProvider setExecutor(ExecutorService executor){
        this.executor = executor;
        return this;
    }

    public RetryNotifierThreadPoolProvider setMinSize(Integer minSize){
        this.minSize = minSize;
        return this;
    }

    public RetryNotifierThreadPoolProvider setMaxSize(Integer maxSize){
        this.maxSize = maxSize;
        return this;
    }

    public RetryNotifierThreadPoolProvider setKeepAliveTimeForIdleThreadsInSeconds(Long keepAliveTimeForIdleThreadsInSeconds){
        this.keepAliveTimeForIdleThreadsInSeconds = keepAliveTimeForIdleThreadsInSeconds;
        return this;
    }

    public RetryNotifierThreadPoolProvider setQueueCapacity(Integer queueCapacity){
        this.queueCapacity = queueCapacity;
        return this;
    }

    public RetryNotifierThreadPoolProvider setPoolName(String poolName){
        this.poolName = poolName;
        return this;
    }

    public ExecutorService getExecutor(){
        if (this.executor == null){
            this.executor = this.autoProvideExecutor();
        }
        return this.executor;
    }

    private ExecutorService autoProvideExecutor() {
        return new ThreadPoolExecutor(
                Optional.ofNullable(this.minSize).orElse(5),
                Optional.ofNullable(this.maxSize).orElse(30),
                Optional.ofNullable(this.keepAliveTimeForIdleThreadsInSeconds).orElse(60L),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Optional.ofNullable(this.queueCapacity).orElse(100)),
                new CaeCustomThreadFactory(Optional.ofNullable(this.poolName).orElse("CaeRetryNotifierThreadPool")),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    protected static class CaeCustomThreadFactory implements ThreadFactory {

        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        protected CaeCustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var newThread = new Thread(runnable, this.namePrefix + " - " + this.threadNumber.getAndIncrement());
            newThread.setDaemon(false);
            return newThread;
        }
    }

}