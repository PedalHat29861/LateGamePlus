/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  it.unimi.dsi.fastutil.objects.ReferenceSet
 */
package net.minecraft.command.permission;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionPredicate;

public class OrPermissionPredicate
implements PermissionPredicate {
    private final ReferenceSet<PermissionPredicate> predicates = new ReferenceArraySet();

    OrPermissionPredicate(PermissionPredicate a, PermissionPredicate b) {
        this.predicates.add((Object)a);
        this.predicates.add((Object)b);
        this.validate();
    }

    private OrPermissionPredicate(ReferenceSet<PermissionPredicate> predicates, PermissionPredicate predicate) {
        this.predicates.addAll(predicates);
        this.predicates.add((Object)predicate);
        this.validate();
    }

    private OrPermissionPredicate(ReferenceSet<PermissionPredicate> a, ReferenceSet<PermissionPredicate> b) {
        this.predicates.addAll(a);
        this.predicates.addAll(b);
        this.validate();
    }

    @Override
    public boolean hasPermission(Permission permission) {
        for (PermissionPredicate permissionPredicate : this.predicates) {
            if (!permissionPredicate.hasPermission(permission)) continue;
            return true;
        }
        return false;
    }

    @Override
    public PermissionPredicate or(PermissionPredicate other) {
        if (other instanceof OrPermissionPredicate) {
            OrPermissionPredicate orPermissionPredicate = (OrPermissionPredicate)other;
            return new OrPermissionPredicate(this.predicates, orPermissionPredicate.predicates);
        }
        return new OrPermissionPredicate(this.predicates, other);
    }

    @VisibleForTesting
    public ReferenceSet<PermissionPredicate> getPredicates() {
        return new ReferenceArraySet(this.predicates);
    }

    private void validate() {
        for (PermissionPredicate permissionPredicate : this.predicates) {
            if (!(permissionPredicate instanceof OrPermissionPredicate)) continue;
            throw new IllegalArgumentException("Cannot have PermissionSetUnion within another PermissionSetUnion");
        }
    }
}

