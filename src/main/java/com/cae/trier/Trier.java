package com.cae.trier;


import com.cae.mapped_exceptions.MappedException;
import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.trier.retry.RetryPolicy;
import com.cae.trier.retry.NoRetriesLeftException;
import com.cae.trier.retry.OnExhaustion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Trier is specialized in executing actions that might throw MappedException
 * instances and other kind of exceptions. When the exception caught is
 * a MappedException instance, the Trier lets it go, as it is considered as
 * part of the planned flow. When the exception caught is not a MappedException,
 * though, the Trier will intercept it and execute the UnexpectedExceptionHandler
 * parameterized at its instantiation, for handling exceptions that might happen
 * but weren't on radar.
 * @param <I> the input type of the action being executed by the Trier
 * @param <O> the output type of the action being executed by the Trier
 */
public class Trier<I, O> {

    /**
     * Action which will be executed by the Trier instance
     */
    private final Action<I, O> action;
    /**
     * The input of the action to be executed, only in case it is needed
     */
    private final I input;
    /**
     * The unexpected exception handler which will be executed in
     * case of any unmapped exception being caught during the
     * execution of the parameterized action
     */
    private final UnexpectedExceptionHandler unexpectedExceptionHandler;
    /**
     * Map for exceptions that must trigger retry attempts
     */
    private final Map<Class<? extends Exception>, RetryPolicy> retryPolicies;
    /**
     * Handler for failed executions
     */
    private final OnExhaustion onExhaustionHandler;

    private Trier(
            Action<I, O> action,
            I input,
            UnexpectedExceptionHandler unexpectedExceptionHandler,
            Map<Class<? extends Exception>, RetryPolicy> retryPolicies,
            OnExhaustion onExhaustionHandler) {
        this.action = action;
        this.input = input;
        this.unexpectedExceptionHandler = unexpectedExceptionHandler;
        this.retryPolicies = retryPolicies;
        this.onExhaustionHandler = onExhaustionHandler;
    }

    /**
     * The starter method for instantiating a Trier object, receiving a
     * Function method as its action to be executed.
     * @param function action with input and output to be executed
     * @param input the input to be fed to the action when it gets executed
     * @return the TrierBuilder instance to continue the instantiation process of the Trier
     * @param <I> the input type of the action
     * @param <O> the output type of the action
     */
    public static <I, O> TrierBuilder<I, O> of(Function<I, O> function, I input){
        return new TrierBuilder<>(ActionFactory.of(function), input);
    }

    /**
     * The starter method for instantiating a Trier object, receiving a
     * Consumer method as its action to be executed.
     * @param consumer action with only input to be executed
     * @param input the input to be fed to the action when it gets executed
     * @return the TrierBuilder instance to continue the instantiation process of the Trier
     * @param <I> the input type of the action
     */
    public static <I> TrierBuilder<I, Void> of(Consumer<I> consumer, I input){
        return new TrierBuilder<>(ActionFactory.of(consumer), input);
    }

    /**
     * The starter method for instantiating a Trier object, receiving a
     * Supplier method as its action to be executed.
     * @param supplier action with only output to be executed
     * @return the TrierBuilder instance to continue the instantiation process of the Trier
     * @param <O> the output type of the action
     */
    public static <O> TrierBuilder<Void, O> of(Supplier<O> supplier){
        return new TrierBuilder<>(ActionFactory.of(supplier), null);
    }

    /**
     * The starter method for instantiating a Trier object, receiving a
     * Runnable method as its action to be executed.
     * @param runnable action to be executed with no input nor output
     * @return the TrierBuilder instance to continue the instantiation process of the Trier
     */
    public static TrierBuilder<Void, Void> of(Runnable runnable){
        return new TrierBuilder<>(ActionFactory.of(runnable), null);
    }

    /**
     * Method which starts the execution of the action the Trier
     * encapsulates. If the action throws a MappedException, the Trier
     * won't intercept it, as it considers the exception to be part of the
     * planned flow. On the other hand, if the action being executed
     * throws an exception that does not extend the MappedException
     * type, it will intercept it and execute the unexpected exception handler
     * to the rescue, handling an exception that is considered not part
     * of the planned flow.
     * @return the output of the action. If the action is a consumer or a
     * runnable, its output will be null.
     */
    public O execute(){
        try {
            return this.action.execute(this.input, this.retryPolicies);
        } catch (NoRetriesLeftException noRetriesLeftException){
            Optional.ofNullable(this.onExhaustionHandler)
                    .orElseThrow(() -> new InternalMappedException(
                            "Exhausted all retries and no OnExhaustion handler was provided",
                            "When configuring your Trier with the autoretry feature, make sure to provide an OnExhaustion handler using the TrierBuilder.onExhaustion(OnExhaustion) API."
                    ))
                    .handle(noRetriesLeftException.getFailureStatus());
            throw noRetriesLeftException;
        } catch (MappedException mappedException){
            throw mappedException;
        } catch (Exception unexpectedException){
            throw this.unexpectedExceptionHandler.handle(unexpectedException);
        }
    }

    /**
     * Provides assistance for the instantiating process of the Trier
     * object, receiving the UnexpectedExceptionHandler.
     * @param <I> the input type of the action being encapsulated
     * @param <O> the output type of the action being encapsulated
     */
    public static class TrierBuilder<I, O>{

        private final Action<I, O> action;
        private final I input;
        private final Map<Class<? extends Exception>, RetryPolicy> retryPolicies = new HashMap<>();
        private OnExhaustion onExhaustion;

        private TrierBuilder(Action<I, O> action, I input) {
            this.action = action;
            this.input = input;
        }

        public <E extends Exception> TrierBuilder<I, O> retryOn(
                Class<E> exceptionClass,
                Integer maxRetries,
                long baseTime,
                TimeUnit timeUnit){
            if (exceptionClass != NoRetriesLeftException.class)
                this.retryPolicies.put(exceptionClass, RetryPolicy.of(maxRetries, baseTime, timeUnit));
            return this;
        }

        public TrierBuilder<I, O> onExhaustion(OnExhaustion onExhaustion){
            this.onExhaustion = onExhaustion;
            return this;
        }

        /**
         * Sets the unexpected exception handler which will be called in
         * case of the action being executed throwing an exception that
         * is unexpected, which means an exception that does not
         * extend the MappedException type.
         * @param handler any runnable that implements the contract of
         *                the UnexpectedExceptionHandler functional interface;
         *                that is anything that is able to receive an Exception
         *                object as parameter and return a MappedException as
         *                its output
         * @return the Trier instance with the action to be executed as well
         * as the input to be fed to the action if necessary, and the handler
         * for unexpected exceptions.
         */
        public Trier<I, O> onUnexpectedExceptions(UnexpectedExceptionHandler handler){
            return new Trier<>(
                    this.action,
                    this.input,
                    handler,
                    this.retryPolicies,
                    this.onExhaustion
            );
        }
    }

}
