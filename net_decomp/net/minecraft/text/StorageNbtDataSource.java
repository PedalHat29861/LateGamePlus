/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.NbtDataSource;
import net.minecraft.util.Identifier;

public record StorageNbtDataSource(Identifier id) implements NbtDataSource
{
    public static final MapCodec<StorageNbtDataSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("storage").forGetter(StorageNbtDataSource::id)).apply((Applicative)instance, StorageNbtDataSource::new));

    @Override
    public Stream<NbtCompound> get(ServerCommandSource source) {
        NbtCompound nbtCompound = source.getServer().getDataCommandStorage().get(this.id);
        return Stream.of(nbtCompound);
    }

    public MapCodec<StorageNbtDataSource> getCodec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "storage=" + String.valueOf(this.id);
    }
}

