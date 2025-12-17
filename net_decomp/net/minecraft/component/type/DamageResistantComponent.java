/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public record DamageResistantComponent(TagKey<DamageType> types) {
    public static final Codec<DamageResistantComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TagKey.codec(RegistryKeys.DAMAGE_TYPE).fieldOf("types").forGetter(DamageResistantComponent::types)).apply((Applicative)instance, DamageResistantComponent::new));
    public static final PacketCodec<RegistryByteBuf, DamageResistantComponent> PACKET_CODEC = PacketCodec.tuple(TagKey.packetCodec(RegistryKeys.DAMAGE_TYPE), DamageResistantComponent::types, DamageResistantComponent::new);

    public boolean resists(DamageSource damageSource) {
        return damageSource.isIn(this.types);
    }
}

