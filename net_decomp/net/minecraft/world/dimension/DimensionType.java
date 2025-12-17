/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.dimension;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import net.minecraft.block.Block;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.timeline.Timeline;

public record DimensionType(boolean hasFixedTime, boolean hasSkyLight, boolean hasCeiling, double coordinateScale, int minY, int height, int logicalHeight, TagKey<Block> infiniburn, float ambientLight, MonsterSettings monsterSettings, Skybox skybox, CardinalLightType cardinalLightType, EnvironmentAttributeMap attributes, RegistryEntryList<Timeline> timelines) {
    public static final int SIZE_BITS_Y = BlockPos.SIZE_BITS_Y;
    public static final int field_33411 = 16;
    public static final int MAX_HEIGHT = (1 << SIZE_BITS_Y) - 32;
    public static final int MAX_COLUMN_HEIGHT = (MAX_HEIGHT >> 1) - 1;
    public static final int MIN_HEIGHT = MAX_COLUMN_HEIGHT - MAX_HEIGHT + 1;
    public static final int field_35478 = MAX_COLUMN_HEIGHT << 4;
    public static final int field_35479 = MIN_HEIGHT << 4;
    public static final Codec<DimensionType> CODEC = DimensionType.createDimensionCodec(EnvironmentAttributeMap.CODEC);
    public static final Codec<DimensionType> NETWORK_CODEC = DimensionType.createDimensionCodec(EnvironmentAttributeMap.NETWORK_CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<DimensionType>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.DIMENSION_TYPE);
    public static final float[] MOON_SIZES = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    public static final Codec<RegistryEntry<DimensionType>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.DIMENSION_TYPE, CODEC);

    public DimensionType {
        if (height < 16) {
            throw new IllegalStateException("height has to be at least 16");
        }
        if (minY + height > MAX_COLUMN_HEIGHT + 1) {
            throw new IllegalStateException("min_y + height cannot be higher than: " + (MAX_COLUMN_HEIGHT + 1));
        }
        if (logicalHeight > height) {
            throw new IllegalStateException("logical_height cannot be higher than height");
        }
        if (height % 16 != 0) {
            throw new IllegalStateException("height has to be multiple of 16");
        }
        if (minY % 16 != 0) {
            throw new IllegalStateException("min_y has to be a multiple of 16");
        }
    }

    private static Codec<DimensionType> createDimensionCodec(Codec<EnvironmentAttributeMap> attributesCodec) {
        return Codecs.exceptionCatching(RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("has_fixed_time", (Object)false).forGetter(DimensionType::hasFixedTime), (App)Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight), (App)Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling), (App)Codec.doubleRange((double)1.0E-5f, (double)3.0E7).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale), (App)Codec.intRange((int)MIN_HEIGHT, (int)MAX_COLUMN_HEIGHT).fieldOf("min_y").forGetter(DimensionType::minY), (App)Codec.intRange((int)16, (int)MAX_HEIGHT).fieldOf("height").forGetter(DimensionType::height), (App)Codec.intRange((int)0, (int)MAX_HEIGHT).fieldOf("logical_height").forGetter(DimensionType::logicalHeight), (App)TagKey.codec(RegistryKeys.BLOCK).fieldOf("infiniburn").forGetter(DimensionType::infiniburn), (App)Codec.FLOAT.fieldOf("ambient_light").forGetter(DimensionType::ambientLight), (App)MonsterSettings.CODEC.forGetter(DimensionType::monsterSettings), (App)Skybox.CODEC.optionalFieldOf("skybox", (Object)Skybox.OVERWORLD).forGetter(DimensionType::skybox), (App)CardinalLightType.CODEC.optionalFieldOf("cardinal_light", (Object)CardinalLightType.DEFAULT).forGetter(DimensionType::cardinalLightType), (App)attributesCodec.optionalFieldOf("attributes", (Object)EnvironmentAttributeMap.EMPTY).forGetter(DimensionType::attributes), (App)RegistryCodecs.entryList(RegistryKeys.TIMELINE).optionalFieldOf("timelines", RegistryEntryList.empty()).forGetter(DimensionType::timelines)).apply((Applicative)instance, DimensionType::new)));
    }

    public static double getCoordinateScaleFactor(DimensionType fromDimension, DimensionType toDimension) {
        double d = fromDimension.coordinateScale();
        double e = toDimension.coordinateScale();
        return d / e;
    }

    public static Path getSaveDirectory(RegistryKey<World> worldRef, Path worldDirectory) {
        if (worldRef == World.OVERWORLD) {
            return worldDirectory;
        }
        if (worldRef == World.END) {
            return worldDirectory.resolve("DIM1");
        }
        if (worldRef == World.NETHER) {
            return worldDirectory.resolve("DIM-1");
        }
        return worldDirectory.resolve("dimensions").resolve(worldRef.getValue().getNamespace()).resolve(worldRef.getValue().getPath());
    }

    public IntProvider monsterSpawnLightTest() {
        return this.monsterSettings.monsterSpawnLightTest();
    }

    public int monsterSpawnBlockLightLimit() {
        return this.monsterSettings.monsterSpawnBlockLightLimit();
    }

    public boolean getSkybox() {
        return this.skybox == Skybox.END;
    }

    public record MonsterSettings(IntProvider monsterSpawnLightTest, int monsterSpawnBlockLightLimit) {
        public static final MapCodec<MonsterSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)IntProvider.createValidatingCodec(0, 15).fieldOf("monster_spawn_light_level").forGetter(MonsterSettings::monsterSpawnLightTest), (App)Codec.intRange((int)0, (int)15).fieldOf("monster_spawn_block_light_limit").forGetter(MonsterSettings::monsterSpawnBlockLightLimit)).apply((Applicative)instance, MonsterSettings::new));
    }

    public static final class Skybox
    extends Enum<Skybox>
    implements StringIdentifiable {
        public static final /* enum */ Skybox NONE = new Skybox("none");
        public static final /* enum */ Skybox OVERWORLD = new Skybox("overworld");
        public static final /* enum */ Skybox END = new Skybox("end");
        public static final Codec<Skybox> CODEC;
        private final String name;
        private static final /* synthetic */ Skybox[] field_64390;

        public static Skybox[] values() {
            return (Skybox[])field_64390.clone();
        }

        public static Skybox valueOf(String string) {
            return Enum.valueOf(Skybox.class, string);
        }

        private Skybox(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Skybox[] method_76469() {
            return new Skybox[]{NONE, OVERWORLD, END};
        }

        static {
            field_64390 = Skybox.method_76469();
            CODEC = StringIdentifiable.createCodec(Skybox::values);
        }
    }

    public static final class CardinalLightType
    extends Enum<CardinalLightType>
    implements StringIdentifiable {
        public static final /* enum */ CardinalLightType DEFAULT = new CardinalLightType("default");
        public static final /* enum */ CardinalLightType NETHER = new CardinalLightType("nether");
        public static final Codec<CardinalLightType> CODEC;
        private final String name;
        private static final /* synthetic */ CardinalLightType[] field_64384;

        public static CardinalLightType[] values() {
            return (CardinalLightType[])field_64384.clone();
        }

        public static CardinalLightType valueOf(String string) {
            return Enum.valueOf(CardinalLightType.class, string);
        }

        private CardinalLightType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ CardinalLightType[] method_76468() {
            return new CardinalLightType[]{DEFAULT, NETHER};
        }

        static {
            field_64384 = CardinalLightType.method_76468();
            CODEC = StringIdentifiable.createCodec(CardinalLightType::values);
        }
    }
}

