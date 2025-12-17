/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.structure;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.FeatureConfig;

@FunctionalInterface
public interface StructurePiecesGenerator<C extends FeatureConfig> {
    public void generatePieces(StructurePiecesCollector var1, Context<C> var2);

    public record Context<C extends FeatureConfig>(C config, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ChunkPos chunkPos, HeightLimitView world, ChunkRandom random, long seed) {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Context.class, "config;chunkGenerator;structureTemplateManager;chunkPos;heightAccessor;random;seed", "config", "chunkGenerator", "structureTemplateManager", "chunkPos", "world", "random", "seed"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Context.class, "config;chunkGenerator;structureTemplateManager;chunkPos;heightAccessor;random;seed", "config", "chunkGenerator", "structureTemplateManager", "chunkPos", "world", "random", "seed"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Context.class, "config;chunkGenerator;structureTemplateManager;chunkPos;heightAccessor;random;seed", "config", "chunkGenerator", "structureTemplateManager", "chunkPos", "world", "random", "seed"}, this, object);
        }
    }
}

