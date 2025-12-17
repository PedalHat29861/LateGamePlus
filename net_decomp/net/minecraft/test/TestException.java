/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import net.minecraft.text.Text;

public abstract class TestException
extends RuntimeException {
    public TestException(String message) {
        super(message);
    }

    public abstract Text getText();
}

