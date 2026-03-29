/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.exception;

import org.slf4j.Logger;

public class RealmsDefaultUncaughtExceptionHandler
implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public RealmsDefaultUncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        this.logger.error("Caught previously unhandled exception", e);
    }
}

