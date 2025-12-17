/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.pattern;

import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jspecify.annotations.Nullable;

public class CachedBlockPosition {
    private final WorldView world;
    private final BlockPos pos;
    private final boolean forceLoad;
    private @Nullable BlockState state;
    private @Nullable BlockEntity blockEntity;
    private boolean cachedEntity;

    public CachedBlockPosition(WorldView world, BlockPos pos, boolean forceLoad) {
        this.world = world;
        this.pos = pos.toImmutable();
        this.forceLoad = forceLoad;
    }

    public BlockState getBlockState() {
        if (this.state == null && (this.forceLoad || this.world.isChunkLoaded(this.pos))) {
            this.state = this.world.getBlockState(this.pos);
        }
        return this.state;
    }

    public @Nullable BlockEntity getBlockEntity() {
        if (this.blockEntity == null && !this.cachedEntity) {
            this.blockEntity = this.world.getBlockEntity(this.pos);
            this.cachedEntity = true;
        }
        return this.blockEntity;
    }

    public WorldView getWorld() {
        return this.world;
    }

    public BlockPos getBlockPos() {
        return this.pos;
    }

    public static Predicate<@Nullable CachedBlockPosition> matchesBlockState(Predicate<BlockState> state) {
        return pos -> pos != null && state.test(pos.getBlockState());
    }
}

