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
import java.util.Optional;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;

public record ItemDialogBody(ItemStack item, Optional<PlainMessageDialogBody> description, boolean showDecorations, boolean showTooltip, int width, int height) implements DialogBody
{
    public static final MapCodec<ItemDialogBody> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ItemStack.VALIDATED_CODEC.fieldOf("item").forGetter(ItemDialogBody::item), (App)PlainMessageDialogBody.ALTERNATIVE_CODEC.optionalFieldOf("description").forGetter(ItemDialogBody::description), (App)Codec.BOOL.optionalFieldOf("show_decorations", (Object)true).forGetter(ItemDialogBody::showDecorations), (App)Codec.BOOL.optionalFieldOf("show_tooltip", (Object)true).forGetter(ItemDialogBody::showTooltip), (App)Codecs.rangedInt(1, 256).optionalFieldOf("width", (Object)16).forGetter(ItemDialogBody::width), (App)Codecs.rangedInt(1, 256).optionalFieldOf("height", (Object)16).forGetter(ItemDialogBody::height)).apply((Applicative)instance, ItemDialogBody::new));

    public MapCodec<ItemDialogBody> getTypeCodec() {
        return CODEC;
    }
}

