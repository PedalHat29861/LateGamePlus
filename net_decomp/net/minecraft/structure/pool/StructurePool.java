/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.structure.pool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.GravityStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import org.apache.commons.lang3.mutable.MutableObject;

public class StructurePool {
    private static final int DEFAULT_Y = Integer.MIN_VALUE;
    private static final MutableObject<Codec<RegistryEntry<StructurePool>>> FALLBACK = new MutableObject();
    public static final Codec<StructurePool> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.lazyInitialized(FALLBACK).fieldOf("fallback").forGetter(StructurePool::getFallback), (App)Codec.mapPair((MapCodec)StructurePoolElement.CODEC.fieldOf("element"), (MapCodec)Codec.intRange((int)1, (int)150).fieldOf("weight")).codec().listOf().fieldOf("elements").forGetter(pool -> pool.elementWeights)).apply((Applicative)instance, StructurePool::new));
    public static final Codec<RegistryEntry<StructurePool>> REGISTRY_CODEC = Util.make(RegistryElementCodec.of(RegistryKeys.TEMPLATE_POOL, CODEC), arg_0 -> FALLBACK.setValue(arg_0));
    private final List<Pair<StructurePoolElement, Integer>> elementWeights;
    private final ObjectArrayList<StructurePoolElement> elements;
    private final RegistryEntry<StructurePool> fallback;
    private int highestY = Integer.MIN_VALUE;

    public StructurePool(RegistryEntry<StructurePool> fallback, List<Pair<StructurePoolElement, Integer>> elementWeights) {
        this.elementWeights = elementWeights;
        this.elements = new ObjectArrayList();
        for (Pair<StructurePoolElement, Integer> pair : elementWeights) {
            StructurePoolElement structurePoolElement = (StructurePoolElement)pair.getFirst();
            for (int i = 0; i < (Integer)pair.getSecond(); ++i) {
                this.elements.add((Object)structurePoolElement);
            }
        }
        this.fallback = fallback;
    }

    public StructurePool(RegistryEntry<StructurePool> fallback, List<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> elementWeightsByGetters, Projection projection) {
        this.elementWeights = Lists.newArrayList();
        this.elements = new ObjectArrayList();
        for (Pair<Function<Projection, ? extends StructurePoolElement>, Integer> pair : elementWeightsByGetters) {
            StructurePoolElement structurePoolElement = (StructurePoolElement)((Function)pair.getFirst()).apply(projection);
            this.elementWeights.add((Pair<StructurePoolElement, Integer>)Pair.of((Object)structurePoolElement, (Object)((Integer)pair.getSecond())));
            for (int i = 0; i < (Integer)pair.getSecond(); ++i) {
                this.elements.add((Object)structurePoolElement);
            }
        }
        this.fallback = fallback;
    }

    public int getHighestY(StructureTemplateManager structureTemplateManager) {
        if (this.highestY == Integer.MIN_VALUE) {
            this.highestY = this.elements.stream().filter(element -> element != EmptyPoolElement.INSTANCE).mapToInt(element -> element.getBoundingBox(structureTemplateManager, BlockPos.ORIGIN, BlockRotation.NONE).getBlockCountY()).max().orElse(0);
        }
        return this.highestY;
    }

    @VisibleForTesting
    public List<Pair<StructurePoolElement, Integer>> getElementWeights() {
        return this.elementWeights;
    }

    public RegistryEntry<StructurePool> getFallback() {
        return this.fallback;
    }

    public StructurePoolElement getRandomElement(Random random) {
        if (this.elements.isEmpty()) {
            return EmptyPoolElement.INSTANCE;
        }
        return (StructurePoolElement)this.elements.get(random.nextInt(this.elements.size()));
    }

    public List<StructurePoolElement> getElementIndicesInRandomOrder(Random random) {
        return Util.copyShuffled(this.elements, random);
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public static final class Projection
    extends Enum<Projection>
    implements StringIdentifiable {
        public static final /* enum */ Projection TERRAIN_MATCHING = new Projection("terrain_matching", (ImmutableList<StructureProcessor>)ImmutableList.of((Object)new GravityStructureProcessor(Heightmap.Type.WORLD_SURFACE_WG, -1)));
        public static final /* enum */ Projection RIGID = new Projection("rigid", (ImmutableList<StructureProcessor>)ImmutableList.of());
        public static final StringIdentifiable.EnumCodec<Projection> CODEC;
        private final String id;
        private final ImmutableList<StructureProcessor> processors;
        private static final /* synthetic */ Projection[] field_16683;

        public static Projection[] values() {
            return (Projection[])field_16683.clone();
        }

        public static Projection valueOf(String string) {
            return Enum.valueOf(Projection.class, string);
        }

        private Projection(String id, ImmutableList<StructureProcessor> processors) {
            this.id = id;
            this.processors = processors;
        }

        public String getId() {
            return this.id;
        }

        public static Projection getById(String id) {
            return CODEC.byId(id);
        }

        public ImmutableList<StructureProcessor> getProcessors() {
            return this.processors;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Projection[] method_36758() {
            return new Projection[]{TERRAIN_MATCHING, RIGID};
        }

        static {
            field_16683 = Projection.method_36758();
            CODEC = StringIdentifiable.createCodec(Projection::values);
        }
    }
}

