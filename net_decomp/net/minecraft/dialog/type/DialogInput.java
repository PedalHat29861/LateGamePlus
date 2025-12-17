/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.dialog.action.ParsedTemplate;
import net.minecraft.dialog.input.InputControl;

public record DialogInput(String key, InputControl control) {
    public static final Codec<DialogInput> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ParsedTemplate.NAME_CODEC.fieldOf("key").forGetter(DialogInput::key), (App)InputControl.CODEC.forGetter(DialogInput::control)).apply((Applicative)instance, DialogInput::new));
}

