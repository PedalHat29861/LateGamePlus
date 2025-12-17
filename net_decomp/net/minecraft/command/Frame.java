/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command;

import net.minecraft.command.ReturnValueConsumer;

public record Frame(int depth, ReturnValueConsumer returnValueConsumer, Control frameControl) {
    public void succeed(int returnValue) {
        this.returnValueConsumer.onSuccess(returnValue);
    }

    public void fail() {
        this.returnValueConsumer.onFailure();
    }

    public void doReturn() {
        this.frameControl.discard();
    }

    @FunctionalInterface
    public static interface Control {
        public void discard();
    }
}

