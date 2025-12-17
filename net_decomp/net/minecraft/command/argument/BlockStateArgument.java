/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.command.argument;

import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class BlockStateArgument
implements Predicate<CachedBlockPosition> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final BlockState state;
    private final Set<Property<?>> properties;
    private final @Nullable NbtCompound data;

    public BlockStateArgument(BlockState state, Set<Property<?>> properties, @Nullable NbtCompound data) {
        this.state = state;
        this.properties = properties;
        this.data = data;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public Set<Property<?>> getProperties() {
        return this.properties;
    }

    @Override
    public boolean test(CachedBlockPosition cachedBlockPosition) {
        BlockState blockState = cachedBlockPosition.getBlockState();
        if (!blockState.isOf(this.state.getBlock())) {
            return false;
        }
        for (Property<?> property : this.properties) {
            if (blockState.get(property) == this.state.get(property)) continue;
            return false;
        }
        if (this.data != null) {
            BlockEntity blockEntity = cachedBlockPosition.getBlockEntity();
            return blockEntity != null && NbtHelper.matches(this.data, blockEntity.createNbtWithIdentifyingData(cachedBlockPosition.getWorld().getRegistryManager()), true);
        }
        return true;
    }

    public boolean test(ServerWorld world, BlockPos pos) {
        return this.test(new CachedBlockPosition(world, pos, false));
    }

    public boolean setBlockState(ServerWorld world, BlockPos pos, @Block.SetBlockStateFlag int flags) {
        BlockEntity blockEntity;
        BlockState blockState;
        BlockState blockState2 = blockState = (flags & 0x10) != 0 ? this.state : Block.postProcessState(this.state, world, pos);
        if (blockState.isAir()) {
            blockState = this.state;
        }
        blockState = this.copyPropertiesTo(blockState);
        boolean bl = false;
        if (world.setBlockState(pos, blockState, flags)) {
            bl = true;
        }
        if (this.data != null && (blockEntity = world.getBlockEntity(pos)) != null) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER);){
                DynamicRegistryManager wrapperLookup = world.getRegistryManager();
                ErrorReporter errorReporter = logging.makeChild(blockEntity.getReporterContext());
                NbtWriteView nbtWriteView = NbtWriteView.create(errorReporter.makeChild(() -> "(before)"), wrapperLookup);
                blockEntity.writeDataWithoutId(nbtWriteView);
                NbtCompound nbtCompound = nbtWriteView.getNbt();
                blockEntity.read(NbtReadView.create(logging, wrapperLookup, this.data));
                NbtWriteView nbtWriteView2 = NbtWriteView.create(errorReporter.makeChild(() -> "(after)"), wrapperLookup);
                blockEntity.writeDataWithoutId(nbtWriteView2);
                NbtCompound nbtCompound2 = nbtWriteView2.getNbt();
                if (!nbtCompound2.equals(nbtCompound)) {
                    bl = true;
                    blockEntity.markDirty();
                    world.getChunkManager().markForUpdate(pos);
                }
            }
        }
        return bl;
    }

    private BlockState copyPropertiesTo(BlockState state) {
        if (state == this.state) {
            return state;
        }
        for (Property<?> property : this.properties) {
            state = BlockStateArgument.copyProperty(state, this.state, property);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState to, BlockState from, Property<T> property) {
        return (BlockState)to.withIfExists(property, from.get(property));
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((CachedBlockPosition)context);
    }
}

