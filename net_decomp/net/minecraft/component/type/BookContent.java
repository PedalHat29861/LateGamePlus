/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.component.type;

import java.util.List;
import net.minecraft.text.RawFilteredPair;

public interface BookContent<T, C> {
    public List<RawFilteredPair<T>> pages();

    public C withPages(List<RawFilteredPair<T>> var1);
}

