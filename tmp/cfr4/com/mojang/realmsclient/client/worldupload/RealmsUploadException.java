/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.client.worldupload;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public abstract class RealmsUploadException
extends RuntimeException {
    public @Nullable Component getStatusMessage() {
        return null;
    }

    public Component @Nullable [] getErrorMessages() {
        return null;
    }
}

