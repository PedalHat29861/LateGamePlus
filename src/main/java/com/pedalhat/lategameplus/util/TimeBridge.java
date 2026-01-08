package com.pedalhat.lategameplus.util;

import java.util.function.LongSupplier;

public final class TimeBridge {
    private static volatile LongSupplier NOW = () -> System.currentTimeMillis() / 1000L;

    private TimeBridge() {}

    public static long nowSeconds() {
        return NOW.getAsLong();
    }

    public static void setNowSupplier(LongSupplier supplier) {
        if (supplier != null) NOW = supplier;
    }
}
