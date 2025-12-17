/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.dialog.input;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;

public interface InputControl {
    public static final MapCodec<InputControl> CODEC = Registries.INPUT_CONTROL_TYPE.getCodec().dispatchMap(InputControl::getCodec, mapCodec -> mapCodec);

    public MapCodec<? extends InputControl> getCodec();
}

