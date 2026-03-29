/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.exception.RealmsServiceException;

public class RetryCallException
extends RealmsServiceException {
    public static final int DEFAULT_DELAY = 5;
    public final int delaySeconds;

    public RetryCallException(int delaySeconds, int statusCode) {
        super(RealmsError.CustomError.retry(statusCode));
        this.delaySeconds = delaySeconds < 0 || delaySeconds > 120 ? 5 : delaySeconds;
    }
}

