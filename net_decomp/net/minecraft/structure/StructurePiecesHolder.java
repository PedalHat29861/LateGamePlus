/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.structure;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import org.jspecify.annotations.Nullable;

public interface StructurePiecesHolder {
    public void addPiece(StructurePiece var1);

    public @Nullable StructurePiece getIntersecting(BlockBox var1);
}

