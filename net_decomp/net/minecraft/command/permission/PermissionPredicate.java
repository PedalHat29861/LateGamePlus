/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command.permission;

import net.minecraft.command.permission.OrPermissionPredicate;
import net.minecraft.command.permission.Permission;

public interface PermissionPredicate {
    public static final PermissionPredicate NONE = perm -> false;
    public static final PermissionPredicate ALL = perm -> true;

    public boolean hasPermission(Permission var1);

    default public PermissionPredicate or(PermissionPredicate other) {
        if (other instanceof OrPermissionPredicate) {
            return other.or(this);
        }
        return new OrPermissionPredicate(this, other);
    }
}

