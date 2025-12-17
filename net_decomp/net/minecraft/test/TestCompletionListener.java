/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;

public interface TestCompletionListener {
    public void onTestFailed(GameTestState var1);

    public void onTestPassed(GameTestState var1);

    default public void onStopped() {
    }
}

