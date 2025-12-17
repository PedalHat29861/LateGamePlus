/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.predicate.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.context.ContextType;

public class LootContextPredicateValidator {
    private final ErrorReporter errorReporter;
    private final RegistryEntryLookup.RegistryLookup conditionsLookup;

    public LootContextPredicateValidator(ErrorReporter errorReporter, RegistryEntryLookup.RegistryLookup conditionsLookup) {
        this.errorReporter = errorReporter;
        this.conditionsLookup = conditionsLookup;
    }

    public void validateEntityPredicate(Optional<LootContextPredicate> predicate, String path) {
        predicate.ifPresent(p -> this.validateEntityPredicate((LootContextPredicate)p, path));
    }

    public void validateEntityPredicates(List<LootContextPredicate> predicates, String path) {
        this.validate(predicates, LootContextTypes.ADVANCEMENT_ENTITY, path);
    }

    public void validateEntityPredicate(LootContextPredicate predicate, String path) {
        this.validate(predicate, LootContextTypes.ADVANCEMENT_ENTITY, path);
    }

    public void validate(LootContextPredicate predicate, ContextType type, String path) {
        predicate.validateConditions(new LootTableReporter(this.errorReporter.makeChild(new ErrorReporter.MapElementContext(path)), type, this.conditionsLookup));
    }

    public void validate(List<LootContextPredicate> predicates, ContextType type, String path) {
        for (int i = 0; i < predicates.size(); ++i) {
            LootContextPredicate lootContextPredicate = predicates.get(i);
            lootContextPredicate.validateConditions(new LootTableReporter(this.errorReporter.makeChild(new ErrorReporter.NamedListElementContext(path, i)), type, this.conditionsLookup));
        }
    }
}

