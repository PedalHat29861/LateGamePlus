/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.command.permission;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class PermissionLevel
extends Enum<PermissionLevel>
implements StringIdentifiable {
    public static final /* enum */ PermissionLevel ALL = new PermissionLevel("all", 0);
    public static final /* enum */ PermissionLevel MODERATORS = new PermissionLevel("moderators", 1);
    public static final /* enum */ PermissionLevel GAMEMASTERS = new PermissionLevel("gamemasters", 2);
    public static final /* enum */ PermissionLevel ADMINS = new PermissionLevel("admins", 3);
    public static final /* enum */ PermissionLevel OWNERS = new PermissionLevel("owners", 4);
    public static final Codec<PermissionLevel> CODEC;
    private static final IntFunction<PermissionLevel> BY_LEVEL;
    public static final Codec<PermissionLevel> NUMERIC_CODEC;
    private final String name;
    private final int level;
    private static final /* synthetic */ PermissionLevel[] field_63206;

    public static PermissionLevel[] values() {
        return (PermissionLevel[])field_63206.clone();
    }

    public static PermissionLevel valueOf(String string) {
        return Enum.valueOf(PermissionLevel.class, string);
    }

    private PermissionLevel(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public boolean isAtLeast(PermissionLevel other) {
        return this.level >= other.level;
    }

    public static PermissionLevel fromLevel(int level) {
        return BY_LEVEL.apply(level);
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ PermissionLevel[] method_75029() {
        return new PermissionLevel[]{ALL, MODERATORS, GAMEMASTERS, ADMINS, OWNERS};
    }

    static {
        field_63206 = PermissionLevel.method_75029();
        CODEC = StringIdentifiable.createCodec(PermissionLevel::values);
        BY_LEVEL = ValueLists.createIndexToValueFunction(level -> level.level, PermissionLevel.values(), ValueLists.OutOfBoundsHandling.CLAMP);
        NUMERIC_CODEC = Codec.INT.xmap(BY_LEVEL::apply, level -> level.level);
    }
}

