/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.rule;

import net.minecraft.world.rule.GameRule;

public interface GameRuleVisitor {
    default public <T> void visit(GameRule<T> rule) {
    }

    default public void visitBoolean(GameRule<Boolean> rule) {
    }

    default public void visitInt(GameRule<Integer> rule) {
    }
}

