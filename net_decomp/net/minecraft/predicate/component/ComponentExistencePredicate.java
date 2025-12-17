/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.predicate.component;

import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.predicate.component.ComponentPredicate;

public record ComponentExistencePredicate(ComponentType<?> type) implements ComponentPredicate
{
    @Override
    public boolean test(ComponentsAccess components) {
        return components.get(this.type) != null;
    }
}

