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
import net.minecraft.datafixer.fix.ComponentFix;
import org.jspecify.annotations.Nullable;

public class TridentAnimationFix
extends ComponentFix {
    public TridentAnimationFix(Schema schema) {
        super(schema, "TridentAnimationFix", "minecraft:consumable");
    }

    @Override
    protected <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return dynamic.update("animation", value -> {
            String string = value.asString().result().orElse("");
            if ("spear".equals(string)) {
                return value.createString("trident");
            }
            return value;
        });
    }
}

