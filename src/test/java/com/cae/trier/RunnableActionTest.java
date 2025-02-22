package com.cae.trier;

import com.cae.trier.autoretry.AutoretryPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class RunnableActionTest {

    @Test
    void shouldExecuteTheRunnableAction(){
        var retryBlueprint = new HashMap<Class<? extends Exception>, AutoretryPolicy>();
        var counter = new Counter();
        var runnableMother = new RunnableMethod(counter);
        var runnableAction = new RunnableAction(runnableMother::runStuff);
        var result = runnableAction.execute(null, retryBlueprint);
        Assertions.assertNull(result);
        Assertions.assertEquals(1, counter.subject);
    }

    public static class Counter{

        public Integer subject = 0;

    }

    public static class RunnableMethod{

        private final Counter counter;

        public RunnableMethod(Counter counter){
            this.counter = counter;
        }

        public void runStuff(){
            this.counter.subject += 1;
        }

    }

}
