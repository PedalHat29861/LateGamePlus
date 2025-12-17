/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.provider.score;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.provider.score.LootScoreProvider;

public record LootScoreProviderType(MapCodec<? extends LootScoreProvider> codec) {
}

