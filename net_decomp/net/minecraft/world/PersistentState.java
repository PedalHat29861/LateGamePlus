/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

public abstract class PersistentState {
    private boolean dirty;

    public void markDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }
}

