/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.Unit;
import com.mojang.realmsclient.client.worldupload.RealmsUploadException;
import net.minecraft.network.chat.Component;

public class RealmsUploadTooLargeException
extends RealmsUploadException {
    final long sizeLimit;

    public RealmsUploadTooLargeException(long sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    @Override
    public Component[] getErrorMessages() {
        return new Component[]{Component.translatable((String)"mco.upload.failed.too_big.title"), Component.translatable((String)"mco.upload.failed.too_big.description", (Object[])new Object[]{Unit.humanReadable(this.sizeLimit, Unit.getLargest(this.sizeLimit))})};
    }
}

