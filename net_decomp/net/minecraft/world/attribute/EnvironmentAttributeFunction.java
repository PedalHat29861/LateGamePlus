/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.WeightedAttributeList;
import org.jspecify.annotations.Nullable;

public sealed interface EnvironmentAttributeFunction<Value> {

    @FunctionalInterface
    public static interface Positional<Value>
    extends EnvironmentAttributeFunction<Value> {
        public Value applyPositional(Value var1, Vec3d var2, @Nullable WeightedAttributeList var3);
    }

    @FunctionalInterface
    public static interface TimeBased<Value>
    extends EnvironmentAttributeFunction<Value> {
        public Value applyTimeBased(Value var1, int var2);
    }

    @FunctionalInterface
    public static interface Constant<Value>
    extends EnvironmentAttributeFunction<Value> {
        public Value applyConstant(Value var1);
    }
}

