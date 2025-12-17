/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 */
package net.minecraft.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;

public class BlockState
extends AbstractBlock.AbstractBlockState {
    public static final Codec<BlockState> CODEC = BlockState.createCodec(Registries.BLOCK.getCodec(), Block::getDefaultState).stable();

    public BlockState(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
        super(block, reference2ObjectArrayMap, mapCodec);
    }

    @Override
    protected BlockState asBlockState() {
        return this;
    }
}

