/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.minecraft.util.LenientJsonParser
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraft.util.LenientJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record RealmsNews(@Nullable String newsLink) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static RealmsNews parse(String json) {
        String newsLink = null;
        try {
            JsonObject object = LenientJsonParser.parse((String)json).getAsJsonObject();
            newsLink = JsonUtils.getStringOr("newsLink", object, null);
        }
        catch (Exception e) {
            LOGGER.error("Could not parse RealmsNews", (Throwable)e);
        }
        return new RealmsNews(newsLink);
    }
}

