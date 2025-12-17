/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import net.minecraft.test.FailureLoggingTestCompletionListener;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestCompletionListener;

public class TestFailureLogger {
    private static TestCompletionListener completionListener = new FailureLoggingTestCompletionListener();

    public static void setCompletionListener(TestCompletionListener listener) {
        completionListener = listener;
    }

    public static void failTest(GameTestState test) {
        completionListener.onTestFailed(test);
    }

    public static void passTest(GameTestState test) {
        completionListener.onTestPassed(test);
    }

    public static void stop() {
        completionListener.onStopped();
    }
}

