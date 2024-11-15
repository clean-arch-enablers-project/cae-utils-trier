package com.cae.trier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FunctionActionTest {

    @Test
    void shouldExecuteTheSupplierAction(){
        var functionMother = new FunctionMother();
        var functionAction = new FunctionAction<>(functionMother::runStuff);
        var result = functionAction.execute("iha!!");
        Assertions.assertEquals("iha!! I ran I swear", result);
    }


    public static class FunctionMother{

        public String runStuff(String parameter){
            return parameter + " I ran I swear";
        }

    }

}
