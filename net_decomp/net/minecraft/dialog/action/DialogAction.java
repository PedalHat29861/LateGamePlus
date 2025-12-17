/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.dialog.action;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;

public interface DialogAction {
    public static final Codec<DialogAction> CODEC = Registries.DIALOG_ACTION_TYPE.getCodec().dispatch(DialogAction::getCodec, codec -> codec);

    public MapCodec<? extends DialogAction> getCodec();

    public Optional<ClickEvent> createClickEvent(Map<String, ValueGetter> var1);

    public static interface ValueGetter {
        public String get();

        public NbtElement getAsNbt();

        public static Map<String, String> resolveAll(Map<String, ValueGetter> valueGetters) {
            return Maps.transformValues(valueGetters, ValueGetter::get);
        }

        public static ValueGetter of(final String value) {
            return new ValueGetter(){

                @Override
                public String get() {
                    return value;
                }

                @Override
                public NbtElement getAsNbt() {
                    return NbtString.of(value);
                }
            };
        }

        public static ValueGetter of(final Supplier<String> valueSupplier) {
            return new ValueGetter(){

                @Override
                public String get() {
                    return (String)valueSupplier.get();
                }

                @Override
                public NbtElement getAsNbt() {
                    return NbtString.of((String)valueSupplier.get());
                }
            };
        }
    }
}

