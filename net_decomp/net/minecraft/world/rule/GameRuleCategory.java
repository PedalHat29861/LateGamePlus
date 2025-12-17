/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record GameRuleCategory(Identifier id) {
    private static final List<GameRuleCategory> CATEGORIES = new ArrayList<GameRuleCategory>();
    public static final GameRuleCategory PLAYER = GameRuleCategory.register("player");
    public static final GameRuleCategory MOBS = GameRuleCategory.register("mobs");
    public static final GameRuleCategory SPAWNING = GameRuleCategory.register("spawning");
    public static final GameRuleCategory DROPS = GameRuleCategory.register("drops");
    public static final GameRuleCategory UPDATES = GameRuleCategory.register("updates");
    public static final GameRuleCategory CHAT = GameRuleCategory.register("chat");
    public static final GameRuleCategory MISC = GameRuleCategory.register("misc");

    public Identifier getCategory() {
        return this.id;
    }

    private static GameRuleCategory register(String name) {
        return GameRuleCategory.register(Identifier.ofVanilla(name));
    }

    public static GameRuleCategory register(Identifier id) {
        GameRuleCategory gameRuleCategory = new GameRuleCategory(id);
        if (CATEGORIES.contains(gameRuleCategory)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Category '%s' is already registered.", id));
        }
        CATEGORIES.add(gameRuleCategory);
        return gameRuleCategory;
    }

    public MutableText getText() {
        return Text.translatable(this.id.toTranslationKey("gamerule.category"));
    }
}

