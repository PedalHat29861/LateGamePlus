/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.datafixer.fix.ComponentFix;
import org.jspecify.annotations.Nullable;

public class InvalidLockComponentPredicateFix
extends ComponentFix {
    private static final Optional<String> DOUBLE_QUOTES = Optional.of("\"\"");

    public InvalidLockComponentPredicateFix(Schema outputSchema) {
        super(outputSchema, "InvalidLockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    protected <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return InvalidLockComponentPredicateFix.validateLock(dynamic);
    }

    public static <T> @Nullable Dynamic<T> validateLock(Dynamic<T> dynamic) {
        return InvalidLockComponentPredicateFix.isLockInvalid(dynamic) ? null : dynamic;
    }

    private static <T> boolean isLockInvalid(Dynamic<T> dynamic) {
        return InvalidLockComponentPredicateFix.hasMatchingKey(dynamic, "components", componentsDynamic -> InvalidLockComponentPredicateFix.hasMatchingKey(componentsDynamic, "minecraft:custom_name", customNameDynamic -> customNameDynamic.asString().result().equals(DOUBLE_QUOTES)));
    }

    private static <T> boolean hasMatchingKey(Dynamic<T> dynamic, String key, Predicate<Dynamic<T>> predicate) {
        Optional optional = dynamic.getMapValues().result();
        if (optional.isEmpty() || ((Map)optional.get()).size() != 1) {
            return false;
        }
        return dynamic.get(key).result().filter(predicate).isPresent();
    }
}

