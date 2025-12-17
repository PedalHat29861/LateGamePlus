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
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record ReplaceBlockEnchantmentEffect(Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState, Optional<RegistryEntry<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect
{
    public static final MapCodec<ReplaceBlockEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3i.CODEC.optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(ReplaceBlockEnchantmentEffect::offset), (App)BlockPredicate.BASE_CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlockEnchantmentEffect::predicate), (App)BlockStateProvider.TYPE_CODEC.fieldOf("block_state").forGetter(ReplaceBlockEnchantmentEffect::blockState), (App)GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceBlockEnchantmentEffect::triggerGameEvent)).apply((Applicative)instance, ReplaceBlockEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos).add(this.offset);
        if (this.predicate.map(predicate -> predicate.test(world, blockPos)).orElse(true).booleanValue() && world.setBlockState(blockPos, this.blockState.get(user.getRandom(), blockPos))) {
            this.triggerGameEvent.ifPresent(gameEvent -> world.emitGameEvent(user, (RegistryEntry<GameEvent>)gameEvent, blockPos));
        }
    }

    public MapCodec<ReplaceBlockEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

