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
package net.minecraft.dialog.input;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Codecs;

public record SingleOptionInputControl(int width, List<Entry> entries, Text label, boolean labelVisible) implements InputControl
{
    public static final MapCodec<SingleOptionInputControl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Dialog.WIDTH_CODEC.optionalFieldOf("width", (Object)200).forGetter(SingleOptionInputControl::width), (App)Codecs.nonEmptyList(Entry.CODEC.listOf()).fieldOf("options").forGetter(SingleOptionInputControl::entries), (App)TextCodecs.CODEC.fieldOf("label").forGetter(SingleOptionInputControl::label), (App)Codec.BOOL.optionalFieldOf("label_visible", (Object)true).forGetter(SingleOptionInputControl::labelVisible)).apply((Applicative)instance, SingleOptionInputControl::new)).validate(inputControl -> {
        long l = inputControl.entries.stream().filter(Entry::initial).count();
        if (l > 1L) {
            return DataResult.error(() -> "Multiple initial values");
        }
        return DataResult.success((Object)inputControl);
    });

    public MapCodec<SingleOptionInputControl> getCodec() {
        return CODEC;
    }

    public Optional<Entry> getInitialEntry() {
        return this.entries.stream().filter(Entry::initial).findFirst();
    }

    public record Entry(String id, Optional<Text> display, boolean initial) {
        public static final Codec<Entry> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("id").forGetter(Entry::id), (App)TextCodecs.CODEC.optionalFieldOf("display").forGetter(Entry::display), (App)Codec.BOOL.optionalFieldOf("initial", (Object)false).forGetter(Entry::initial)).apply((Applicative)instance, Entry::new));
        public static final Codec<Entry> CODEC = Codec.withAlternative(BASE_CODEC, (Codec)Codec.STRING, id -> new Entry((String)id, Optional.empty(), false));

        public Text getDisplay() {
            return this.display.orElseGet(() -> Text.literal(this.id));
        }
    }
}

