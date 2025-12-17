/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.item.consume;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import net.minecraft.item.consume.PlaySoundConsumeEffect;
import net.minecraft.item.consume.RemoveEffectsConsumeEffect;
import net.minecraft.item.consume.TeleportRandomlyConsumeEffect;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public interface ConsumeEffect {
    public static final Codec<ConsumeEffect> CODEC = Registries.CONSUME_EFFECT_TYPE.getCodec().dispatch(ConsumeEffect::getType, Type::codec);
    public static final PacketCodec<RegistryByteBuf, ConsumeEffect> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.CONSUME_EFFECT_TYPE).dispatch(ConsumeEffect::getType, Type::streamCodec);

    public Type<? extends ConsumeEffect> getType();

    public boolean onConsume(World var1, ItemStack var2, LivingEntity var3);

    public record Type<T extends ConsumeEffect>(MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> streamCodec) {
        public static final Type<ApplyEffectsConsumeEffect> APPLY_EFFECTS = Type.register("apply_effects", ApplyEffectsConsumeEffect.CODEC, ApplyEffectsConsumeEffect.PACKET_CODEC);
        public static final Type<RemoveEffectsConsumeEffect> REMOVE_EFFECTS = Type.register("remove_effects", RemoveEffectsConsumeEffect.CODEC, RemoveEffectsConsumeEffect.PACKET_CODEC);
        public static final Type<ClearAllEffectsConsumeEffect> CLEAR_ALL_EFFECTS = Type.register("clear_all_effects", ClearAllEffectsConsumeEffect.CODEC, ClearAllEffectsConsumeEffect.PACKET_CODEC);
        public static final Type<TeleportRandomlyConsumeEffect> TELEPORT_RANDOMLY = Type.register("teleport_randomly", TeleportRandomlyConsumeEffect.CODEC, TeleportRandomlyConsumeEffect.PACKET_CODEC);
        public static final Type<PlaySoundConsumeEffect> PLAY_SOUND = Type.register("play_sound", PlaySoundConsumeEffect.CODEC, PlaySoundConsumeEffect.PACKET_CODEC);

        private static <T extends ConsumeEffect> Type<T> register(String id, MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) {
            return Registry.register(Registries.CONSUME_EFFECT_TYPE, id, new Type<T>(codec, packetCodec));
        }
    }
}

