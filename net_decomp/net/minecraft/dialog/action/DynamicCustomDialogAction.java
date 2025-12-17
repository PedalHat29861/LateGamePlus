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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.Identifier;

public record DynamicCustomDialogAction(Identifier id, Optional<NbtCompound> additions) implements DialogAction
{
    public static final MapCodec<DynamicCustomDialogAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("id").forGetter(DynamicCustomDialogAction::id), (App)NbtCompound.CODEC.optionalFieldOf("additions").forGetter(DynamicCustomDialogAction::additions)).apply((Applicative)instance, DynamicCustomDialogAction::new));

    public MapCodec<DynamicCustomDialogAction> getCodec() {
        return CODEC;
    }

    @Override
    public Optional<ClickEvent> createClickEvent(Map<String, DialogAction.ValueGetter> valueGetters) {
        NbtCompound nbtCompound = this.additions.map(NbtCompound::copy).orElseGet(NbtCompound::new);
        valueGetters.forEach((string, valueGetter) -> nbtCompound.put((String)string, valueGetter.getAsNbt()));
        return Optional.of(new ClickEvent.Custom(this.id, Optional.of(nbtCompound)));
    }
}

