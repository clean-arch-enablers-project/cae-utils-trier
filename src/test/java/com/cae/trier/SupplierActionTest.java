package com.cae.trier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierActionTest {

    @Test
    void shouldExecuteTheSupplierAction(){
        var supplierMother = new SupplierMother();
        var supplierAction = new SupplierAction<>(supplierMother::supplyStuff);
        var result = supplierAction.execute(null);
        Assertions.assertEquals("ran", result);
    }


    public static class SupplierMother{

        public String supplyStuff(){
            return "ran";
        }

    }

}
