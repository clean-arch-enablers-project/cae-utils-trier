package com.cae.trier;

import com.cae.trier.retries.RetryPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class SupplierActionTest {

    @Test
    void shouldExecuteTheSupplierAction(){
        var retryBlueprint = new HashMap<Class<? extends Exception>, RetryPolicy>();
        var supplierMother = new SupplierMother();
        var supplierAction = new SupplierAction<>(supplierMother::supplyStuff);
        var result = supplierAction.execute(null, retryBlueprint);
        Assertions.assertEquals("ran", result);
    }


    public static class SupplierMother{

        public String supplyStuff(){
            return "ran";
        }

    }

}
