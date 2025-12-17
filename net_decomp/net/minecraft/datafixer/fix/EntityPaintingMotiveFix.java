/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class EntityPaintingMotiveFix
extends ChoiceFix {
    private static final Map<String, String> RENAMED_MOTIVES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
        map.put("donkeykong", "donkey_kong");
        map.put("burningskull", "burning_skull");
        map.put("skullandroses", "skull_and_roses");
    });

    public EntityPaintingMotiveFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityPaintingMotiveFix", TypeReferences.ENTITY, "minecraft:painting");
    }

    public Dynamic<?> renameMotive(Dynamic<?> painting) {
        Optional optional = painting.get("Motive").asString().result();
        if (optional.isPresent()) {
            String string = ((String)optional.get()).toLowerCase(Locale.ROOT);
            return painting.set("Motive", painting.createString(IdentifierNormalizingSchema.normalize(RENAMED_MOTIVES.getOrDefault(string, string))));
        }
        return painting;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::renameMotive);
    }
}

