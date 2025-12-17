/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command.permission;

import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.command.permission.PermissionPredicate;

public interface LeveledPermissionPredicate
extends PermissionPredicate {
    @Deprecated
    public static final LeveledPermissionPredicate ALL = LeveledPermissionPredicate.create(PermissionLevel.ALL);
    public static final LeveledPermissionPredicate MODERATORS = LeveledPermissionPredicate.create(PermissionLevel.MODERATORS);
    public static final LeveledPermissionPredicate GAMEMASTERS = LeveledPermissionPredicate.create(PermissionLevel.GAMEMASTERS);
    public static final LeveledPermissionPredicate ADMINS = LeveledPermissionPredicate.create(PermissionLevel.ADMINS);
    public static final LeveledPermissionPredicate OWNERS = LeveledPermissionPredicate.create(PermissionLevel.OWNERS);

    public PermissionLevel getLevel();

    @Override
    default public boolean hasPermission(Permission permission) {
        if (permission instanceof Permission.Level) {
            Permission.Level level = (Permission.Level)permission;
            return this.getLevel().isAtLeast(level.level());
        }
        if (permission.equals(DefaultPermissions.ENTITY_SELECTORS)) {
            return this.getLevel().isAtLeast(PermissionLevel.GAMEMASTERS);
        }
        return false;
    }

    @Override
    default public PermissionPredicate or(PermissionPredicate other) {
        if (other instanceof LeveledPermissionPredicate) {
            LeveledPermissionPredicate leveledPermissionPredicate = (LeveledPermissionPredicate)other;
            if (this.getLevel().isAtLeast(leveledPermissionPredicate.getLevel())) {
                return leveledPermissionPredicate;
            }
            return this;
        }
        return PermissionPredicate.super.or(other);
    }

    public static LeveledPermissionPredicate fromLevel(PermissionLevel level) {
        return switch (level) {
            default -> throw new MatchException(null, null);
            case PermissionLevel.ALL -> ALL;
            case PermissionLevel.MODERATORS -> MODERATORS;
            case PermissionLevel.GAMEMASTERS -> GAMEMASTERS;
            case PermissionLevel.ADMINS -> ADMINS;
            case PermissionLevel.OWNERS -> OWNERS;
        };
    }

    private static LeveledPermissionPredicate create(final PermissionLevel level) {
        return new LeveledPermissionPredicate(){

            @Override
            public PermissionLevel getLevel() {
                return level;
            }

            public String toString() {
                return "permission level: " + level.name();
            }
        };
    }
}

