/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog.action;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.action.ParsedTemplate;
import net.minecraft.text.ClickEvent;

public record DynamicRunCommandDialogAction(ParsedTemplate template) implements DialogAction
{
    public static final MapCodec<DynamicRunCommandDialogAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ParsedTemplate.CODEC.fieldOf("template").forGetter(DynamicRunCommandDialogAction::template)).apply((Applicative)instance, DynamicRunCommandDialogAction::new));

    public MapCodec<DynamicRunCommandDialogAction> getCodec() {
        return CODEC;
    }

    @Override
    public Optional<ClickEvent> createClickEvent(Map<String, DialogAction.ValueGetter> valueGetters) {
        String string = this.template.apply(DialogAction.ValueGetter.resolveAll(valueGetters));
        return Optional.of(new ClickEvent.RunCommand(string));
    }
}

