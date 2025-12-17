/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.event.GameEvent;

public record SetBlockPropertiesEnchantmentEffect(BlockStateComponent properties, Vec3i offset, Optional<RegistryEntry<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect
{
    public static final MapCodec<SetBlockPropertiesEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockStateComponent.CODEC.fieldOf("properties").forGetter(SetBlockPropertiesEnchantmentEffect::properties), (App)Vec3i.CODEC.optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(SetBlockPropertiesEnchantmentEffect::offset), (App)GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(SetBlockPropertiesEnchantmentEffect::triggerGameEvent)).apply((Applicative)instance, SetBlockPropertiesEnchantmentEffect::new));

    public SetBlockPropertiesEnchantmentEffect(BlockStateComponent properties) {
        this(properties, Vec3i.ZERO, Optional.of(GameEvent.BLOCK_CHANGE));
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockState blockState2;
        BlockPos blockPos = BlockPos.ofFloored(pos).add(this.offset);
        BlockState blockState = user.getEntityWorld().getBlockState(blockPos);
        if (blockState != (blockState2 = this.properties.applyToState(blockState)) && user.getEntityWorld().setBlockState(blockPos, blockState2, 3)) {
            this.triggerGameEvent.ifPresent(gameEvent -> world.emitGameEvent(user, (RegistryEntry<GameEvent>)gameEvent, blockPos));
        }
    }

    public MapCodec<SetBlockPropertiesEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

