/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.RewriteResult
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.View
 *  com.mojang.datafixers.functions.PointFreeRule
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;

public class FixUtil {
    public static Dynamic<?> fixBlockPos(Dynamic<?> dynamic) {
        Optional optional = dynamic.get("X").asNumber().result();
        Optional optional2 = dynamic.get("Y").asNumber().result();
        Optional optional3 = dynamic.get("Z").asNumber().result();
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return dynamic;
        }
        return FixUtil.createBlockPos(dynamic, ((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue());
    }

    public static Dynamic<?> consolidateBlockPos(Dynamic<?> dynamic, String xKey, String yKey, String zKey, String newPosKey) {
        Optional optional = dynamic.get(xKey).asNumber().result();
        Optional optional2 = dynamic.get(yKey).asNumber().result();
        Optional optional3 = dynamic.get(zKey).asNumber().result();
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return dynamic;
        }
        return dynamic.remove(xKey).remove(yKey).remove(zKey).set(newPosKey, FixUtil.createBlockPos(dynamic, ((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue()));
    }

    public static Dynamic<?> createBlockPos(Dynamic<?> dynamic, int x, int y, int z) {
        return dynamic.createIntList(IntStream.of(x, y, z));
    }

    public static <T, R> Typed<R> withType(Type<R> type, Typed<T> typed) {
        return new Typed(type, typed.getOps(), typed.getValue());
    }

    public static <T> Typed<T> withType(Type<T> type, Object value, DynamicOps<?> ops) {
        return new Typed(type, ops, value);
    }

    public static Type<?> withTypeChanged(Type<?> type, Type<?> oldType, Type<?> newType) {
        return type.all(FixUtil.typeChangingRule(oldType, newType), true, false).view().newType();
    }

    private static <A, B> TypeRewriteRule typeChangingRule(Type<A> oldType, Type<B> newType) {
        RewriteResult rewriteResult = RewriteResult.create((View)View.create((String)"Patcher", oldType, newType, ops -> object -> {
            throw new UnsupportedOperationException();
        }), (BitSet)new BitSet());
        return TypeRewriteRule.everywhere((TypeRewriteRule)TypeRewriteRule.ifSame(oldType, (RewriteResult)rewriteResult), (PointFreeRule)PointFreeRule.nop(), (boolean)true, (boolean)true);
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> compose(Function<Typed<?>, Typed<?>> ... fixes) {
        return typed -> {
            for (Function function : fixes) {
                typed = (Typed)function.apply(typed);
            }
            return typed;
        };
    }

    public static Dynamic<?> createBlockState(String id, Map<String, String> properties) {
        Dynamic dynamic = new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)new NbtCompound());
        Dynamic dynamic2 = dynamic.set("Name", dynamic.createString(id));
        if (!properties.isEmpty()) {
            dynamic2 = dynamic2.set("Properties", dynamic.createMap(properties.entrySet().stream().collect(Collectors.toMap(entry -> dynamic.createString((String)entry.getKey()), entry -> dynamic.createString((String)entry.getValue())))));
        }
        return dynamic2;
    }

    public static Dynamic<?> createBlockState(String id) {
        return FixUtil.createBlockState(id, Map.of());
    }

    public static Dynamic<?> apply(Dynamic<?> dynamic, String fieldName, UnaryOperator<String> applier) {
        return dynamic.update(fieldName, value -> (Dynamic)DataFixUtils.orElse((Optional)value.asString().map((Function)applier).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)).result(), (Object)value));
    }

    public static String getColorName(int index) {
        return switch (index) {
            default -> "white";
            case 1 -> "orange";
            case 2 -> "magenta";
            case 3 -> "light_blue";
            case 4 -> "yellow";
            case 5 -> "lime";
            case 6 -> "pink";
            case 7 -> "gray";
            case 8 -> "light_gray";
            case 9 -> "cyan";
            case 10 -> "purple";
            case 11 -> "blue";
            case 12 -> "brown";
            case 13 -> "green";
            case 14 -> "red";
            case 15 -> "black";
        };
    }

    public static <T> Typed<?> method_67590(Typed<?> typed, OpticFinder<T> opticFinder, Dynamic<?> dynamic) {
        return typed.set(opticFinder, Util.readTyped(opticFinder.type(), dynamic, true));
    }
}

