/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.village;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public record VillagerData(RegistryEntry<VillagerType> type, RegistryEntry<VillagerProfession> profession, int level) {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    private static final int[] LEVEL_BASE_EXPERIENCE = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Registries.VILLAGER_TYPE.getEntryCodec().fieldOf("type").orElseGet(() -> Registries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS)).forGetter(data -> data.type), (App)Registries.VILLAGER_PROFESSION.getEntryCodec().fieldOf("profession").orElseGet(() -> Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE)).forGetter(data -> data.profession), (App)Codec.INT.fieldOf("level").orElse((Object)1).forGetter(data -> data.level)).apply((Applicative)instance, VillagerData::new));
    public static final PacketCodec<RegistryByteBuf, VillagerData> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.registryEntry(RegistryKeys.VILLAGER_TYPE), VillagerData::type, PacketCodecs.registryEntry(RegistryKeys.VILLAGER_PROFESSION), VillagerData::profession, PacketCodecs.VAR_INT, VillagerData::level, VillagerData::new);

    public VillagerData {
        level = Math.max(1, level);
    }

    public VillagerData withType(RegistryEntry<VillagerType> type) {
        return new VillagerData(type, this.profession, this.level);
    }

    public VillagerData withType(RegistryEntryLookup.RegistryLookup registries, RegistryKey<VillagerType> typeKey) {
        return this.withType(registries.getEntryOrThrow(typeKey));
    }

    public VillagerData withProfession(RegistryEntry<VillagerProfession> profession) {
        return new VillagerData(this.type, profession, this.level);
    }

    public VillagerData withProfession(RegistryEntryLookup.RegistryLookup registries, RegistryKey<VillagerProfession> professionKey) {
        return this.withProfession(registries.getEntryOrThrow(professionKey));
    }

    public VillagerData withLevel(int level) {
        return new VillagerData(this.type, this.profession, level);
    }

    public static int getLowerLevelExperience(int level) {
        return VillagerData.canLevelUp(level) ? LEVEL_BASE_EXPERIENCE[level - 1] : 0;
    }

    public static int getUpperLevelExperience(int level) {
        return VillagerData.canLevelUp(level) ? LEVEL_BASE_EXPERIENCE[level] : 0;
    }

    public static boolean canLevelUp(int level) {
        return level >= 1 && level < 5;
    }
}

