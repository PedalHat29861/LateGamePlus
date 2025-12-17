/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.item.map;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

public record MapFrameMarker(BlockPos pos, int rotation, int entityId) {
    public static final Codec<MapFrameMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(MapFrameMarker::pos), (App)Codec.INT.fieldOf("rotation").forGetter(MapFrameMarker::rotation), (App)Codec.INT.fieldOf("entity_id").forGetter(MapFrameMarker::entityId)).apply((Applicative)instance, MapFrameMarker::new));

    public String getKey() {
        return MapFrameMarker.getKey(this.pos);
    }

    public static String getKey(BlockPos pos) {
        return "frame-" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}

