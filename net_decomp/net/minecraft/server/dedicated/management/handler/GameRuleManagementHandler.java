/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.stream.Stream;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.rule.GameRule;

public interface GameRuleManagementHandler {
    public <T> GameRuleRpcDispatcher.RuleEntry<T> updateRule(GameRuleRpcDispatcher.RuleEntry<T> var1, ManagementConnectionId var2);

    public <T> T getValue(GameRule<T> var1);

    public <T> GameRuleRpcDispatcher.RuleEntry<T> toEntry(GameRule<T> var1, T var2);

    public Stream<GameRule<?>> getRules();
}

