/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Box;

public interface Hopper
extends Inventory {
    public static final Box INPUT_AREA_SHAPE = Block.createColumnShape(16.0, 11.0, 32.0).getBoundingBoxes().get(0);

    default public Box getInputAreaShape() {
        return INPUT_AREA_SHAPE;
    }

    public double getHopperX();

    public double getHopperY();

    public double getHopperZ();

    public boolean canBlockFromAbove();
}

