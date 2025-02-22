package com.cae.trier.autoretry;

@FunctionalInterface
public interface OnExhaustion {

    void handle(FailureStatus failureStatus);

    class FailureStatus{

        public static FailureStatus of(Exception exception, Integer totalOfRetries){
            return new FailureStatus(exception, totalOfRetries);
        }

        private FailureStatus(Exception exception, Integer totalOfRetries) {
            this.exception = exception;
            this.totalOfRetries = totalOfRetries;
        }

        private final Exception exception;
        private final Integer totalOfRetries;

        public Exception getException() {
            return exception;
        }

        public Integer getTotalOfRetries() {
            return totalOfRetries;
        }
    }

}
