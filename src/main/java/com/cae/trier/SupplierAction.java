package com.cae.trier;

import java.util.function.Supplier;

/**
 * Actions that only have output, but no input
 * @param <O> the output type
 */
public class SupplierAction<O> extends Action<Void, O>{

    /**
     * The action itself
      */
    private final Supplier<O> supplier;

    SupplierAction(Supplier<O> supplier) {
        this.supplier = supplier;
    }

    /**
     * Where the action gets executed
     * @param input null as suppliers don't use any input for their
     *              execution
     * @return the output provided by the action itself
     */
    @Override
    public O executeInternalAction(Void input) {
        return this.supplier.get();
    }
}
