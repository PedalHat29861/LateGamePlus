/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public interface Nameable {
    public Text getName();

    default public String getStringifiedName() {
        return this.getName().getString();
    }

    default public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    default public Text getDisplayName() {
        return this.getName();
    }

    default public @Nullable Text getCustomName() {
        return null;
    }
}

