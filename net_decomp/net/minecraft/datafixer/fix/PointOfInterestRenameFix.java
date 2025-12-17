/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.datafixer.fix.PointOfInterestFix;

public class PointOfInterestRenameFix
extends PointOfInterestFix {
    private final Function<String, String> renamer;

    public PointOfInterestRenameFix(Schema outputSchema, String name, Function<String, String> renamer) {
        super(outputSchema, name);
        this.renamer = renamer;
    }

    @Override
    protected <T> Stream<Dynamic<T>> update(Stream<Dynamic<T>> dynamics) {
        return dynamics.map(dynamic2 -> dynamic2.update("type", dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.asString().map(this.renamer).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)).result(), (Object)dynamic)));
    }
}

