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
import java.util.Optional;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.type.ColumnsDialog;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.dynamic.Codecs;

public record DialogListDialog(DialogCommonData common, RegistryEntryList<Dialog> dialogs, Optional<DialogActionButtonData> exitAction, int columns, int buttonWidth) implements ColumnsDialog
{
    public static final MapCodec<DialogListDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DialogCommonData.CODEC.forGetter(DialogListDialog::common), (App)Dialog.ENTRY_LIST_CODEC.fieldOf("dialogs").forGetter(DialogListDialog::dialogs), (App)DialogActionButtonData.CODEC.optionalFieldOf("exit_action").forGetter(DialogListDialog::exitAction), (App)Codecs.POSITIVE_INT.optionalFieldOf("columns", (Object)2).forGetter(DialogListDialog::columns), (App)WIDTH_CODEC.optionalFieldOf("button_width", (Object)150).forGetter(DialogListDialog::buttonWidth)).apply((Applicative)instance, DialogListDialog::new));

    public MapCodec<DialogListDialog> getCodec() {
        return CODEC;
    }
}

