package com.pedalhat.lategameplus.util;

import java.util.function.LongSupplier;

/** Puente de tiempo: por defecto wall-clock; el cliente lo sobreescribe con game time. */
public final class TimeBridge {
    private static volatile LongSupplier NOW = () -> System.currentTimeMillis() / 1000L;

    private TimeBridge() {}

    /** Segundos “ahora” (serán del mundo en cliente; wall-clock en server/menús). */
    public static long nowSeconds() {
        return NOW.getAsLong();
    }

    /** Lo llama el entrypoint cliente para reemplazar el proveedor de tiempo. */
    public static void setNowSupplier(LongSupplier supplier) {
        if (supplier != null) NOW = supplier;
    }
}
