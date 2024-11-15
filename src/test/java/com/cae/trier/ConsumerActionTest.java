package com.cae.trier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsumerActionTest {

    @Test
    void shouldExecuteTheRunnableAction(){
        var counter = new Counter();
        var consumerMethod = new ConsumerMethod();
        var consumerAction = new ConsumerAction<>(consumerMethod::consumeStuff);
        var result = consumerAction.execute(counter);
        Assertions.assertNull(result);
        Assertions.assertEquals(1, counter.subject);
    }

    public static class Counter{

        public Integer subject = 0;

    }

    public static class ConsumerMethod{

        public void consumeStuff(Counter counter){
            counter.subject += 1;
        }

    }

}