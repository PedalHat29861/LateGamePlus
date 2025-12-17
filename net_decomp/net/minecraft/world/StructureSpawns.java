/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.biome.SpawnSettings;

public record StructureSpawns(BoundingBox boundingBox, Pool<SpawnSettings.SpawnEntry> spawns) {
    public static final Codec<StructureSpawns> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BoundingBox.CODEC.fieldOf("bounding_box").forGetter(StructureSpawns::boundingBox), (App)Pool.createCodec(SpawnSettings.SpawnEntry.CODEC).fieldOf("spawns").forGetter(StructureSpawns::spawns)).apply((Applicative)instance, StructureSpawns::new));

    public static final class BoundingBox
    extends Enum<BoundingBox>
    implements StringIdentifiable {
        public static final /* enum */ BoundingBox PIECE = new BoundingBox("piece");
        public static final /* enum */ BoundingBox STRUCTURE = new BoundingBox("full");
        public static final Codec<BoundingBox> CODEC;
        private final String name;
        private static final /* synthetic */ BoundingBox[] field_37204;

        public static BoundingBox[] values() {
            return (BoundingBox[])field_37204.clone();
        }

        public static BoundingBox valueOf(String string) {
            return Enum.valueOf(BoundingBox.class, string);
        }

        private BoundingBox(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ BoundingBox[] method_41152() {
            return new BoundingBox[]{PIECE, STRUCTURE};
        }

        static {
            field_37204 = BoundingBox.method_41152();
            CODEC = StringIdentifiable.createCodec(BoundingBox::values);
        }
    }
}

