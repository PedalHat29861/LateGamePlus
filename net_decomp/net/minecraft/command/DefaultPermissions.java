/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command;

import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;

public class DefaultPermissions {
    public static final Permission MODERATORS = new Permission.Level(PermissionLevel.MODERATORS);
    public static final Permission GAMEMASTERS = new Permission.Level(PermissionLevel.GAMEMASTERS);
    public static final Permission ADMINS = new Permission.Level(PermissionLevel.ADMINS);
    public static final Permission OWNERS = new Permission.Level(PermissionLevel.OWNERS);
    public static final Permission ENTITY_SELECTORS = Permission.Atom.ofVanilla("commands/entity_selectors");
}

