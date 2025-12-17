/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

public record TextInputControl(int width, Text label, boolean labelVisible, String initial, int maxLength, Optional<Multiline> multiline) implements InputControl
{
    public static final MapCodec<TextInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)200).forGetter(TextInputControl::width), (App)TextCodecs.CODEC.fieldOf("label").forGetter(TextInputControl::label), (App)Codec.BOOL.optionalFieldOf("label_visible", (Object)true).forGetter(TextInputControl::labelVisible), (App)Codec.STRING.optionalFieldOf("initial", (Object)"").forGetter(TextInputControl::initial), (App)Codecs.POSITIVE_INT.optionalFieldOf("max_length", (Object)32).forGetter(TextInputControl::maxLength), (App)Multiline.CODEC.optionalFieldOf("multiline").forGetter(TextInputControl::multiline)).apply((Applicative)instance, TextInputControl::new)).validate(inputControl -> {
        if (inputControl.initial.length() > inputControl.maxLength()) {
            return DataResult.error(() -> "Default text length exceeds allowed size");
        }
        return DataResult.success((Object)inputControl);
    });

    public MapCodec<TextInputControl> getCodec() {
        return CODEC;
    }

    public record Multiline(Optional<Integer> maxLines, Optional<Integer> height) {
        public static final int MAX_HEIGHT = 512;
        public static final Codec<Multiline> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.POSITIVE_INT.optionalFieldOf("max_lines").forGetter(Multiline::maxLines), (App)Codecs.rangedInt(1, 512).optionalFieldOf("height").forGetter(Multiline::height)).apply((Applicative)instance, Multiline::new));
    }
}

