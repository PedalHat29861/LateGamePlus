/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.util.math.random;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSequence;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class RandomSequencesState
extends PersistentState {
    public static final Codec<RandomSequencesState> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("salt").forGetter(RandomSequencesState::getSalt), (App)Codec.BOOL.optionalFieldOf("include_world_seed", (Object)true).forGetter(RandomSequencesState::shouldIncludeWorldSeed), (App)Codec.BOOL.optionalFieldOf("include_sequence_id", (Object)true).forGetter(RandomSequencesState::shouldIncludeSequenceId), (App)Codec.unboundedMap(Identifier.CODEC, RandomSequence.CODEC).fieldOf("sequences").forGetter(randomSequencesState -> randomSequencesState.sequences)).apply((Applicative)instance, RandomSequencesState::new));
    public static final PersistentStateType<RandomSequencesState> STATE_TYPE = new PersistentStateType<RandomSequencesState>("random_sequences", RandomSequencesState::new, CODEC, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<Identifier, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public RandomSequencesState() {
    }

    private RandomSequencesState(int salt, boolean includeWorldSeed, boolean includeSequenceId, Map<Identifier, RandomSequence> sequences) {
        this.salt = salt;
        this.includeWorldSeed = includeWorldSeed;
        this.includeSequenceId = includeSequenceId;
        this.sequences.putAll(sequences);
    }

    public Random getOrCreate(Identifier id, long worldSeed) {
        Random random = this.sequences.computeIfAbsent(id, idx -> this.createSequence((Identifier)idx, worldSeed)).getSource();
        return new WrappedRandom(random);
    }

    private RandomSequence createSequence(Identifier id, long worldSeed) {
        return this.createSequence(id, worldSeed, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(Identifier id, long worldSeed, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        long l = (includeWorldSeed ? worldSeed : 0L) ^ (long)salt;
        return new RandomSequence(l, includeSequenceId ? Optional.of(id) : Optional.empty());
    }

    public void forEachSequence(BiConsumer<Identifier, RandomSequence> consumer) {
        this.sequences.forEach(consumer);
    }

    public void setDefaultParameters(int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.salt = salt;
        this.includeWorldSeed = includeWorldSeed;
        this.includeSequenceId = includeSequenceId;
    }

    public int resetAll() {
        int i = this.sequences.size();
        this.sequences.clear();
        return i;
    }

    public void reset(Identifier id, long worldSeed) {
        this.sequences.put(id, this.createSequence(id, worldSeed));
    }

    public void reset(Identifier id, long worldSeed, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.sequences.put(id, this.createSequence(id, worldSeed, salt, includeWorldSeed, includeSequenceId));
    }

    private int getSalt() {
        return this.salt;
    }

    private boolean shouldIncludeWorldSeed() {
        return this.includeWorldSeed;
    }

    private boolean shouldIncludeSequenceId() {
        return this.includeSequenceId;
    }

    class WrappedRandom
    implements Random {
        private final Random random;

        WrappedRandom(Random random) {
            this.random = random;
        }

        @Override
        public Random split() {
            RandomSequencesState.this.markDirty();
            return this.random.split();
        }

        @Override
        public RandomSplitter nextSplitter() {
            RandomSequencesState.this.markDirty();
            return this.random.nextSplitter();
        }

        @Override
        public void setSeed(long seed) {
            RandomSequencesState.this.markDirty();
            this.random.setSeed(seed);
        }

        @Override
        public int nextInt() {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt(bound);
        }

        @Override
        public long nextLong() {
            RandomSequencesState.this.markDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequencesState.this.markDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequencesState.this.markDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequencesState.this.markDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequencesState.this.markDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof WrappedRandom) {
                WrappedRandom wrappedRandom = (WrappedRandom)o;
                return this.random.equals(wrappedRandom.random);
            }
            return false;
        }
    }
}

