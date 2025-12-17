/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.particle.TrailParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;

public class EyeblossomBlock
extends FlowerBlock {
    public static final MapCodec<EyeblossomBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("open").forGetter(block -> block.state.open), EyeblossomBlock.createSettingsCodec()).apply((Applicative)instance, EyeblossomBlock::new));
    private static final int NOTIFY_RANGE_XZ = 3;
    private static final int NOTIFY_RANGE_Y = 2;
    private final EyeblossomState state;

    public MapCodec<? extends EyeblossomBlock> getCodec() {
        return CODEC;
    }

    public EyeblossomBlock(EyeblossomState state, AbstractBlock.Settings settings) {
        super(state.stewEffect, state.effectLengthInSeconds, settings);
        this.state = state;
    }

    public EyeblossomBlock(boolean open, AbstractBlock.Settings settings) {
        super(EyeblossomState.of((boolean)open).stewEffect, EyeblossomState.of((boolean)open).effectLengthInSeconds, settings);
        this.state = EyeblossomState.of(open);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockState blockState;
        if (this.state.isOpen() && random.nextInt(700) == 0 && (blockState = world.getBlockState(pos.down())).isOf(Blocks.PALE_MOSS_BLOCK)) {
            world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_EYEBLOSSOM_IDLE, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.updateStateAndNotifyOthers(state, world, pos, random)) {
            world.playSound(null, pos, this.state.getOpposite().longSound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        super.randomTick(state, world, pos, random);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.updateStateAndNotifyOthers(state, world, pos, random)) {
            world.playSound(null, pos, this.state.getOpposite().sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        super.scheduledTick(state, world, pos, random);
    }

    private boolean updateStateAndNotifyOthers(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean bl = world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.EYEBLOSSOM_OPEN_GAMEPLAY, pos).asBoolean(this.state.open);
        if (bl == this.state.open) {
            return false;
        }
        EyeblossomState eyeblossomState = this.state.getOpposite();
        world.setBlockState(pos, eyeblossomState.getBlockState(), 3);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
        eyeblossomState.spawnTrailParticle(world, pos, random);
        BlockPos.iterate(pos.add(-3, -2, -3), pos.add(3, 2, 3)).forEach(otherPos -> {
            BlockState blockState2 = world.getBlockState((BlockPos)otherPos);
            if (blockState2 == state) {
                double d = Math.sqrt(pos.getSquaredDistance((Vec3i)otherPos));
                int i = random.nextBetween((int)(d * 5.0), (int)(d * 10.0));
                world.scheduleBlockTick((BlockPos)otherPos, state.getBlock(), i);
            }
        });
        return true;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (!world.isClient() && world.getDifficulty() != Difficulty.PEACEFUL && entity instanceof BeeEntity) {
            BeeEntity beeEntity = (BeeEntity)entity;
            if (BeeEntity.isAttractive(state) && !beeEntity.hasStatusEffect(StatusEffects.POISON)) {
                beeEntity.addStatusEffect(this.getContactEffect());
            }
        }
    }

    @Override
    public StatusEffectInstance getContactEffect() {
        return new StatusEffectInstance(StatusEffects.POISON, 25);
    }

    public static final class EyeblossomState
    extends Enum<EyeblossomState> {
        public static final /* enum */ EyeblossomState OPEN = new EyeblossomState(true, StatusEffects.BLINDNESS, 11.0f, SoundEvents.BLOCK_EYEBLOSSOM_OPEN_LONG, SoundEvents.BLOCK_EYEBLOSSOM_OPEN, 16545810);
        public static final /* enum */ EyeblossomState CLOSED = new EyeblossomState(false, StatusEffects.NAUSEA, 7.0f, SoundEvents.BLOCK_EYEBLOSSOM_CLOSE_LONG, SoundEvents.BLOCK_EYEBLOSSOM_CLOSE, 0x5F5F5F);
        final boolean open;
        final RegistryEntry<StatusEffect> stewEffect;
        final float effectLengthInSeconds;
        final SoundEvent longSound;
        final SoundEvent sound;
        private final int particleColor;
        private static final /* synthetic */ EyeblossomState[] field_55078;

        public static EyeblossomState[] values() {
            return (EyeblossomState[])field_55078.clone();
        }

        public static EyeblossomState valueOf(String string) {
            return Enum.valueOf(EyeblossomState.class, string);
        }

        private EyeblossomState(boolean open, RegistryEntry<StatusEffect> stewEffect, float effectLengthInSeconds, SoundEvent longSound, SoundEvent sound, int particleColor) {
            this.open = open;
            this.stewEffect = stewEffect;
            this.effectLengthInSeconds = effectLengthInSeconds;
            this.longSound = longSound;
            this.sound = sound;
            this.particleColor = particleColor;
        }

        public Block getBlock() {
            return this.open ? Blocks.OPEN_EYEBLOSSOM : Blocks.CLOSED_EYEBLOSSOM;
        }

        public BlockState getBlockState() {
            return this.getBlock().getDefaultState();
        }

        public EyeblossomState getOpposite() {
            return EyeblossomState.of(!this.open);
        }

        public boolean isOpen() {
            return this.open;
        }

        public static EyeblossomState of(boolean open) {
            return open ? OPEN : CLOSED;
        }

        public void spawnTrailParticle(ServerWorld world, BlockPos pos, Random random) {
            Vec3d vec3d = pos.toCenterPos();
            double d = 0.5 + random.nextDouble();
            Vec3d vec3d2 = new Vec3d(random.nextDouble() - 0.5, random.nextDouble() + 1.0, random.nextDouble() - 0.5);
            Vec3d vec3d3 = vec3d.add(vec3d2.multiply(d));
            TrailParticleEffect trailParticleEffect = new TrailParticleEffect(vec3d3, this.particleColor, (int)(20.0 * d));
            world.spawnParticles(trailParticleEffect, vec3d.x, vec3d.y, vec3d.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        public SoundEvent getLongSound() {
            return this.longSound;
        }

        private static /* synthetic */ EyeblossomState[] method_65159() {
            return new EyeblossomState[]{OPEN, CLOSED};
        }

        static {
            field_55078 = EyeblossomState.method_65159();
        }
    }
}

