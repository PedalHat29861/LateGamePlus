/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.provider.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public record StorageLootNumberProvider(Identifier storage, NbtPathArgumentType.NbtPath path) implements LootNumberProvider
{
    public static final MapCodec<StorageLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("storage").forGetter(StorageLootNumberProvider::storage), (App)NbtPathArgumentType.NbtPath.CODEC.fieldOf("path").forGetter(StorageLootNumberProvider::path)).apply((Applicative)instance, StorageLootNumberProvider::new));

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.STORAGE;
    }

    private Number getNumber(LootContext context, Number fallback) {
        NbtCompound nbtCompound = context.getWorld().getServer().getDataCommandStorage().get(this.storage);
        try {
            NbtElement nbtElement;
            List<NbtElement> list = this.path.get(nbtCompound);
            if (list.size() == 1 && (nbtElement = list.getFirst()) instanceof AbstractNbtNumber) {
                AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement;
                return abstractNbtNumber.numberValue();
            }
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return fallback;
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.getNumber(context, Float.valueOf(0.0f)).floatValue();
    }

    @Override
    public int nextInt(LootContext context) {
        return this.getNumber(context, 0).intValue();
    }
}

