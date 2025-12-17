/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;

public record MapDecorationsComponent(Map<String, Decoration> decorations) {
    public static final MapDecorationsComponent DEFAULT = new MapDecorationsComponent(Map.of());
    public static final Codec<MapDecorationsComponent> CODEC = Codec.unboundedMap((Codec)Codec.STRING, Decoration.CODEC).xmap(MapDecorationsComponent::new, MapDecorationsComponent::decorations);

    public MapDecorationsComponent with(String id, Decoration decoration) {
        return new MapDecorationsComponent(Util.mapWith(this.decorations, id, decoration));
    }

    public record Decoration(RegistryEntry<MapDecorationType> type, double x, double z, float rotation) {
        public static final Codec<Decoration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MapDecorationType.CODEC.fieldOf("type").forGetter(Decoration::type), (App)Codec.DOUBLE.fieldOf("x").forGetter(Decoration::x), (App)Codec.DOUBLE.fieldOf("z").forGetter(Decoration::z), (App)Codec.FLOAT.fieldOf("rotation").forGetter(Decoration::rotation)).apply((Applicative)instance, Decoration::new));
    }
}

