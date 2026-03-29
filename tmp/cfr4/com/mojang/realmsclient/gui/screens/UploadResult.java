/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.gui.screens;

import org.jspecify.annotations.Nullable;

public record UploadResult(int statusCode, @Nullable String errorMessage) {
    public @Nullable String getSimplifiedErrorMessage() {
        if (this.statusCode < 200 || this.statusCode >= 300) {
            if (this.statusCode == 400 && this.errorMessage != null) {
                return this.errorMessage;
            }
            return String.valueOf(this.statusCode);
        }
        return null;
    }

    public static class Builder {
        private int statusCode = -1;
        private @Nullable String errorMessage;

        public Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public UploadResult build() {
            return new UploadResult(this.statusCode, this.errorMessage);
        }
    }
}

