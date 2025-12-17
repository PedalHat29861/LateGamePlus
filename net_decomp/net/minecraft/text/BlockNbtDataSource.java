/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.NbtDataSource;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;

public record BlockNbtDataSource(String rawPos, @Nullable PosArgument pos) implements NbtDataSource
{
    public static final MapCodec<BlockNbtDataSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("block").forGetter(BlockNbtDataSource::rawPos)).apply((Applicative)instance, BlockNbtDataSource::new));

    public BlockNbtDataSource(String rawPath) {
        this(rawPath, BlockNbtDataSource.parsePos(rawPath));
    }

    private static @Nullable PosArgument parsePos(String string) {
        try {
            return BlockPosArgumentType.blockPos().parse(new StringReader(string));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public Stream<NbtCompound> get(ServerCommandSource source) {
        BlockEntity blockEntity;
        BlockPos blockPos;
        ServerWorld serverWorld;
        if (this.pos != null && (serverWorld = source.getWorld()).isPosLoaded(blockPos = this.pos.toAbsoluteBlockPos(source)) && (blockEntity = serverWorld.getBlockEntity(blockPos)) != null) {
            return Stream.of(blockEntity.createNbtWithIdentifyingData(source.getRegistryManager()));
        }
        return Stream.empty();
    }

    public MapCodec<BlockNbtDataSource> getCodec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "block=" + this.rawPos;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockNbtDataSource)) return false;
        BlockNbtDataSource blockNbtDataSource = (BlockNbtDataSource)o;
        if (!this.rawPos.equals(blockNbtDataSource.rawPos)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.rawPos.hashCode();
    }
}

