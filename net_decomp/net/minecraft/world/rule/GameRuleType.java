/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.rule;

import net.minecraft.util.StringIdentifiable;

public final class GameRuleType
extends Enum<GameRuleType>
implements StringIdentifiable {
    public static final /* enum */ GameRuleType INT = new GameRuleType("integer");
    public static final /* enum */ GameRuleType BOOL = new GameRuleType("boolean");
    private final String name;
    private static final /* synthetic */ GameRuleType[] field_62402;

    public static GameRuleType[] values() {
        return (GameRuleType[])field_62402.clone();
    }

    public static GameRuleType valueOf(String string) {
        return Enum.valueOf(GameRuleType.class, string);
    }

    private GameRuleType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ GameRuleType[] method_73877() {
        return new GameRuleType[]{INT, BOOL};
    }

    static {
        field_62402 = GameRuleType.method_73877();
    }
}

