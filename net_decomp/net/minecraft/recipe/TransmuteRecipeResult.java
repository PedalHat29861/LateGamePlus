/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;

public record TransmuteRecipeResult(RegistryEntry<Item> itemEntry, int count, ComponentChanges components) {
    private static final Codec<TransmuteRecipeResult> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Item.ENTRY_CODEC.fieldOf("id").forGetter(TransmuteRecipeResult::itemEntry), (App)Codecs.rangedInt(1, 99).optionalFieldOf("count", (Object)1).forGetter(TransmuteRecipeResult::count), (App)ComponentChanges.CODEC.optionalFieldOf("components", (Object)ComponentChanges.EMPTY).forGetter(TransmuteRecipeResult::components)).apply((Applicative)instance, TransmuteRecipeResult::new));
    public static final Codec<TransmuteRecipeResult> CODEC = Codec.withAlternative(BASE_CODEC, Item.ENTRY_CODEC, itemEntry -> new TransmuteRecipeResult((Item)itemEntry.value())).validate(TransmuteRecipeResult::validate);
    public static final PacketCodec<RegistryByteBuf, TransmuteRecipeResult> PACKET_CODEC = PacketCodec.tuple(Item.ENTRY_PACKET_CODEC, TransmuteRecipeResult::itemEntry, PacketCodecs.VAR_INT, TransmuteRecipeResult::count, ComponentChanges.PACKET_CODEC, TransmuteRecipeResult::components, TransmuteRecipeResult::new);

    public TransmuteRecipeResult(Item item) {
        this(item.getRegistryEntry(), 1, ComponentChanges.EMPTY);
    }

    private static DataResult<TransmuteRecipeResult> validate(TransmuteRecipeResult result) {
        return ItemStack.validate(new ItemStack(result.itemEntry, result.count, result.components)).map(stack -> result);
    }

    public ItemStack apply(ItemStack stack) {
        ItemStack itemStack = stack.copyComponentsToNewStack(this.itemEntry.value(), this.count);
        itemStack.applyUnvalidatedChanges(this.components);
        return itemStack;
    }

    public boolean isEqualToResult(ItemStack stack) {
        ItemStack itemStack = this.apply(stack);
        return itemStack.getCount() == 1 && ItemStack.areItemsAndComponentsEqual(stack, itemStack);
    }

    public SlotDisplay createSlotDisplay() {
        return new SlotDisplay.StackSlotDisplay(new ItemStack(this.itemEntry, this.count, this.components));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{TransmuteRecipeResult.class, "item;count;components", "itemEntry", "count", "components"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TransmuteRecipeResult.class, "item;count;components", "itemEntry", "count", "components"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TransmuteRecipeResult.class, "item;count;components", "itemEntry", "count", "components"}, this, object);
    }
}

