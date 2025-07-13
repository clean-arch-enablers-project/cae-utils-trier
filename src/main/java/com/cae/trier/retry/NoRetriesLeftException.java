package com.cae.trier.retry;

import com.cae.mapped_exceptions.specifics.InternalMappedException;

public class NoRetriesLeftException extends InternalMappedException {

    public NoRetriesLeftException(String briefPublicMessage, OnExhaustion.FailureStatus failureStatus) {
        super(
                briefPublicMessage,
                "All retries (" + failureStatus.getTotalOfRetries() + ") done for " + failureStatus.getException()
        );
        this.failureStatus = failureStatus;
    }

    private final OnExhaustion.FailureStatus failureStatus;

    public OnExhaustion.FailureStatus getFailureStatus() {
        return this.failureStatus;
    }
}
