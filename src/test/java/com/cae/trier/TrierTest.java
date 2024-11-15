package com.cae.trier;

import com.cae.mapped_exceptions.specifics.InputMappedException;
import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.mapped_exceptions.specifics.NotFoundMappedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .setHandlerForUnexpectedException(unexpected -> new InputMappedException("iha!"));
        Assertions.assertNotNull(trier);
    }

    @Test
    void whenExecutingActionWhichThrowsRuntimeExceptionShouldMapItToNotFoundMappedException(){
        var trier = Trier.of(new Actions()::runtimeExceptionThrowingMethod)
                .setHandlerForUnexpectedException(unexpectedException -> new NotFoundMappedException("Hmm..."));
        Assertions.assertThrows(NotFoundMappedException.class, trier::finishAndExecuteAction);
    }

    @Test
    void whenExecutingActionWhichThrowsMappedExceptionShouldLetItGo(){
        var trier = Trier.of(new Actions()::inputMappedExceptionThrowingMethod)
                .setHandlerForUnexpectedException(unexpectedException -> new InternalMappedException("Hmm...", "ok...."));
        Assertions.assertThrows(InputMappedException.class, trier::finishAndExecuteAction);
    }

    public static class Actions{
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

    }

}
