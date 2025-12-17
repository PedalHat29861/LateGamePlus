/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class StructurePiecesCollector
implements StructurePiecesHolder {
    private final List<StructurePiece> pieces = Lists.newArrayList();

    @Override
    public void addPiece(StructurePiece piece) {
        this.pieces.add(piece);
    }

    @Override
    public @Nullable StructurePiece getIntersecting(BlockBox box) {
        return StructurePiece.firstIntersecting(this.pieces, box);
    }

    @Deprecated
    public void shift(int y) {
        for (StructurePiece structurePiece : this.pieces) {
            structurePiece.translate(0, y, 0);
        }
    }

    @Deprecated
    public int shiftInto(int topY, int bottomY, Random random, int topPenalty) {
        int i = topY - topPenalty;
        BlockBox blockBox = this.getBoundingBox();
        int j = blockBox.getBlockCountY() + bottomY + 1;
        if (j < i) {
            j += random.nextInt(i - j);
        }
        int k = j - blockBox.getMaxY();
        this.shift(k);
        return k;
    }

    public void shiftInto(Random random, int baseY, int topY) {
        BlockBox blockBox = this.getBoundingBox();
        int i = topY - baseY + 1 - blockBox.getBlockCountY();
        int j = i > 1 ? baseY + random.nextInt(i) : baseY;
        int k = j - blockBox.getMinY();
        this.shift(k);
    }

    public StructurePiecesList toList() {
        return new StructurePiecesList(this.pieces);
    }

    public void clear() {
        this.pieces.clear();
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public BlockBox getBoundingBox() {
        return StructurePiece.boundingBox(this.pieces.stream());
    }
}

