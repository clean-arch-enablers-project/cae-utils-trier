package com.cae.trier;

import com.cae.mapped_exceptions.MappedException;
import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.trier.autoretry.AsyncDelayScheduler;
import com.cae.trier.autoretry.NoRetriesLeftException;
import com.cae.trier.autoretry.AutoretryPolicy;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;

/**
 * Interface that represents an action which has an input and an output
 * defined.
 * @param <I> the type of the input
 * @param <O> the type of the output
 */
public abstract class Action <I, O>{


    public O execute(
            I input,
            Map<Class<? extends Exception>, AutoretryPolicy> retryPolicies){
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
                return this.runRetryOn(input, retryPolicies, delayToWait);
            }
            else
                throw problem;
        }
    }

    private O runRetryOn(
            I input,
            Map<Class<? extends Exception>, AutoretryPolicy> retryPolicies,
            Integer delayToWait) {
        try{
            return AsyncDelayScheduler.SINGLETON.scheduleWithDelay(
                () -> this.execute(input, retryPolicies),
                delayToWait
            )
            .join();
        } catch (CompletionException completionException){
            if (completionException.getCause() instanceof MappedException)
                throw (MappedException) completionException.getCause();
            else
                throw new InternalMappedException(
                        "Something went unexpectedly wrong while trying to perform retry at the Action level",
                        "More details: " + completionException.getCause()
                );
        }
    }

    private Optional<AutoretryPolicy> getRetryPolicyBy(
            Exception exception,
            Map<Class<? extends Exception>, AutoretryPolicy> retryPolicies){
        return Optional.ofNullable(retryPolicies.get(exception.getClass()));
    }

    protected abstract O executeInternalAction(I input);


}
