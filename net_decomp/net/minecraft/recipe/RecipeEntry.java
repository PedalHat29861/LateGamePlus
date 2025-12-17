/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record RecipeEntry<T extends Recipe<?>>(RegistryKey<Recipe<?>> id, T value) {
    public static final PacketCodec<RegistryByteBuf, RecipeEntry<?>> PACKET_CODEC = PacketCodec.tuple(RegistryKey.createPacketCodec(RegistryKeys.RECIPE), RecipeEntry::id, Recipe.PACKET_CODEC, RecipeEntry::value, RecipeEntry::new);

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipeEntry)) return false;
        RecipeEntry recipeEntry = (RecipeEntry)o;
        if (this.id != recipeEntry.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id.toString();
    }
}

