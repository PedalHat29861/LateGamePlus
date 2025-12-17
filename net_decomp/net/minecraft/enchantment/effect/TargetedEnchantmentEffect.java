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
package net.minecraft.enchantment.effect;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.context.ContextType;

public record TargetedEnchantmentEffect<T>(EnchantmentEffectTarget enchanted, EnchantmentEffectTarget affected, T effect, Optional<LootCondition> requirements) {
    public static <S> Codec<TargetedEnchantmentEffect<S>> createPostAttackCodec(Codec<S> effectCodec, ContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group((App)EnchantmentEffectTarget.CODEC.fieldOf("enchanted").forGetter(TargetedEnchantmentEffect::enchanted), (App)EnchantmentEffectTarget.CODEC.fieldOf("affected").forGetter(TargetedEnchantmentEffect::affected), (App)effectCodec.fieldOf("effect").forGetter(TargetedEnchantmentEffect::effect), (App)EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(TargetedEnchantmentEffect::requirements)).apply((Applicative)instance, TargetedEnchantmentEffect::new));
    }

    public static <S> Codec<TargetedEnchantmentEffect<S>> createEquipmentDropsCodec(Codec<S> effectCodec, ContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group((App)EnchantmentEffectTarget.CODEC.validate(enchanted -> enchanted != EnchantmentEffectTarget.DAMAGING_ENTITY ? DataResult.success((Object)enchanted) : DataResult.error(() -> "enchanted must be attacker or victim")).fieldOf("enchanted").forGetter(TargetedEnchantmentEffect::enchanted), (App)effectCodec.fieldOf("effect").forGetter(TargetedEnchantmentEffect::effect), (App)EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(TargetedEnchantmentEffect::requirements)).apply((Applicative)instance, (enchantedx, effect, requirements) -> new TargetedEnchantmentEffect<Object>((EnchantmentEffectTarget)enchantedx, EnchantmentEffectTarget.VICTIM, effect, (Optional<LootCondition>)requirements)));
    }

    public boolean test(LootContext lootContext) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(lootContext);
    }
}

