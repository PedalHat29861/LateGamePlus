/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import java.util.function.IntFunction;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jspecify.annotations.Nullable;

public final class ScoreboardDisplaySlot
extends Enum<ScoreboardDisplaySlot>
implements StringIdentifiable {
    public static final /* enum */ ScoreboardDisplaySlot LIST = new ScoreboardDisplaySlot(0, "list");
    public static final /* enum */ ScoreboardDisplaySlot SIDEBAR = new ScoreboardDisplaySlot(1, "sidebar");
    public static final /* enum */ ScoreboardDisplaySlot BELOW_NAME = new ScoreboardDisplaySlot(2, "below_name");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_BLACK = new ScoreboardDisplaySlot(3, "sidebar.team.black");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_DARK_BLUE = new ScoreboardDisplaySlot(4, "sidebar.team.dark_blue");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_DARK_GREEN = new ScoreboardDisplaySlot(5, "sidebar.team.dark_green");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_DARK_AQUA = new ScoreboardDisplaySlot(6, "sidebar.team.dark_aqua");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_DARK_RED = new ScoreboardDisplaySlot(7, "sidebar.team.dark_red");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_DARK_PURPLE = new ScoreboardDisplaySlot(8, "sidebar.team.dark_purple");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_GOLD = new ScoreboardDisplaySlot(9, "sidebar.team.gold");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_GRAY = new ScoreboardDisplaySlot(10, "sidebar.team.gray");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_DARK_GRAY = new ScoreboardDisplaySlot(11, "sidebar.team.dark_gray");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_BLUE = new ScoreboardDisplaySlot(12, "sidebar.team.blue");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_GREEN = new ScoreboardDisplaySlot(13, "sidebar.team.green");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_AQUA = new ScoreboardDisplaySlot(14, "sidebar.team.aqua");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_RED = new ScoreboardDisplaySlot(15, "sidebar.team.red");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_LIGHT_PURPLE = new ScoreboardDisplaySlot(16, "sidebar.team.light_purple");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_YELLOW = new ScoreboardDisplaySlot(17, "sidebar.team.yellow");
    public static final /* enum */ ScoreboardDisplaySlot TEAM_WHITE = new ScoreboardDisplaySlot(18, "sidebar.team.white");
    public static final StringIdentifiable.EnumCodec<ScoreboardDisplaySlot> CODEC;
    public static final IntFunction<ScoreboardDisplaySlot> FROM_ID;
    private final int id;
    private final String name;
    private static final /* synthetic */ ScoreboardDisplaySlot[] field_45179;

    public static ScoreboardDisplaySlot[] values() {
        return (ScoreboardDisplaySlot[])field_45179.clone();
    }

    public static ScoreboardDisplaySlot valueOf(String string) {
        return Enum.valueOf(ScoreboardDisplaySlot.class, string);
    }

    private ScoreboardDisplaySlot(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public static @Nullable ScoreboardDisplaySlot fromFormatting(Formatting formatting) {
        return switch (formatting) {
            default -> throw new MatchException(null, null);
            case Formatting.BLACK -> TEAM_BLACK;
            case Formatting.DARK_BLUE -> TEAM_DARK_BLUE;
            case Formatting.DARK_GREEN -> TEAM_DARK_GREEN;
            case Formatting.DARK_AQUA -> TEAM_DARK_AQUA;
            case Formatting.DARK_RED -> TEAM_DARK_RED;
            case Formatting.DARK_PURPLE -> TEAM_DARK_PURPLE;
            case Formatting.GOLD -> TEAM_GOLD;
            case Formatting.GRAY -> TEAM_GRAY;
            case Formatting.DARK_GRAY -> TEAM_DARK_GRAY;
            case Formatting.BLUE -> TEAM_BLUE;
            case Formatting.GREEN -> TEAM_GREEN;
            case Formatting.AQUA -> TEAM_AQUA;
            case Formatting.RED -> TEAM_RED;
            case Formatting.LIGHT_PURPLE -> TEAM_LIGHT_PURPLE;
            case Formatting.YELLOW -> TEAM_YELLOW;
            case Formatting.WHITE -> TEAM_WHITE;
            case Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE, Formatting.RESET, Formatting.OBFUSCATED, Formatting.STRIKETHROUGH -> null;
        };
    }

    private static /* synthetic */ ScoreboardDisplaySlot[] method_52623() {
        return new ScoreboardDisplaySlot[]{LIST, SIDEBAR, BELOW_NAME, TEAM_BLACK, TEAM_DARK_BLUE, TEAM_DARK_GREEN, TEAM_DARK_AQUA, TEAM_DARK_RED, TEAM_DARK_PURPLE, TEAM_GOLD, TEAM_GRAY, TEAM_DARK_GRAY, TEAM_BLUE, TEAM_GREEN, TEAM_AQUA, TEAM_RED, TEAM_LIGHT_PURPLE, TEAM_YELLOW, TEAM_WHITE};
    }

    static {
        field_45179 = ScoreboardDisplaySlot.method_52623();
        CODEC = StringIdentifiable.createCodec(ScoreboardDisplaySlot::values);
        FROM_ID = ValueLists.createIndexToValueFunction(ScoreboardDisplaySlot::getId, ScoreboardDisplaySlot.values(), ValueLists.OutOfBoundsHandling.ZERO);
    }
}

