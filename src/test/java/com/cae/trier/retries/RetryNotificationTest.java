package com.cae.trier.retries;

import com.cae.mapped_exceptions.specifics.InternalMappedException;
import com.cae.trier.Trier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class RetryNotificationTest {

    @Test
    void shouldNotifyAllInterested() {
        var interestedA = new InterestedA();
        var interestedA2 = new InterestedA();
        var interestedB = new InterestedB();
        var interestedB2 = new InterestedB();
        List.of(interestedA, interestedA2, interestedB, interestedB2).forEach(interested -> Assertions.assertFalse(interested.called));
        RetryNotifier.SINGLETON
                .subscribe(interestedA)
                .subscribe(interestedA2)
                .subscribe(interestedB)
                .subscribe(interestedB2);
        try {
            Trier.of(this::someFailingAction)
                    .retryOn(RuntimeException.class, 5, 1)
                    .onExhaustion(failureStatus -> System.out.println("I knew it! " + failureStatus.getException()))
                    .setUnexpectedExceptionHandler(unexpectedException -> new InternalMappedException("opsie", "indeed"))
                    .execute();
        } catch (NoRetriesLeftException noRetriesLeftException){
            List.of(interestedA, interestedA2, interestedB, interestedB2).forEach(interested -> Assertions.assertTrue(interested.called));
        }
    }

    void someFailingAction(){
        throw new RuntimeException("opsie...");
    }

    public static class InterestedA extends ConcreteInterested{

        @Override
        public void getNotified(RetryNotification retryNotification) {
            this.called = true;
            System.out.println(retryNotification);
        }

    }

    public static class InterestedB extends ConcreteInterested{

        @Override
        public void getNotified(RetryNotification retryNotification) {
            this.called = true;
            System.out.println(retryNotification);
        }

    }

    public static abstract class ConcreteInterested implements RetryObserver {
        public Boolean called = false;
    }

}
