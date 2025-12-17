/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record BooleanInputControl(Text label, boolean initial, String onTrue, String onFalse) implements InputControl
{
    public static final MapCodec<BooleanInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("label").forGetter(BooleanInputControl::label), (App)Codec.BOOL.optionalFieldOf("initial", (Object)false).forGetter(BooleanInputControl::initial), (App)Codec.STRING.optionalFieldOf("on_true", (Object)"true").forGetter(BooleanInputControl::onTrue), (App)Codec.STRING.optionalFieldOf("on_false", (Object)"false").forGetter(BooleanInputControl::onFalse)).apply((Applicative)instance, BooleanInputControl::new));

    public MapCodec<BooleanInputControl> getCodec() {
        return CODEC;
    }
}

