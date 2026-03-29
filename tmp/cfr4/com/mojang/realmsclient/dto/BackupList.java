/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  net.minecraft.util.LenientJsonParser
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.Backup;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.LenientJsonParser;
import org.slf4j.Logger;

public record BackupList(List<Backup> backups) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static BackupList parse(String json) {
        ArrayList<Backup> backups = new ArrayList<Backup>();
        try {
            JsonElement node = LenientJsonParser.parse((String)json).getAsJsonObject().get("backups");
            if (node.isJsonArray()) {
                for (JsonElement element : node.getAsJsonArray()) {
                    Backup entry = Backup.parse(element);
                    if (entry == null) continue;
                    backups.add(entry);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Could not parse BackupList", (Throwable)e);
        }
        return new BackupList(backups);
    }
}

