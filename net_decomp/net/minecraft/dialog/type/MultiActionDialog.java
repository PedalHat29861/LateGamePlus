/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.type.ColumnsDialog;
import net.minecraft.util.dynamic.Codecs;

public record MultiActionDialog(DialogCommonData common, List<DialogActionButtonData> actions, Optional<DialogActionButtonData> exitAction, int columns) implements ColumnsDialog
{
    public static final MapCodec<MultiActionDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DialogCommonData.CODEC.forGetter(MultiActionDialog::common), (App)Codecs.nonEmptyList(DialogActionButtonData.CODEC.listOf()).fieldOf("actions").forGetter(MultiActionDialog::actions), (App)DialogActionButtonData.CODEC.optionalFieldOf("exit_action").forGetter(MultiActionDialog::exitAction), (App)Codecs.POSITIVE_INT.optionalFieldOf("columns", (Object)2).forGetter(MultiActionDialog::columns)).apply((Applicative)instance, MultiActionDialog::new));

    public MapCodec<MultiActionDialog> getCodec() {
        return CODEC;
    }
}

