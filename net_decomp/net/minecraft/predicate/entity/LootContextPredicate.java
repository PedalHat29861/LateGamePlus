/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.predicate.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Util;

public class LootContextPredicate {
    public static final Codec<LootContextPredicate> CODEC = LootCondition.CODEC.listOf().xmap(LootContextPredicate::new, lootContextPredicate -> lootContextPredicate.conditions);
    private final List<LootCondition> conditions;
    private final Predicate<LootContext> combinedCondition;

    LootContextPredicate(List<LootCondition> conditions) {
        this.conditions = conditions;
        this.combinedCondition = Util.allOf(conditions);
    }

    public static LootContextPredicate create(LootCondition ... conditions) {
        return new LootContextPredicate(List.of(conditions));
    }

    public boolean test(LootContext context) {
        return this.combinedCondition.test(context);
    }

    public void validateConditions(LootTableReporter reporter) {
        for (int i = 0; i < this.conditions.size(); ++i) {
            LootCondition lootCondition = this.conditions.get(i);
            lootCondition.validate(reporter.makeChild(new ErrorReporter.ListElementContext(i)));
        }
    }
}

