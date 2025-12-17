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
package net.minecraft.dialog.body;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record PlainMessageDialogBody(Text contents, int width) implements DialogBody
{
    public static final int DEFAULT_WIDTH = 200;
    public static final MapCodec<PlainMessageDialogBody> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("contents").forGetter(PlainMessageDialogBody::contents), (App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)200).forGetter(PlainMessageDialogBody::width)).apply((Applicative)instance, PlainMessageDialogBody::new));
    public static final Codec<PlainMessageDialogBody> ALTERNATIVE_CODEC = Codec.withAlternative((Codec)CODEC.codec(), TextCodecs.CODEC, contents -> new PlainMessageDialogBody((Text)contents, 200));

    public MapCodec<PlainMessageDialogBody> getTypeCodec() {
        return CODEC;
    }
}

