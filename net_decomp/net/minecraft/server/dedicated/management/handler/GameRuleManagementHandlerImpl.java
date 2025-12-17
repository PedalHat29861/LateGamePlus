/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.stream.Stream;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

public class GameRuleManagementHandlerImpl
implements GameRuleManagementHandler {
    private final MinecraftDedicatedServer server;
    private final GameRules gameRules;
    private final ManagementLogger logger;

    public GameRuleManagementHandlerImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
        this.server = server;
        this.gameRules = server.getSaveProperties().getGameRules();
        this.logger = logger;
    }

    @Override
    public <T> GameRuleRpcDispatcher.RuleEntry<T> updateRule(GameRuleRpcDispatcher.RuleEntry<T> entry, ManagementConnectionId remote) {
        GameRule<T> gameRule = entry.gameRule();
        T object = this.gameRules.getValue(gameRule);
        T object2 = entry.value();
        this.gameRules.setValue(gameRule, object2, this.server);
        this.logger.logAction(remote, "Game rule '{}' updated from '{}' to '{}'", gameRule.toShortString(), gameRule.getValueName(object), gameRule.getValueName(object2));
        return entry;
    }

    @Override
    public <T> GameRuleRpcDispatcher.RuleEntry<T> toEntry(GameRule<T> rule, T value) {
        return new GameRuleRpcDispatcher.RuleEntry<T>(rule, value);
    }

    @Override
    public Stream<GameRule<?>> getRules() {
        return this.gameRules.streamRules();
    }

    @Override
    public <T> T getValue(GameRule<T> rule) {
        return this.gameRules.getValue(rule);
    }
}

