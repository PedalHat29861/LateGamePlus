/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record DialogButtonData(Text label, Optional<Text> tooltip, int width) {
    public static final int DEFAULT_WIDTH = 150;
    public static final MapCodec<DialogButtonData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("label").forGetter(DialogButtonData::label), (App)TextCodecs.CODEC.optionalFieldOf("tooltip").forGetter(DialogButtonData::tooltip), (App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)150).forGetter(DialogButtonData::width)).apply((Applicative)instance, DialogButtonData::new));

    public DialogButtonData(Text label, int width) {
        this(label, Optional.empty(), width);
    }
}

