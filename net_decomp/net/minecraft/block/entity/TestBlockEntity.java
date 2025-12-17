/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.TestBlockMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TestBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_MESSAGE = "";
    private static final boolean DEFAULT_POWERED = false;
    private TestBlockMode mode;
    private String message = "";
    private boolean powered = false;
    private boolean triggered;

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.TEST_BLOCK, pos, state);
        this.mode = state.get(TestBlock.MODE);
    }

    @Override
    protected void writeData(WriteView view) {
        view.put("mode", TestBlockMode.CODEC, this.mode);
        view.putString("message", this.message);
        view.putBoolean("powered", this.powered);
    }

    @Override
    protected void readData(ReadView view) {
        this.mode = view.read("mode", TestBlockMode.CODEC).orElse(TestBlockMode.FAIL);
        this.message = view.getString("message", DEFAULT_MESSAGE);
        this.powered = view.getBoolean("powered", false);
    }

    private void update() {
        if (this.world == null) {
            return;
        }
        BlockPos blockPos = this.getPos();
        BlockState blockState = this.world.getBlockState(blockPos);
        if (blockState.isOf(Blocks.TEST_BLOCK)) {
            this.world.setBlockState(blockPos, (BlockState)blockState.with(TestBlock.MODE, this.mode), 2);
        }
    }

    public @Nullable BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public TestBlockMode getMode() {
        return this.mode;
    }

    public void setMode(TestBlockMode mode) {
        this.mode = mode;
        this.update();
    }

    private Block getBlock() {
        return this.getCachedState().getBlock();
    }

    public void reset() {
        this.triggered = false;
        if (this.mode == TestBlockMode.START && this.world != null) {
            this.setPowered(false);
            this.world.updateNeighbors(this.getPos(), this.getBlock());
        }
    }

    public void trigger() {
        if (this.mode == TestBlockMode.START && this.world != null) {
            this.setPowered(true);
            BlockPos blockPos = this.getPos();
            this.world.updateNeighbors(blockPos, this.getBlock());
            this.world.getBlockTickScheduler().isTicking(blockPos, this.getBlock());
            this.logMessage();
            return;
        }
        if (this.mode == TestBlockMode.LOG) {
            this.logMessage();
        }
        this.triggered = true;
    }

    public void logMessage() {
        if (!this.message.isBlank()) {
            LOGGER.info("Test {} (at {}): {}", new Object[]{this.mode.asString(), this.getPos(), this.message});
        }
    }

    public boolean hasTriggered() {
        return this.triggered;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public /* synthetic */ @Nullable Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

