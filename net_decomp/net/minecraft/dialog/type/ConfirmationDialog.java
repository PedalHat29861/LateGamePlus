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
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.type.SimpleDialog;

public record ConfirmationDialog(DialogCommonData common, DialogActionButtonData yesButton, DialogActionButtonData noButton) implements SimpleDialog
{
    public static final MapCodec<ConfirmationDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DialogCommonData.CODEC.forGetter(ConfirmationDialog::common), (App)DialogActionButtonData.CODEC.fieldOf("yes").forGetter(ConfirmationDialog::yesButton), (App)DialogActionButtonData.CODEC.fieldOf("no").forGetter(ConfirmationDialog::noButton)).apply((Applicative)instance, ConfirmationDialog::new));

    public MapCodec<ConfirmationDialog> getCodec() {
        return CODEC;
    }

    @Override
    public Optional<DialogAction> getCancelAction() {
        return this.noButton.action();
    }

    @Override
    public List<DialogActionButtonData> getButtons() {
        return List.of(this.yesButton, this.noButton);
    }
}

