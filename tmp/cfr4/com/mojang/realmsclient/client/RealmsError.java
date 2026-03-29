/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.GsonHelper
 *  net.minecraft.util.LenientJsonParser
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsHttpException;
import java.util.Locale;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public interface RealmsError {
    public static final Component NO_MESSAGE = Component.translatable((String)"mco.errorMessage.noDetails");
    public static final Logger LOGGER = LogUtils.getLogger();

    public int errorCode();

    public Component errorMessage();

    public String logMessage();

    public static RealmsError parse(int httpCode, String payload) {
        if (httpCode == 429) {
            return CustomError.SERVICE_BUSY;
        }
        if (Strings.isNullOrEmpty((String)payload)) {
            return CustomError.noPayload(httpCode);
        }
        try {
            JsonObject object = LenientJsonParser.parse((String)payload).getAsJsonObject();
            String errorReason = GsonHelper.getAsString((JsonObject)object, (String)"reason", null);
            String errorMessage = GsonHelper.getAsString((JsonObject)object, (String)"errorMsg", null);
            int errorCode = GsonHelper.getAsInt((JsonObject)object, (String)"errorCode", (int)-1);
            if (errorMessage != null || errorReason != null || errorCode != -1) {
                return new ErrorWithJsonPayload(httpCode, errorCode != -1 ? errorCode : httpCode, errorReason, errorMessage);
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not parse RealmsError", (Throwable)e);
        }
        return new ErrorWithRawPayload(httpCode, payload);
    }

    public record CustomError(int httpCode, @Nullable Component payload) implements RealmsError
    {
        public static final CustomError SERVICE_BUSY = new CustomError(429, (Component)Component.translatable((String)"mco.errorMessage.serviceBusy"));
        public static final Component RETRY_MESSAGE = Component.translatable((String)"mco.errorMessage.retry");
        public static final String BODY_TAG = "<body>";
        public static final String CLOSING_BODY_TAG = "</body>";

        public static CustomError unknownCompatibilityResponse(String response) {
            return new CustomError(500, (Component)Component.translatable((String)"mco.errorMessage.realmsService.unknownCompatibility", (Object[])new Object[]{response}));
        }

        public static CustomError configurationError() {
            return new CustomError(500, (Component)Component.translatable((String)"mco.errorMessage.realmsService.configurationError"));
        }

        public static CustomError connectivityError(RealmsHttpException exception) {
            return new CustomError(500, (Component)Component.translatable((String)"mco.errorMessage.realmsService.connectivity", (Object[])new Object[]{exception.getMessage()}));
        }

        public static CustomError retry(int statusCode) {
            return new CustomError(statusCode, RETRY_MESSAGE);
        }

        public static CustomError noPayload(int statusCode) {
            return new CustomError(statusCode, null);
        }

        public static CustomError htmlPayload(int statusCode, String payload) {
            int bodyStart = payload.indexOf(BODY_TAG);
            int bodyEnd = payload.indexOf(CLOSING_BODY_TAG);
            if (bodyStart >= 0 && bodyEnd > bodyStart) {
                return new CustomError(statusCode, (Component)Component.literal((String)payload.substring(bodyStart + BODY_TAG.length(), bodyEnd).trim()));
            }
            LOGGER.error("Got an error with an unreadable html body {}", (Object)payload);
            return new CustomError(statusCode, null);
        }

        @Override
        public int errorCode() {
            return this.httpCode;
        }

        @Override
        public Component errorMessage() {
            return this.payload != null ? this.payload : NO_MESSAGE;
        }

        @Override
        public String logMessage() {
            if (this.payload != null) {
                return String.format(Locale.ROOT, "Realms service error (%d) with message '%s'", this.httpCode, this.payload.getString());
            }
            return String.format(Locale.ROOT, "Realms service error (%d) with no payload", this.httpCode);
        }
    }

    public record ErrorWithJsonPayload(int httpCode, int code, @Nullable String reason, @Nullable String message) implements RealmsError
    {
        @Override
        public int errorCode() {
            return this.code;
        }

        @Override
        public Component errorMessage() {
            String reasonTranslationKey;
            String codeTranslationKey = "mco.errorMessage." + this.code;
            if (I18n.exists(codeTranslationKey)) {
                return Component.translatable((String)codeTranslationKey);
            }
            if (this.reason != null && I18n.exists(reasonTranslationKey = "mco.errorReason." + this.reason)) {
                return Component.translatable((String)reasonTranslationKey);
            }
            return this.message != null ? Component.literal((String)this.message) : NO_MESSAGE;
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms service error (%d/%d/%s) with message '%s'", this.httpCode, this.code, this.reason, this.message);
        }
    }

    public record ErrorWithRawPayload(int httpCode, String payload) implements RealmsError
    {
        @Override
        public int errorCode() {
            return this.httpCode;
        }

        @Override
        public Component errorMessage() {
            return Component.literal((String)this.payload);
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms service error (%d) with raw payload '%s'", this.httpCode, this.payload);
        }
    }

    public record AuthenticationError(String message) implements RealmsError
    {
        public static final int ERROR_CODE = 401;

        @Override
        public int errorCode() {
            return 401;
        }

        @Override
        public Component errorMessage() {
            return Component.literal((String)this.message);
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms authentication error with message '%s'", this.message);
        }
    }
}

