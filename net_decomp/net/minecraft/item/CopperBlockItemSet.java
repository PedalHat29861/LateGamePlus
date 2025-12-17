/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableBiMap
 */
package net.minecraft.item;

import com.google.common.collect.ImmutableBiMap;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.CopperBlockSet;
import net.minecraft.item.Item;

public record CopperBlockItemSet(Item unaffected, Item exposed, Item weathered, Item oxidized, Item waxed, Item waxedExposed, Item waxedWeathered, Item waxedOxidized) {
    public static CopperBlockItemSet create(CopperBlockSet blockSet, Function<Block, Item> registerFunction) {
        return new CopperBlockItemSet(registerFunction.apply(blockSet.unaffected()), registerFunction.apply(blockSet.exposed()), registerFunction.apply(blockSet.weathered()), registerFunction.apply(blockSet.oxidized()), registerFunction.apply(blockSet.waxed()), registerFunction.apply(blockSet.waxedExposed()), registerFunction.apply(blockSet.waxedWeathered()), registerFunction.apply(blockSet.waxedOxidized()));
    }

    public ImmutableBiMap<Item, Item> getWaxingMap() {
        return ImmutableBiMap.of((Object)this.unaffected, (Object)this.waxed, (Object)this.exposed, (Object)this.waxedExposed, (Object)this.weathered, (Object)this.waxedWeathered, (Object)this.oxidized, (Object)this.waxedOxidized);
    }

    public void forEach(Consumer<Item> consumer) {
        consumer.accept(this.unaffected);
        consumer.accept(this.exposed);
        consumer.accept(this.weathered);
        consumer.accept(this.oxidized);
        consumer.accept(this.waxed);
        consumer.accept(this.waxedExposed);
        consumer.accept(this.waxedWeathered);
        consumer.accept(this.waxedOxidized);
    }
}

