/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringEscapeUtils
 */
package net.minecraft.util;

import org.apache.commons.lang3.StringEscapeUtils;

public class InvalidIdentifierException
extends RuntimeException {
    public InvalidIdentifierException(String message) {
        super(StringEscapeUtils.escapeJava((String)message));
    }

    public InvalidIdentifierException(String message, Throwable throwable) {
        super(StringEscapeUtils.escapeJava((String)message), throwable);
    }
}

