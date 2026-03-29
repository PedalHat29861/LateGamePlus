/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.client.UploadStatus;

public interface RealmsWorldUploadStatusTracker {
    public UploadStatus getUploadStatus();

    public void setUploading();

    public static RealmsWorldUploadStatusTracker noOp() {
        return new RealmsWorldUploadStatusTracker(){
            private final UploadStatus uploadStatus = new UploadStatus();

            @Override
            public UploadStatus getUploadStatus() {
                return this.uploadStatus;
            }

            @Override
            public void setUploading() {
            }
        };
    }
}

