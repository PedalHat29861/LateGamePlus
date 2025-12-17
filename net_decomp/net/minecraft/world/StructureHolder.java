/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.gen.structure.Structure;
import org.jspecify.annotations.Nullable;

public interface StructureHolder {
    public @Nullable StructureStart getStructureStart(Structure var1);

    public void setStructureStart(Structure var1, StructureStart var2);

    public LongSet getStructureReferences(Structure var1);

    public void addStructureReference(Structure var1, long var2);

    public Map<Structure, LongSet> getStructureReferences();

    public void setStructureReferences(Map<Structure, LongSet> var1);
}

