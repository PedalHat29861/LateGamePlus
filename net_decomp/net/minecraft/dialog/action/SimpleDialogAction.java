/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.dialog.action;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.text.ClickEvent;
import net.minecraft.util.Util;

public record SimpleDialogAction(ClickEvent value) implements DialogAction
{
    public static final Map<ClickEvent.Action, MapCodec<SimpleDialogAction>> CODECS = Util.make(() -> {
        EnumMap<ClickEvent.Action, MapCodec> map = new EnumMap<ClickEvent.Action, MapCodec>(ClickEvent.Action.class);
        for (ClickEvent.Action action : (ClickEvent.Action[])ClickEvent.Action.class.getEnumConstants()) {
            if (!action.isUserDefinable()) continue;
            MapCodec<? extends ClickEvent> mapCodec = action.getCodec();
            map.put(action, mapCodec.xmap(SimpleDialogAction::new, SimpleDialogAction::value));
        }
        return Collections.unmodifiableMap(map);
    });

    public MapCodec<SimpleDialogAction> getCodec() {
        return CODECS.get(this.value.getAction());
    }

    @Override
    public Optional<ClickEvent> createClickEvent(Map<String, DialogAction.ValueGetter> valueGetters) {
        return Optional.of(this.value);
    }
}

