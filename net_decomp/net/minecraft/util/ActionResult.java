/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.Nullable;

public sealed interface ActionResult {
    public static final Success SUCCESS = new Success(SwingSource.CLIENT, ItemContext.KEEP_HAND_STACK);
    public static final Success SUCCESS_SERVER = new Success(SwingSource.SERVER, ItemContext.KEEP_HAND_STACK);
    public static final Success CONSUME = new Success(SwingSource.NONE, ItemContext.KEEP_HAND_STACK);
    public static final Fail FAIL = new Fail();
    public static final Pass PASS = new Pass();
    public static final PassToDefaultBlockAction PASS_TO_DEFAULT_BLOCK_ACTION = new PassToDefaultBlockAction();

    default public boolean isAccepted() {
        return false;
    }

    public record Success(SwingSource swingSource, ItemContext itemContext) implements ActionResult
    {
        @Override
        public boolean isAccepted() {
            return true;
        }

        public Success withNewHandStack(ItemStack newHandStack) {
            return new Success(this.swingSource, new ItemContext(true, newHandStack));
        }

        public Success noIncrementStat() {
            return new Success(this.swingSource, ItemContext.KEEP_HAND_STACK_NO_INCREMENT_STAT);
        }

        public boolean shouldIncrementStat() {
            return this.itemContext.incrementStat;
        }

        public @Nullable ItemStack getNewHandStack() {
            return this.itemContext.newHandStack;
        }
    }

    public static final class SwingSource
    extends Enum<SwingSource> {
        public static final /* enum */ SwingSource NONE = new SwingSource();
        public static final /* enum */ SwingSource CLIENT = new SwingSource();
        public static final /* enum */ SwingSource SERVER = new SwingSource();
        private static final /* synthetic */ SwingSource[] field_52429;

        public static SwingSource[] values() {
            return (SwingSource[])field_52429.clone();
        }

        public static SwingSource valueOf(String string) {
            return Enum.valueOf(SwingSource.class, string);
        }

        private static /* synthetic */ SwingSource[] method_61397() {
            return new SwingSource[]{NONE, CLIENT, SERVER};
        }

        static {
            field_52429 = SwingSource.method_61397();
        }
    }

    public static final class ItemContext
    extends Record {
        final boolean incrementStat;
        final @Nullable ItemStack newHandStack;
        static ItemContext KEEP_HAND_STACK_NO_INCREMENT_STAT = new ItemContext(false, null);
        static ItemContext KEEP_HAND_STACK = new ItemContext(true, null);

        public ItemContext(boolean incrementStat, @Nullable ItemStack newHandStack) {
            this.incrementStat = incrementStat;
            this.newHandStack = newHandStack;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ItemContext.class, "wasItemInteraction;heldItemTransformedTo", "incrementStat", "newHandStack"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ItemContext.class, "wasItemInteraction;heldItemTransformedTo", "incrementStat", "newHandStack"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ItemContext.class, "wasItemInteraction;heldItemTransformedTo", "incrementStat", "newHandStack"}, this, object);
        }

        public boolean incrementStat() {
            return this.incrementStat;
        }

        public @Nullable ItemStack newHandStack() {
            return this.newHandStack;
        }
    }

    public record Fail() implements ActionResult
    {
    }

    public record Pass() implements ActionResult
    {
    }

    public record PassToDefaultBlockAction() implements ActionResult
    {
    }
}

