/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;

public final class BooleanModifier
extends Enum<BooleanModifier>
implements EnvironmentAttributeModifier<Boolean, Boolean> {
    public static final /* enum */ BooleanModifier AND = new BooleanModifier();
    public static final /* enum */ BooleanModifier NAND = new BooleanModifier();
    public static final /* enum */ BooleanModifier OR = new BooleanModifier();
    public static final /* enum */ BooleanModifier NOR = new BooleanModifier();
    public static final /* enum */ BooleanModifier XOR = new BooleanModifier();
    public static final /* enum */ BooleanModifier XNOR = new BooleanModifier();
    private static final /* synthetic */ BooleanModifier[] field_63794;

    public static BooleanModifier[] values() {
        return (BooleanModifier[])field_63794.clone();
    }

    public static BooleanModifier valueOf(String string) {
        return Enum.valueOf(BooleanModifier.class, string);
    }

    @Override
    public Boolean apply(Boolean boolean_, Boolean boolean2) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> boolean2 != false && boolean_ != false;
            case 1 -> boolean2 == false || boolean_ == false;
            case 2 -> boolean2 != false || boolean_ != false;
            case 3 -> boolean2 == false && boolean_ == false;
            case 4 -> boolean2 ^ boolean_;
            case 5 -> boolean2 == boolean_;
        };
    }

    @Override
    public Codec<Boolean> argumentCodec(EnvironmentAttribute<Boolean> environmentAttribute) {
        return Codec.BOOL;
    }

    @Override
    public Interpolator<Boolean> argumentKeyframeLerp(EnvironmentAttribute<Boolean> environmentAttribute) {
        return Interpolator.first();
    }

    @Override
    public /* synthetic */ Object apply(Object object, Object object2) {
        return this.apply((Boolean)object, (Boolean)object2);
    }

    private static /* synthetic */ BooleanModifier[] method_75716() {
        return new BooleanModifier[]{AND, NAND, OR, NOR, XOR, XNOR};
    }

    static {
        field_63794 = BooleanModifier.method_75716();
    }
}

