/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.util.Set;

public record RealmsWorldResetDto(@SerializedName(value="seed") String seed, @SerializedName(value="worldTemplateId") long worldTemplateId, @SerializedName(value="levelType") int levelType, @SerializedName(value="generateStructures") boolean generateStructures, @SerializedName(value="experiments") Set<String> experiments) implements ReflectionBasedSerialization
{
}

