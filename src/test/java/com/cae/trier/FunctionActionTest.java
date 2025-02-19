package com.cae.trier;

import com.cae.trier.retries.RetryPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class FunctionActionTest {

    @Test
    void shouldExecuteTheSupplierAction(){
        var retryBlueprint = new HashMap<Class<? extends Exception>, RetryPolicy>();
        var functionMother = new FunctionMother();
        var functionAction = new FunctionAction<>(functionMother::runStuff);
        var result = functionAction.execute("iha!!", retryBlueprint);
        Assertions.assertEquals("iha!! I ran I swear", result);
    }


    public static class FunctionMother{

        public String runStuff(String parameter){
            return parameter + " I ran I swear";
        }

    }

}
