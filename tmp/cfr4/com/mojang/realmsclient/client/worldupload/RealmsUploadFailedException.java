/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadFailedException
extends RealmsUploadException {
    private final Component errorMessage;

    public RealmsUploadFailedException(Component errorMessage) {
        this.errorMessage = errorMessage;
    }

    public RealmsUploadFailedException(String errorMessage) {
        this((Component)Component.literal((String)errorMessage));
    }

    @Override
    public Component getStatusMessage() {
        return Component.translatable((String)"mco.upload.failed", (Object[])new Object[]{this.errorMessage});
    }
}

