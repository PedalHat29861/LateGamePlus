/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.predicate.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.component.ComponentPredicate;

public record CustomDataPredicate(NbtPredicate value) implements ComponentPredicate
{
    public static final Codec<CustomDataPredicate> CODEC = NbtPredicate.CODEC.xmap(CustomDataPredicate::new, CustomDataPredicate::value);

    @Override
    public boolean test(ComponentsAccess components) {
        return this.value.test(components);
    }

    public static CustomDataPredicate customData(NbtPredicate value) {
        return new CustomDataPredicate(value);
    }
}

