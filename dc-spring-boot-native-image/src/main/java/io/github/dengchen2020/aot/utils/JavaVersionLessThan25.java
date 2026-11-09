package io.github.dengchen2020.aot.utils;

import java.util.function.BooleanSupplier;

public class JavaVersionLessThan25 implements BooleanSupplier {
    @Override
    public boolean getAsBoolean() {
        return Runtime.version().feature() < 25;
    }
}
