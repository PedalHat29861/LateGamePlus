/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadCanceledException
extends RealmsUploadException {
    private static final Component UPLOAD_CANCELED = Component.translatable((String)"mco.upload.cancelled");

    @Override
    public Component getStatusMessage() {
        return UPLOAD_CANCELED;
    }
}

