/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class MemoryExpiryDataFix
extends ChoiceFix {
    public MemoryExpiryDataFix(Schema outputSchema, String choiceName) {
        super(outputSchema, false, "Memory expiry data fix (" + choiceName + ")", TypeReferences.ENTITY, choiceName);
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::updateBrain);
    }

    public Dynamic<?> updateBrain(Dynamic<?> entityDynamic) {
        return entityDynamic.update("Brain", this::updateMemories);
    }

    private Dynamic<?> updateMemories(Dynamic<?> brainDynamic) {
        return brainDynamic.update("memories", this::updateMemoryMap);
    }

    private Dynamic<?> updateMemoryMap(Dynamic<?> memoriesDynamic) {
        return memoriesDynamic.updateMapValues(this::updateMemoryMapValues);
    }

    private Pair<Dynamic<?>, Dynamic<?>> updateMemoryMapValues(Pair<Dynamic<?>, Dynamic<?>> memoryKv) {
        return memoryKv.mapSecond(this::updateMemoryMapValueEntry);
    }

    private Dynamic<?> updateMemoryMapValueEntry(Dynamic<?> memoryValue) {
        return memoryValue.createMap((Map)ImmutableMap.of((Object)memoryValue.createString("value"), memoryValue));
    }
}

