/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.dialog.body;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.util.dynamic.Codecs;

public interface DialogBody {
    public static final Codec<DialogBody> CODEC = Registries.DIALOG_BODY_TYPE.getCodec().dispatch(DialogBody::getTypeCodec, mapCodec -> mapCodec);
    public static final Codec<List<DialogBody>> LIST_CODEC = Codecs.listOrSingle(CODEC);

    public MapCodec<? extends DialogBody> getTypeCodec();
}

