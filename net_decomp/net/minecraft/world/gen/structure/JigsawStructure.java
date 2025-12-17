/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.structure;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public final class JigsawStructure
extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING = DimensionPadding.NONE;
    public static final StructureLiquidSettings DEFAULT_LIQUID_SETTINGS = StructureLiquidSettings.APPLY_WATERLOGGING;
    public static final int MAX_SIZE = 128;
    public static final int field_49155 = 0;
    public static final int MAX_GENERATION_DEPTH = 20;
    public static final MapCodec<JigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(JigsawStructure.configCodecBuilder(instance), (App)StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool), (App)Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName), (App)Codec.intRange((int)0, (int)20).fieldOf("size").forGetter(structure -> structure.size), (App)HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight), (App)Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack), (App)Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap), (App)MaxDistanceFromCenter.CODEC.fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter), (App)Codec.list(StructurePoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(structure -> structure.poolAliasBindings), (App)DimensionPadding.CODEC.optionalFieldOf("dimension_padding", (Object)DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding), (App)StructureLiquidSettings.codec.optionalFieldOf("liquid_settings", (Object)DEFAULT_LIQUID_SETTINGS).forGetter(jigsawStructure -> jigsawStructure.liquidSettings)).apply((Applicative)instance, JigsawStructure::new)).validate(JigsawStructure::validate);
    private final RegistryEntry<StructurePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final MaxDistanceFromCenter maxDistanceFromCenter;
    private final List<StructurePoolAliasBinding> poolAliasBindings;
    private final DimensionPadding dimensionPadding;
    private final StructureLiquidSettings liquidSettings;

    private static DataResult<JigsawStructure> validate(JigsawStructure structure) {
        int i;
        switch (structure.getTerrainAdaptation()) {
            default: {
                throw new MatchException(null, null);
            }
            case NONE: {
                int n = 0;
                break;
            }
            case BURY: 
            case BEARD_THIN: 
            case BEARD_BOX: 
            case ENCAPSULATE: {
                int n = i = 12;
            }
        }
        if (structure.maxDistanceFromCenter.horizontal() + i > 128) {
            return DataResult.error(() -> "Horizontal structure size including terrain adaptation must not exceed 128");
        }
        return DataResult.success((Object)structure);
    }

    public JigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, Optional<Identifier> startJigsawName, int size, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, MaxDistanceFromCenter maxDistanceFromCenter, List<StructurePoolAliasBinding> poolAliasBindings, DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliasBindings = poolAliasBindings;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    public JigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, int size, HeightProvider startHeight, boolean useExpansionHack, Heightmap.Type projectStartToHeightmap) {
        this(config, startPool, Optional.empty(), size, startHeight, useExpansionHack, Optional.of(projectStartToHeightmap), new MaxDistanceFromCenter(80), List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    public JigsawStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, int size, HeightProvider startHeight, boolean useExpansionHack) {
        this(config, startPool, Optional.empty(), size, startHeight, useExpansionHack, Optional.empty(), new MaxDistanceFromCenter(80), List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int i = this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));
        BlockPos blockPos = new BlockPos(chunkPos.getStartX(), i, chunkPos.getStartZ());
        return StructurePoolBasedGenerator.generate(context, this.startPool, this.startJigsawName, this.size, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter, StructurePoolAliasLookup.create(this.poolAliasBindings, blockPos, context.seed()), this.dimensionPadding, this.liquidSettings);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.JIGSAW;
    }

    @VisibleForTesting
    public RegistryEntry<StructurePool> getStartPool() {
        return this.startPool;
    }

    @VisibleForTesting
    public List<StructurePoolAliasBinding> getPoolAliasBindings() {
        return this.poolAliasBindings;
    }

    public record MaxDistanceFromCenter(int horizontal, int vertical) {
        private static final Codec<Integer> DISTANCE_CODEC = Codec.intRange((int)1, (int)128);
        private static final Codec<MaxDistanceFromCenter> EXPANDED_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DISTANCE_CODEC.fieldOf("horizontal").forGetter(MaxDistanceFromCenter::horizontal), (App)Codecs.rangedInt(1, DimensionType.MAX_HEIGHT).optionalFieldOf("vertical", (Object)DimensionType.MAX_HEIGHT).forGetter(MaxDistanceFromCenter::vertical)).apply((Applicative)instance, MaxDistanceFromCenter::new));
        public static final Codec<MaxDistanceFromCenter> CODEC = Codec.either(EXPANDED_CODEC, DISTANCE_CODEC).xmap(either -> (MaxDistanceFromCenter)either.map(Function.identity(), MaxDistanceFromCenter::new), distance -> distance.horizontal == distance.vertical ? Either.right((Object)distance.horizontal) : Either.left((Object)distance));

        public MaxDistanceFromCenter(int distance) {
            this(distance, distance);
        }
    }
}

