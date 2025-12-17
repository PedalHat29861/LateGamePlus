/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 */
package net.minecraft.util.logging;

import org.slf4j.Logger;

public class UncaughtExceptionLogger
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public UncaughtExceptionLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        this.logger.error("Caught previously unhandled exception :", throwable);
    }
}

