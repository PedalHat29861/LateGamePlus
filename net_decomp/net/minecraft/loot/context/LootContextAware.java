/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.loot.context;

import java.util.Set;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.util.context.ContextParameter;

public interface LootContextAware {
    default public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of();
    }

    default public void validate(LootTableReporter reporter) {
        reporter.validateContext(this);
    }
}

