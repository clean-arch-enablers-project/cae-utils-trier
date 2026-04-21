package com.cae.trier;

import com.cae.mapped_exceptions.specifics.InputMappedException;
import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.mapped_exceptions.specifics.NotFoundMappedException;
import com.cae.trier.retry.NoRetriesLeftException;
import com.cae.trier.retry.OnExhaustion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class TrierTest {

    @Test
    void shouldInstantiateTheBuilderBasedOnAFunctionAction(){
        Assertions.assertNotNull(Trier.of(new Actions()::someFunctionMethod, "uai"));
    }

    @Test
    void shouldInstantiateTheBuilderBasedOnAConsumerAction(){
        Assertions.assertNotNull(Trier.of(new Actions()::someConsumerMethod, "uai"));
    }

    @Test
    void shouldInstantiateTheBuilderBasedOnARunnableAction(){
        Assertions.assertNotNull(Trier.of(new Actions()::someRunnableMethod));
    }

    @Test
    void shouldInstantiateTheBuilderBasedOnASupplierAction(){
        Assertions.assertNotNull(Trier.of(new Actions()::someSupplierMethod));
    }

    @Test
    void shouldInstantiateTheTrierWhenHandlerForUnexpectedExceptionIsSet(){
        var trier = Trier.of(new Actions()::someRunnableMethod)
                .onUnexpectedExceptions(unexpected -> new InputMappedException("iha!"));
        Assertions.assertNotNull(trier);
    }

    @Test
    void whenExecutingActionWhichThrowsRuntimeExceptionShouldMapItToNotFoundMappedException(){
        var trier = Trier.of(new Actions()::runtimeExceptionThrowingMethod)
                .onUnexpectedExceptions(unexpectedException -> new NotFoundMappedException("Hmm..."));
        Assertions.assertThrows(NotFoundMappedException.class, trier::execute);
    }

    @Test
    void whenExecutingActionWhichThrowsMappedExceptionShouldLetItGo(){
        var trier = Trier.of(new Actions()::inputMappedExceptionThrowingMethod)
                .onUnexpectedExceptions(unexpectedException -> new InternalMappedException("Hmm...", "ok...."));
        Assertions.assertThrows(InputMappedException.class, trier::execute);
    }

    @Test
    void shouldReturnWhatTheActualActionReturns(){
        var actualReturn = new Actions().someSupplierMethod();
        var returnFromTrier = Trier.of(() -> new Actions().someSupplierMethod())
                .onUnexpectedExceptions(unexpectedException -> new InternalMappedException("Alright", "Sweet!"))
                .execute();
        Assertions.assertEquals(actualReturn, returnFromTrier);
    }

    @Test
    void shouldRetryOnRuntimeException(){
        var trier = Trier.of(() -> new Actions().runtimeExceptionThrowingMethod()) //the method is forced to throw RuntimeException
            .retryOn(RuntimeException.class, 3, 200, TimeUnit.MILLISECONDS)
            .onExhaustion(this::handleExhaustion)
            .onUnexpectedExceptions(unexpectedException -> new InternalMappedException(
                "Something went wrong while trying to run the method",
                "More details on original problem: " + unexpectedException
            ));
        Assertions.assertThrows(NoRetriesLeftException.class, trier::execute);
    }

    @Test
    void shouldRetryOnInputMappedException(){
        var trier = Trier.of(() -> new Actions().inputMappedExceptionThrowingMethod()) //the method is forced to throw InputMappedException
                .retryOn(InputMappedException.class, 3, 200, TimeUnit.MILLISECONDS)
                .onExhaustion(this::handleExhaustion)
                .onUnexpectedExceptions(unexpectedException -> new InternalMappedException(
                        "Something went wrong while trying to run the method",
                        "More details on original problem: " + unexpectedException
                ));
        Assertions.assertThrows(NoRetriesLeftException.class, trier::execute);
    }

    @Test
    void shouldBeAbleToFinishSuccessfullyAfterSecondRetry(){
        var action = new Actions();
        var trier = Trier.of(action::someIntermittentMethod) //the method is forced to throw RuntimeException
                .retryOn(InternalMappedException.class, 3, 200, TimeUnit.MILLISECONDS)
                .retryOn(RuntimeException.class, 3, 200, TimeUnit.MILLISECONDS)
                .retryOn(IOException.class, 5, 100, TimeUnit.MILLISECONDS)
                .onExhaustion(this::handleExhaustion)
                .onUnexpectedExceptions(unexpectedException -> new InternalMappedException(
                        "Something went wrong while trying to run the method",
                        "More details on original problem: " + unexpectedException
                ));
        Assertions.assertDoesNotThrow(trier::execute);
    }

    private void handleExhaustion(OnExhaustion.FailureStatus failureStatus) {
        System.out.println("Exhausted: " + failureStatus.getTotalOfRetries() + " retries | " + failureStatus.getException());
    }

    public static class Actions{

        private Integer retries = 0;

        public String someFunctionMethod(String parameter){
            return "result";
        }

        void someConsumerMethod(String parameter){
            System.out.println("yes: " + parameter);
        }

        String someSupplierMethod(){
            return "result";
        }

        void someRunnableMethod(){
            System.out.println("Nothing really...");
        }

        void runtimeExceptionThrowingMethod(){
            throw new RuntimeException("ouch!");
        }

        void inputMappedExceptionThrowingMethod(){
            throw new InputMappedException("yeah");
        }

        void someIntermittentMethod(){
            if (this.retries < 2){
                this.retries += 1;
                throw new RuntimeException("nope...");
            }
        }

    }

}
