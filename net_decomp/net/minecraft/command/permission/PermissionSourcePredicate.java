/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command.permission;

import java.util.function.Predicate;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.command.permission.PermissionSource;

public record PermissionSourcePredicate<T extends PermissionSource>(PermissionCheck test) implements Predicate<T>
{
    @Override
    public boolean test(T permissionSource) {
        return this.test.allows(permissionSource.getPermissions());
    }

    @Override
    public /* synthetic */ boolean test(Object source) {
        return this.test((T)((PermissionSource)source));
    }
}

