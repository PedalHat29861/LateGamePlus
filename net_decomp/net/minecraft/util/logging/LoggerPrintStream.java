/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.logging;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class LoggerPrintStream
extends PrintStream {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String name;

    public LoggerPrintStream(String name, OutputStream out) {
        super(out, false, StandardCharsets.UTF_8);
        this.name = name;
    }

    @Override
    public void println(@Nullable String message) {
        this.log(message);
    }

    @Override
    public void println(@Nullable Object object) {
        this.log(String.valueOf(object));
    }

    protected void log(@Nullable String message) {
        LOGGER.info("[{}]: {}", (Object)this.name, (Object)message);
    }
}

