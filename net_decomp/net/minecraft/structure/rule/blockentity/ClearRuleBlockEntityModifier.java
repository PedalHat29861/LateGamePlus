/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.structure.rule.blockentity;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class ClearRuleBlockEntityModifier
implements RuleBlockEntityModifier {
    private static final ClearRuleBlockEntityModifier INSTANCE = new ClearRuleBlockEntityModifier();
    public static final MapCodec<ClearRuleBlockEntityModifier> CODEC = MapCodec.unit((Object)INSTANCE);

    @Override
    public NbtCompound modifyBlockEntityNbt(Random random, @Nullable NbtCompound nbt) {
        return new NbtCompound();
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.CLEAR;
    }
}

