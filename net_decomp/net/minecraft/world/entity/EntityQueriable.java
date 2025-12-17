/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.UniquelyIdentifiable;
import org.jspecify.annotations.Nullable;

public interface EntityQueriable<IdentifiedType extends UniquelyIdentifiable> {
    public @Nullable IdentifiedType lookup(UUID var1);
}

