/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import net.minecraft.test.TestException;
import net.minecraft.text.Text;

public class TickLimitExceededException
extends TestException {
    protected final Text message;

    public TickLimitExceededException(Text message) {
        super(message.getString());
        this.message = message;
    }

    @Override
    public Text getText() {
        return this.message;
    }
}

