/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.text;

import com.mojang.serialization.MapCodec;
import net.minecraft.text.BlockNbtDataSource;
import net.minecraft.text.EntityNbtDataSource;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.StorageNbtDataSource;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

public class NbtDataSourceTypes {
    private static final Codecs.IdMapper<String, MapCodec<? extends NbtDataSource>> ID_MAPPER = new Codecs.IdMapper();
    public static final MapCodec<NbtDataSource> CODEC = TextCodecs.dispatchingCodec(ID_MAPPER, NbtDataSource::getCodec, "source");

    static {
        ID_MAPPER.put("entity", EntityNbtDataSource.CODEC);
        ID_MAPPER.put("block", BlockNbtDataSource.CODEC);
        ID_MAPPER.put("storage", StorageNbtDataSource.CODEC);
    }
}

