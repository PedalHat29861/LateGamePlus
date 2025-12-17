/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.WeightedAttributeList;
import org.jspecify.annotations.Nullable;

public interface EnvironmentAttributeAccess {
    public static final EnvironmentAttributeAccess DEFAULT = new EnvironmentAttributeAccess(){

        @Override
        public <Value> Value getAttributeValue(EnvironmentAttribute<Value> attribute) {
            return attribute.getDefaultValue();
        }

        @Override
        public <Value> Value getAttributeValue(EnvironmentAttribute<Value> attribute, Vec3d pos, @Nullable WeightedAttributeList pool) {
            return attribute.getDefaultValue();
        }
    };

    public <Value> Value getAttributeValue(EnvironmentAttribute<Value> var1);

    default public <Value> Value getAttributeValue(EnvironmentAttribute<Value> attribute, BlockPos pos) {
        return this.getAttributeValue(attribute, Vec3d.ofCenter(pos));
    }

    default public <Value> Value getAttributeValue(EnvironmentAttribute<Value> attribute, Vec3d pos) {
        return this.getAttributeValue(attribute, pos, null);
    }

    public <Value> Value getAttributeValue(EnvironmentAttribute<Value> var1, Vec3d var2, @Nullable WeightedAttributeList var3);
}

