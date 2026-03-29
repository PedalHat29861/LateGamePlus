/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.util.List;

public record RealmsSetting(@SerializedName(value="name") String name, @SerializedName(value="value") String value) implements ReflectionBasedSerialization
{
    public static RealmsSetting hardcoreSetting(boolean hardcore) {
        return new RealmsSetting("hardcore", Boolean.toString(hardcore));
    }

    public static boolean isHardcore(List<RealmsSetting> settings) {
        for (RealmsSetting setting : settings) {
            if (!setting.name().equals("hardcore")) continue;
            return Boolean.parseBoolean(setting.value());
        }
        return false;
    }
}

