/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.dialog;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.action.DialogAction;

public record DialogActionButtonData(DialogButtonData data, Optional<DialogAction> action) {
    public static final Codec<DialogActionButtonData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DialogButtonData.CODEC.forGetter(DialogActionButtonData::data), (App)DialogAction.CODEC.optionalFieldOf("action").forGetter(DialogActionButtonData::action)).apply((Applicative)instance, DialogActionButtonData::new));

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{DialogActionButtonData.class, "button;action", "data", "action"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DialogActionButtonData.class, "button;action", "data", "action"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DialogActionButtonData.class, "button;action", "data", "action"}, this, object);
    }
}

