/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadWorldNotClosedException
extends RealmsUploadException {
    @Override
    public Component getStatusMessage() {
        return Component.translatable((String)"mco.upload.close.failure");
    }
}

