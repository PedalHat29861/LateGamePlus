/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongArraySet
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BatchListener;
import net.minecraft.test.Batches;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestListener;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstanceUtil;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestStructurePlacer;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TestRunContext {
    public static final int DEFAULT_TESTS_PER_ROW = 8;
    private static final Logger LOGGER = LogUtils.getLogger();
    final ServerWorld world;
    private final TestManager manager;
    private final List<GameTestState> states;
    private ImmutableList<GameTestBatch> batches;
    final List<BatchListener> batchListeners = Lists.newArrayList();
    private final List<GameTestState> toBeRetried = Lists.newArrayList();
    private final Batcher batcher;
    private boolean stopped = true;
    private @Nullable RegistryEntry<TestEnvironmentDefinition> environment;
    private final TestStructureSpawner reuseSpawner;
    private final TestStructureSpawner initialSpawner;
    final boolean stopAfterFailure;
    private final boolean clearBetweenBatches;

    protected TestRunContext(Batcher batcher, Collection<GameTestBatch> batches, ServerWorld world, TestManager manager, TestStructureSpawner reuseSpawner, TestStructureSpawner initialSpawner, boolean stopAfterFailure, boolean clearBetweenBatches) {
        this.world = world;
        this.manager = manager;
        this.batcher = batcher;
        this.reuseSpawner = reuseSpawner;
        this.initialSpawner = initialSpawner;
        this.batches = ImmutableList.copyOf(batches);
        this.stopAfterFailure = stopAfterFailure;
        this.clearBetweenBatches = clearBetweenBatches;
        this.states = this.batches.stream().flatMap(batch -> batch.states().stream()).collect(Util.toArrayList());
        manager.setRunContext(this);
        this.states.forEach(state -> state.addListener(new StructureTestListener()));
    }

    public List<GameTestState> getStates() {
        return this.states;
    }

    public void start() {
        this.stopped = false;
        this.runBatch(0);
    }

    public void clear() {
        this.stopped = true;
        if (this.environment != null) {
            this.clearEnvironment();
        }
    }

    public void retry(GameTestState state) {
        GameTestState gameTestState = state.copy();
        state.streamListeners().forEach(listener -> listener.onRetry(state, gameTestState, this));
        this.states.add(gameTestState);
        this.toBeRetried.add(gameTestState);
        if (this.stopped) {
            this.onFinish();
        }
    }

    void runBatch(final int batchIndex) {
        GameTestBatch gameTestBatch;
        if (batchIndex >= this.batches.size()) {
            this.clearEnvironment();
            this.onFinish();
            return;
        }
        if (batchIndex > 0 && this.clearBetweenBatches) {
            gameTestBatch = (GameTestBatch)this.batches.get(batchIndex - 1);
            gameTestBatch.states().forEach(state -> {
                TestInstanceBlockEntity testInstanceBlockEntity = state.getTestInstanceBlockEntity();
                TestInstanceUtil.clearArea(testInstanceBlockEntity.getBlockBox(), this.world);
                this.world.breakBlock(testInstanceBlockEntity.getPos(), false);
            });
        }
        gameTestBatch = (GameTestBatch)this.batches.get(batchIndex);
        this.reuseSpawner.onBatch(this.world);
        this.initialSpawner.onBatch(this.world);
        Collection<GameTestState> collection = this.prepareStructures(gameTestBatch.states());
        LOGGER.info("Running test environment '{}' batch {} ({} tests)...", new Object[]{gameTestBatch.environment().getIdAsString(), gameTestBatch.index(), collection.size()});
        this.clearEnvironment();
        this.environment = gameTestBatch.environment();
        this.environment.value().setup(this.world);
        this.batchListeners.forEach(listener -> listener.onStarted(gameTestBatch));
        final TestSet testSet = new TestSet();
        collection.forEach(testSet::add);
        testSet.addListener(new TestListener(){

            private void onFinished(GameTestState state) {
                state.getTestInstanceBlockEntity().clearBarriers();
                if (testSet.isDone()) {
                    TestRunContext.this.batchListeners.forEach(listener -> listener.onFinished(gameTestBatch));
                    LongArraySet longSet = new LongArraySet(TestRunContext.this.world.getForcedChunks());
                    longSet.forEach(chunkPos -> TestRunContext.this.world.setChunkForced(ChunkPos.getPackedX(chunkPos), ChunkPos.getPackedZ(chunkPos), false));
                    TestRunContext.this.runBatch(batchIndex + 1);
                }
            }

            @Override
            public void onStarted(GameTestState test) {
            }

            @Override
            public void onPassed(GameTestState test, TestRunContext context) {
                this.onFinished(test);
            }

            @Override
            public void onFailed(GameTestState test, TestRunContext context) {
                if (TestRunContext.this.stopAfterFailure) {
                    TestRunContext.this.clearEnvironment();
                    LongArraySet longSet = new LongArraySet(TestRunContext.this.world.getForcedChunks());
                    longSet.forEach(chunkPos -> TestRunContext.this.world.setChunkForced(ChunkPos.getPackedX(chunkPos), ChunkPos.getPackedZ(chunkPos), false));
                    TestManager.INSTANCE.clear();
                    test.getTestInstanceBlockEntity().clearBarriers();
                } else {
                    this.onFinished(test);
                }
            }

            @Override
            public void onRetry(GameTestState lastState, GameTestState nextState, TestRunContext context) {
            }
        });
        collection.forEach(this.manager::start);
    }

    void clearEnvironment() {
        if (this.environment != null) {
            this.environment.value().teardown(this.world);
            this.environment = null;
        }
    }

    private void onFinish() {
        if (!this.toBeRetried.isEmpty()) {
            LOGGER.info("Starting re-run of tests: {}", (Object)this.toBeRetried.stream().map(state -> state.getId().toString()).collect(Collectors.joining(", ")));
            this.batches = ImmutableList.copyOf(this.batcher.batch(this.toBeRetried));
            this.toBeRetried.clear();
            this.stopped = false;
            this.runBatch(0);
        } else {
            this.batches = ImmutableList.of();
            this.stopped = true;
        }
    }

    public void addBatchListener(BatchListener batchListener) {
        this.batchListeners.add(batchListener);
    }

    private Collection<GameTestState> prepareStructures(Collection<GameTestState> oldStates) {
        return oldStates.stream().map(this::prepareStructure).flatMap(Optional::stream).toList();
    }

    private Optional<GameTestState> prepareStructure(GameTestState oldState) {
        if (oldState.getPos() == null) {
            return this.initialSpawner.spawnStructure(oldState);
        }
        return this.reuseSpawner.spawnStructure(oldState);
    }

    public static interface Batcher {
        public Collection<GameTestBatch> batch(Collection<GameTestState> var1);
    }

    public static interface TestStructureSpawner {
        public static final TestStructureSpawner REUSE = oldState -> Optional.ofNullable(oldState.init()).map(gameTestState -> gameTestState.startCountdown(1));
        public static final TestStructureSpawner NOOP = oldState -> Optional.empty();

        public Optional<GameTestState> spawnStructure(GameTestState var1);

        default public void onBatch(ServerWorld world) {
        }
    }

    public static class Builder {
        private final ServerWorld world;
        private final TestManager manager = TestManager.INSTANCE;
        private Batcher batcher = Batches.defaultBatcher();
        private TestStructureSpawner reuseSpawner = TestStructureSpawner.REUSE;
        private TestStructureSpawner initialSpawner = TestStructureSpawner.NOOP;
        private final Collection<GameTestBatch> batches;
        private boolean stopAfterFailure = false;
        private boolean clearBetweenBatches = false;

        private Builder(Collection<GameTestBatch> batches, ServerWorld world) {
            this.batches = batches;
            this.world = world;
        }

        public static Builder of(Collection<GameTestBatch> batches, ServerWorld world) {
            return new Builder(batches, world);
        }

        public static Builder ofStates(Collection<GameTestState> states, ServerWorld world) {
            return Builder.of(Batches.defaultBatcher().batch(states), world);
        }

        public Builder stopAfterFailure() {
            this.stopAfterFailure = true;
            return this;
        }

        public Builder clearBetweenBatches() {
            this.clearBetweenBatches = true;
            return this;
        }

        public Builder initialSpawner(TestStructureSpawner initialSpawner) {
            this.initialSpawner = initialSpawner;
            return this;
        }

        public Builder reuseSpawner(TestStructurePlacer reuseSpawner) {
            this.reuseSpawner = reuseSpawner;
            return this;
        }

        public Builder batcher(Batcher batcher) {
            this.batcher = batcher;
            return this;
        }

        public TestRunContext build() {
            return new TestRunContext(this.batcher, this.batches, this.world, this.manager, this.reuseSpawner, this.initialSpawner, this.stopAfterFailure, this.clearBetweenBatches);
        }
    }
}

