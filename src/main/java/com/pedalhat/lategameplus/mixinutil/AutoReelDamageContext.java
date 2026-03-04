package com.pedalhat.lategameplus.mixinutil;

public final class AutoReelDamageContext {
    private static final ThreadLocal<Integer> EXTRA_DAMAGE = ThreadLocal.withInitial(() -> 0);

    private AutoReelDamageContext() {
    }

    public static void setExtraDamage(int extraDamage) {
        EXTRA_DAMAGE.set(Math.max(0, extraDamage));
    }

    public static int consumeExtraDamage() {
        int value = EXTRA_DAMAGE.get();
        EXTRA_DAMAGE.set(0);
        return value;
    }

    public static void clear() {
        EXTRA_DAMAGE.set(0);
    }
}
