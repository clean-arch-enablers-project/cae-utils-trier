package com.cae.trier;

import com.cae.mapped_exceptions.specifics.InputMappedException;
import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.mapped_exceptions.specifics.NotFoundMappedException;
import com.cae.trier.retrier.NoRetriesLeftException;
import com.cae.trier.retrier.OnExhaustion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

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
                .setUnexpectedExceptionHandler(unexpected -> new InputMappedException("iha!"));
        Assertions.assertNotNull(trier);
    }

    @Test
    void whenExecutingActionWhichThrowsRuntimeExceptionShouldMapItToNotFoundMappedException(){
        var trier = Trier.of(new Actions()::runtimeExceptionThrowingMethod)
                .setUnexpectedExceptionHandler(unexpectedException -> new NotFoundMappedException("Hmm..."));
        Assertions.assertThrows(NotFoundMappedException.class, trier::execute);
    }

    @Test
    void whenExecutingActionWhichThrowsMappedExceptionShouldLetItGo(){
        var trier = Trier.of(new Actions()::inputMappedExceptionThrowingMethod)
                .setUnexpectedExceptionHandler(unexpectedException -> new InternalMappedException("Hmm...", "ok...."));
        Assertions.assertThrows(InputMappedException.class, trier::execute);
    }

    @Test
    void shouldReturnWhatTheActualActionReturns(){
        var actualReturn = new Actions().someSupplierMethod();
        var returnFromTrier = Trier.of(() -> new Actions().someSupplierMethod())
                .setUnexpectedExceptionHandler(unexpectedException -> new InternalMappedException("Alright", "Sweet!"))
                .execute();
        Assertions.assertEquals(actualReturn, returnFromTrier);
    }

    @Test
    void shouldRetryOnRuntimeException(){
        var trier = Trier.of(() -> new Actions().runtimeExceptionThrowingMethod()) //the method is forced to throw RuntimeException
            .retryOn(RuntimeException.class, 3, 2)
            .onExhaustion(this::handleExhaustion)
            .setUnexpectedExceptionHandler(unexpectedException -> new InternalMappedException(
                "Something went wrong while trying to run the method",
                "More details on original problem: " + unexpectedException
            ));
        Assertions.assertThrows(NoRetriesLeftException.class, trier::execute);
    }

    @Test
    void shouldRetryOnInputMappedException(){
        var trier = Trier.of(() -> new Actions().inputMappedExceptionThrowingMethod()) //the method is forced to throw InputMappedException
                .retryOn(InputMappedException.class, 3, 2)
                .onExhaustion(this::handleExhaustion)
                .setUnexpectedExceptionHandler(unexpectedException -> new InternalMappedException(
                        "Something went wrong while trying to run the method",
                        "More details on original problem: " + unexpectedException
                ));
        Assertions.assertThrows(NoRetriesLeftException.class, trier::execute);
    }

    @Test
    void shouldBeAbleToFinishSuccessfullyAfterSecondRetry(){
        var action = new Actions();
        var trier = Trier.of(action::someIntermittentMethod) //the method is forced to throw RuntimeException
                .retryOn(InternalMappedException.class, 3, 2)
                .retryOn(RuntimeException.class, 3, 2)
                .retryOn(IOException.class, 5, 1)
                .onExhaustion(this::handleExhaustion)
                .setUnexpectedExceptionHandler(unexpectedException -> new InternalMappedException(
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
