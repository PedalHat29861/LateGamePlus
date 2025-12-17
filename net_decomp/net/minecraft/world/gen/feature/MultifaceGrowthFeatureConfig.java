/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeatureConfig;

public class MultifaceGrowthFeatureConfig
implements FeatureConfig {
    public static final Codec<MultifaceGrowthFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Registries.BLOCK.getCodec().fieldOf("block").flatXmap(MultifaceGrowthFeatureConfig::validateBlock, DataResult::success).orElse((Object)((MultifaceGrowthBlock)Blocks.GLOW_LICHEN)).forGetter(config -> config.block), (App)Codec.intRange((int)1, (int)64).fieldOf("search_range").orElse((Object)10).forGetter(config -> config.searchRange), (App)Codec.BOOL.fieldOf("can_place_on_floor").orElse((Object)false).forGetter(config -> config.placeOnFloor), (App)Codec.BOOL.fieldOf("can_place_on_ceiling").orElse((Object)false).forGetter(config -> config.placeOnCeiling), (App)Codec.BOOL.fieldOf("can_place_on_wall").orElse((Object)false).forGetter(config -> config.placeOnWalls), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_spreading").orElse((Object)Float.valueOf(0.5f)).forGetter(config -> Float.valueOf(config.spreadChance)), (App)RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("can_be_placed_on").forGetter(config -> config.canPlaceOn)).apply((Applicative)instance, MultifaceGrowthFeatureConfig::new));
    public final MultifaceGrowthBlock block;
    public final int searchRange;
    public final boolean placeOnFloor;
    public final boolean placeOnCeiling;
    public final boolean placeOnWalls;
    public final float spreadChance;
    public final RegistryEntryList<Block> canPlaceOn;
    private final ObjectArrayList<Direction> directions;

    private static DataResult<MultifaceGrowthBlock> validateBlock(Block block) {
        DataResult dataResult;
        if (block instanceof MultifaceGrowthBlock) {
            MultifaceGrowthBlock multifaceGrowthBlock = (MultifaceGrowthBlock)block;
            dataResult = DataResult.success((Object)multifaceGrowthBlock);
        } else {
            dataResult = DataResult.error(() -> "Growth block should be a multiface spreadeable block");
        }
        return dataResult;
    }

    public MultifaceGrowthFeatureConfig(MultifaceGrowthBlock block, int searchRange, boolean placeOnFloor, boolean placeOnCeiling, boolean placeOnWalls, float spreadChance, RegistryEntryList<Block> canPlaceOn) {
        this.block = block;
        this.searchRange = searchRange;
        this.placeOnFloor = placeOnFloor;
        this.placeOnCeiling = placeOnCeiling;
        this.placeOnWalls = placeOnWalls;
        this.spreadChance = spreadChance;
        this.canPlaceOn = canPlaceOn;
        this.directions = new ObjectArrayList(6);
        if (placeOnCeiling) {
            this.directions.add((Object)Direction.UP);
        }
        if (placeOnFloor) {
            this.directions.add((Object)Direction.DOWN);
        }
        if (placeOnWalls) {
            Direction.Type.HORIZONTAL.forEach(arg_0 -> this.directions.add(arg_0));
        }
    }

    public List<Direction> shuffleDirections(Random random, Direction excluded) {
        return Util.copyShuffled(this.directions.stream().filter(direction -> direction != excluded), random);
    }

    public List<Direction> shuffleDirections(Random random) {
        return Util.copyShuffled(this.directions, random);
    }
}

