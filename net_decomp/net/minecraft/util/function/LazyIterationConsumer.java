/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.function;

import java.util.function.Consumer;

@FunctionalInterface
public interface LazyIterationConsumer<T> {
    public NextIteration accept(T var1);

    public static <T> LazyIterationConsumer<T> forConsumer(Consumer<T> consumer) {
        return value -> {
            consumer.accept(value);
            return NextIteration.CONTINUE;
        };
    }

    public static final class NextIteration
    extends Enum<NextIteration> {
        public static final /* enum */ NextIteration CONTINUE = new NextIteration();
        public static final /* enum */ NextIteration ABORT = new NextIteration();
        private static final /* synthetic */ NextIteration[] field_41285;

        public static NextIteration[] values() {
            return (NextIteration[])field_41285.clone();
        }

        public static NextIteration valueOf(String string) {
            return Enum.valueOf(NextIteration.class, string);
        }

        public boolean shouldAbort() {
            return this == ABORT;
        }

        private static /* synthetic */ NextIteration[] method_47544() {
            return new NextIteration[]{CONTINUE, ABORT};
        }

        static {
            field_41285 = NextIteration.method_47544();
        }
    }
}

