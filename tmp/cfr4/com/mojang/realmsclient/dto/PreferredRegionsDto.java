/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.RegionDataDto;
import java.util.List;

public record PreferredRegionsDto(@SerializedName(value="regionDataList") List<RegionDataDto> regionData) implements ReflectionBasedSerialization
{
    public static PreferredRegionsDto empty() {
        return new PreferredRegionsDto(List.of());
    }
}

