package io.github.dengchen2020.aot.utils;

import java.util.function.BooleanSupplier;

public class IsNotLibericaNIK implements BooleanSupplier {
    @Override
    public boolean getAsBoolean() {
        return OS.isNotLibericaNIK();
    }
}
