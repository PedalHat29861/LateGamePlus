/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.passive;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.AssetInfo;

public record FrogVariant(AssetInfo.TextureAssetInfo assetInfo, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<FrogVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)AssetInfo.TextureAssetInfo.MAP_CODEC.forGetter(FrogVariant::assetInfo), (App)SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions").forGetter(FrogVariant::spawnConditions)).apply((Applicative)instance, FrogVariant::new));
    public static final Codec<FrogVariant> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)AssetInfo.TextureAssetInfo.MAP_CODEC.forGetter(FrogVariant::assetInfo)).apply((Applicative)instance, FrogVariant::new));
    public static final Codec<RegistryEntry<FrogVariant>> ENTRY_CODEC = RegistryFixedCodec.of(RegistryKeys.FROG_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<FrogVariant>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.FROG_VARIANT);

    private FrogVariant(AssetInfo.TextureAssetInfo assetInfo) {
        this(assetInfo, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<VariantSelectorProvider.Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }
}

