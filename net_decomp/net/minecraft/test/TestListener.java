/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;
import net.minecraft.test.TestRunContext;

public interface TestListener {
    public void onStarted(GameTestState var1);

    public void onPassed(GameTestState var1, TestRunContext var2);

    public void onFailed(GameTestState var1, TestRunContext var2);

    public void onRetry(GameTestState var1, GameTestState var2, TestRunContext var3);
}

