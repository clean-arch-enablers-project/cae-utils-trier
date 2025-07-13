package com.cae.trier;

import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.trier.retry.RetryPolicy;
import com.cae.trier.retry.NoRetriesLeftException;

import java.util.Map;
import java.util.Optional;

/**
 * Interface that represents an action which has an input and an output
 * defined.
 * @param <I> the type of the input
 * @param <O> the type of the output
 */
public abstract class Action <I, O>{


    public O execute(
            I input,
            Map<Class<? extends Exception>, RetryPolicy> retryPolicies){
        try {
            return this.executeInternalAction(input);
        } catch (NoRetriesLeftException noRetriesLeftException){
          throw noRetriesLeftException;
        } catch (Exception problem) {
            var retryPolicyByException = this.getRetryPolicyBy(problem, retryPolicies);
            if (retryPolicyByException.isPresent()){
                var delayToWait = retryPolicyByException.get()
                        .getRetryTracker()
                        .trackNewAttemptOn(problem);
                return this.runRetryOn(input, retryPolicies, delayToWait, retryPolicyByException.get());
            }
            else
                throw problem;
        }
    }

    private O runRetryOn(
            I input,
            Map<Class<? extends Exception>, RetryPolicy> retryPolicies,
            Long delayToWait,
            RetryPolicy retryPolicy) {
        try{
            retryPolicy.getTimeUnit().sleep(delayToWait);
            return this.execute(input, retryPolicies);
        } catch (InterruptedException interruptedException){
            throw new InternalMappedException(
                    "Something went unexpectedly wrong while trying to perform retry at the Action level",
                    "More details: " + interruptedException.getCause()
            );
        }
    }

    private Optional<RetryPolicy> getRetryPolicyBy(
            Exception exception,
            Map<Class<? extends Exception>, RetryPolicy> retryPolicies){
        return Optional.ofNullable(retryPolicies.get(exception.getClass()));
    }

    protected abstract O executeInternalAction(I input);


}
