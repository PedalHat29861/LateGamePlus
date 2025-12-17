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
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.context.ContextParameter;

public record EnchantmentActiveCheckLootCondition(boolean active) implements LootCondition
{
    public static final MapCodec<EnchantmentActiveCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("active").forGetter(EnchantmentActiveCheckLootCondition::active)).apply((Applicative)instance, EnchantmentActiveCheckLootCondition::new));

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getOrThrow(LootContextParameters.ENCHANTMENT_ACTIVE) == this.active;
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENCHANTMENT_ACTIVE_CHECK;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ENCHANTMENT_ACTIVE);
    }

    public static LootCondition.Builder requireActive() {
        return () -> new EnchantmentActiveCheckLootCondition(true);
    }

    public static LootCondition.Builder requireInactive() {
        return () -> new EnchantmentActiveCheckLootCondition(false);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

