/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.provider.nbt;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameter;

public record StorageLootNbtProvider(Identifier source) implements LootNbtProvider
{
    public static final MapCodec<StorageLootNbtProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("source").forGetter(StorageLootNbtProvider::source)).apply((Applicative)instance, StorageLootNbtProvider::new));

    @Override
    public LootNbtProviderType getType() {
        return LootNbtProviderTypes.STORAGE;
    }

    @Override
    public NbtElement getNbt(LootContext context) {
        return context.getWorld().getServer().getDataCommandStorage().get(this.source);
    }

    @Override
    public Set<ContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{StorageLootNbtProvider.class, "id", "source"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StorageLootNbtProvider.class, "id", "source"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StorageLootNbtProvider.class, "id", "source"}, this, object);
    }
}

