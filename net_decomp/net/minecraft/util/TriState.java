/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.StringIdentifiable;

public final class TriState
extends Enum<TriState>
implements StringIdentifiable {
    public static final /* enum */ TriState TRUE = new TriState("true");
    public static final /* enum */ TriState FALSE = new TriState("false");
    public static final /* enum */ TriState DEFAULT = new TriState("default");
    public static final Codec<TriState> CODEC;
    private final String name;
    private static final /* synthetic */ TriState[] field_52397;

    public static TriState[] values() {
        return (TriState[])field_52397.clone();
    }

    public static TriState valueOf(String string) {
        return Enum.valueOf(TriState.class, string);
    }

    private TriState(String name) {
        this.name = name;
    }

    public static TriState ofBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean asBoolean(boolean fallback) {
        return switch (this.ordinal()) {
            case 0 -> true;
            case 1 -> false;
            default -> fallback;
        };
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ TriState[] method_61347() {
        return new TriState[]{TRUE, FALSE, DEFAULT};
    }

    static {
        field_52397 = TriState.method_61347();
        CODEC = Codec.either((Codec)Codec.BOOL, StringIdentifiable.createCodec(TriState::values)).xmap(either -> (TriState)either.map(TriState::ofBoolean, Function.identity()), triState -> switch (triState.ordinal()) {
            default -> throw new MatchException(null, null);
            case 2 -> Either.right((Object)triState);
            case 0 -> Either.left((Object)true);
            case 1 -> Either.left((Object)false);
        });
    }
}

