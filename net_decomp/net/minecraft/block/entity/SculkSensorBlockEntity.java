/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.GameEventListener;
import org.jspecify.annotations.Nullable;

public class SculkSensorBlockEntity
extends BlockEntity
implements GameEventListener.Holder<Vibrations.VibrationListener>,
Vibrations {
    private static final int DEFAULT_LAST_VIBRATION_FREQUENCY = 0;
    private Vibrations.ListenerData listenerData;
    private final Vibrations.VibrationListener listener;
    private final Vibrations.Callback callback = this.createCallback();
    private int lastVibrationFrequency = 0;

    protected SculkSensorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.listenerData = new Vibrations.ListenerData();
        this.listener = new Vibrations.VibrationListener(this);
    }

    public SculkSensorBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityType.SCULK_SENSOR, pos, state);
    }

    public Vibrations.Callback createCallback() {
        return new VibrationCallback(this.getPos());
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.lastVibrationFrequency = view.getInt("last_vibration_frequency", 0);
        this.listenerData = view.read("listener", Vibrations.ListenerData.CODEC).orElseGet(Vibrations.ListenerData::new);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        view.put("listener", Vibrations.ListenerData.CODEC, this.listenerData);
    }

    @Override
    public Vibrations.ListenerData getVibrationListenerData() {
        return this.listenerData;
    }

    @Override
    public Vibrations.Callback getVibrationCallback() {
        return this.callback;
    }

    public int getLastVibrationFrequency() {
        return this.lastVibrationFrequency;
    }

    public void setLastVibrationFrequency(int lastVibrationFrequency) {
        this.lastVibrationFrequency = lastVibrationFrequency;
    }

    @Override
    public Vibrations.VibrationListener getEventListener() {
        return this.listener;
    }

    @Override
    public /* synthetic */ GameEventListener getEventListener() {
        return this.getEventListener();
    }

    protected class VibrationCallback
    implements Vibrations.Callback {
        public static final int RANGE = 8;
        protected final BlockPos pos;
        private final PositionSource positionSource;

        public VibrationCallback(BlockPos pos) {
            this.pos = pos;
            this.positionSource = new BlockPositionSource(pos);
        }

        @Override
        public int getRange() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean triggersAvoidCriterion() {
            return true;
        }

        @Override
        public boolean accepts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable GameEvent.Emitter emitter) {
            if (pos.equals(this.pos) && (event.matches(GameEvent.BLOCK_DESTROY) || event.matches(GameEvent.BLOCK_PLACE))) {
                return false;
            }
            if (Vibrations.getFrequency(event) == 0) {
                return false;
            }
            return SculkSensorBlock.isInactive(SculkSensorBlockEntity.this.getCachedState());
        }

        @Override
        public void accept(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
            BlockState blockState = SculkSensorBlockEntity.this.getCachedState();
            if (SculkSensorBlock.isInactive(blockState)) {
                int i = Vibrations.getFrequency(event);
                SculkSensorBlockEntity.this.setLastVibrationFrequency(i);
                int j = Vibrations.getSignalStrength(distance, this.getRange());
                Block block = blockState.getBlock();
                if (block instanceof SculkSensorBlock) {
                    SculkSensorBlock sculkSensorBlock = (SculkSensorBlock)block;
                    sculkSensorBlock.setActive(sourceEntity, world, this.pos, blockState, j, i);
                }
            }
        }

        @Override
        public void onListen() {
            SculkSensorBlockEntity.this.markDirty();
        }

        @Override
        public boolean requiresTickingChunksAround() {
            return true;
        }
    }
}

