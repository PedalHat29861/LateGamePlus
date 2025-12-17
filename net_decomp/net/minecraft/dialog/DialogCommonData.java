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
package net.minecraft.dialog;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.type.DialogInput;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record DialogCommonData(Text title, Optional<Text> externalTitle, boolean canCloseWithEscape, boolean pause, AfterAction afterAction, List<DialogBody> body, List<DialogInput> inputs) {
    public static final MapCodec<DialogCommonData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("title").forGetter(DialogCommonData::title), (App)TextCodecs.CODEC.optionalFieldOf("external_title").forGetter(DialogCommonData::externalTitle), (App)Codec.BOOL.optionalFieldOf("can_close_with_escape", (Object)true).forGetter(DialogCommonData::canCloseWithEscape), (App)Codec.BOOL.optionalFieldOf("pause", (Object)true).forGetter(DialogCommonData::pause), (App)AfterAction.CODEC.optionalFieldOf("after_action", AfterAction.CLOSE).forGetter(DialogCommonData::afterAction), (App)DialogBody.LIST_CODEC.optionalFieldOf("body", List.of()).forGetter(DialogCommonData::body), (App)DialogInput.CODEC.listOf().optionalFieldOf("inputs", List.of()).forGetter(DialogCommonData::inputs)).apply((Applicative)instance, DialogCommonData::new)).validate(data -> {
        if (data.pause && !data.afterAction.canUnpause()) {
            return DataResult.error(() -> "Dialogs that pause the game must use after_action values that unpause it after user action!");
        }
        return DataResult.success((Object)data);
    });

    public Text getExternalTitle() {
        return this.externalTitle.orElse(this.title);
    }
}

