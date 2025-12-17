/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import net.minecraft.datafixer.fix.BlockPropertyRenameFix;

public class JigsawRotationFix
extends BlockPropertyRenameFix {
    private static final Map<String, String> ORIENTATION_UPDATES = ImmutableMap.builder().put((Object)"down", (Object)"down_south").put((Object)"up", (Object)"up_north").put((Object)"north", (Object)"north_up").put((Object)"south", (Object)"south_up").put((Object)"west", (Object)"west_up").put((Object)"east", (Object)"east_up").build();

    public JigsawRotationFix(Schema outputSchema) {
        super(outputSchema, "jigsaw_rotation_fix");
    }

    @Override
    protected boolean shouldFix(String id) {
        return id.equals("minecraft:jigsaw");
    }

    @Override
    protected <T> Dynamic<T> fix(String id, Dynamic<T> properties) {
        String string = properties.get("facing").asString("north");
        return properties.remove("facing").set("orientation", properties.createString(ORIENTATION_UPDATES.getOrDefault(string, string)));
    }
}

