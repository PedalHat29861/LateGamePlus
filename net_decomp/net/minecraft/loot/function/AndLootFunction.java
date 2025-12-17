/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.ErrorReporter;

public class AndLootFunction
implements LootFunction {
    public static final MapCodec<AndLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootFunctionTypes.BASE_CODEC.listOf().fieldOf("functions").forGetter(function -> function.terms)).apply((Applicative)instance, AndLootFunction::new));
    public static final Codec<AndLootFunction> INLINE_CODEC = LootFunctionTypes.BASE_CODEC.listOf().xmap(AndLootFunction::new, function -> function.terms);
    private final List<LootFunction> terms;
    private final BiFunction<ItemStack, LootContext, ItemStack> applier;

    private AndLootFunction(List<LootFunction> terms) {
        this.terms = terms;
        this.applier = LootFunctionTypes.join(terms);
    }

    public static AndLootFunction create(List<LootFunction> terms) {
        return new AndLootFunction(List.copyOf(terms));
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return this.applier.apply(itemStack, lootContext);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        LootFunction.super.validate(reporter);
        for (int i = 0; i < this.terms.size(); ++i) {
            this.terms.get(i).validate(reporter.makeChild(new ErrorReporter.NamedListElementContext("functions", i)));
        }
    }

    public LootFunctionType<AndLootFunction> getType() {
        return LootFunctionTypes.SEQUENCE;
    }

    @Override
    public /* synthetic */ Object apply(Object stack, Object context) {
        return this.apply((ItemStack)stack, (LootContext)context);
    }
}

