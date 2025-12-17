/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.predicate.entity;

import com.mojang.serialization.MapCodec;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.FishingHookPredicate;
import net.minecraft.predicate.entity.LightningBoltPredicate;
import net.minecraft.predicate.entity.PlayerPredicate;
import net.minecraft.predicate.entity.RaiderPredicate;
import net.minecraft.predicate.entity.SheepPredicate;
import net.minecraft.predicate.entity.SlimePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntitySubPredicateTypes {
    public static final MapCodec<LightningBoltPredicate> LIGHTNING = EntitySubPredicateTypes.register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<FishingHookPredicate> FISHING_HOOK = EntitySubPredicateTypes.register("fishing_hook", FishingHookPredicate.CODEC);
    public static final MapCodec<PlayerPredicate> PLAYER = EntitySubPredicateTypes.register("player", PlayerPredicate.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = EntitySubPredicateTypes.register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = EntitySubPredicateTypes.register("raider", RaiderPredicate.CODEC);
    public static final MapCodec<SheepPredicate> SHEEP = EntitySubPredicateTypes.register("sheep", SheepPredicate.CODEC);

    private static <T extends EntitySubPredicate> MapCodec<T> register(String id, MapCodec<T> codec) {
        return Registry.register(Registries.ENTITY_SUB_PREDICATE_TYPE, id, codec);
    }

    public static MapCodec<? extends EntitySubPredicate> getDefault(Registry<MapCodec<? extends EntitySubPredicate>> registry) {
        return LIGHTNING;
    }
}

