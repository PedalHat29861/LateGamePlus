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
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.context.ContextType;

public record EnchantmentEffectEntry<T>(T effect, Optional<LootCondition> requirements) {
    public static Codec<LootCondition> createRequirementsCodec(ContextType lootContextType) {
        return LootCondition.CODEC.validate(condition -> {
            ErrorReporter.Impl impl = new ErrorReporter.Impl();
            LootTableReporter lootTableReporter = new LootTableReporter(impl, lootContextType);
            condition.validate(lootTableReporter);
            if (!impl.isEmpty()) {
                return DataResult.error(() -> "Validation error in enchantment effect condition: " + impl.getErrorsAsString());
            }
            return DataResult.success((Object)condition);
        });
    }

    public static <T> Codec<EnchantmentEffectEntry<T>> createCodec(Codec<T> effectCodec, ContextType lootContextType) {
        return RecordCodecBuilder.create(instance -> instance.group((App)effectCodec.fieldOf("effect").forGetter(EnchantmentEffectEntry::effect), (App)EnchantmentEffectEntry.createRequirementsCodec(lootContextType).optionalFieldOf("requirements").forGetter(EnchantmentEffectEntry::requirements)).apply((Applicative)instance, EnchantmentEffectEntry::new));
    }

    public boolean test(LootContext context) {
        if (this.requirements.isEmpty()) {
            return true;
        }
        return this.requirements.get().test(context);
    }
}

