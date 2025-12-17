/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestException;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TickLimitExceededException;
import net.minecraft.test.TimedTaskRunner;
import net.minecraft.test.UnknownTestException;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jspecify.annotations.Nullable;

public class GameTestState {
    private final RegistryEntry.Reference<TestInstance> instanceEntry;
    private @Nullable BlockPos testBlockPos;
    private final ServerWorld world;
    private final Collection<TestListener> listeners = Lists.newArrayList();
    private final int tickLimit;
    private final Collection<TimedTaskRunner> timedTaskRunners = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> ticksByRunnables = new Object2LongOpenHashMap();
    private boolean initialized;
    private boolean tickedOnce;
    private int tick;
    private boolean started;
    private final TestAttemptConfig testAttemptConfig;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private boolean completed;
    private final BlockRotation rotation;
    private @Nullable TestException exception;
    private @Nullable TestInstanceBlockEntity blockEntity;

    public GameTestState(RegistryEntry.Reference<TestInstance> instanceEntry, BlockRotation rotation, ServerWorld world, TestAttemptConfig testAttemptConfig) {
        this.instanceEntry = instanceEntry;
        this.world = world;
        this.testAttemptConfig = testAttemptConfig;
        this.tickLimit = instanceEntry.value().getMaxTicks();
        this.rotation = rotation;
    }

    public void setTestBlockPos(@Nullable BlockPos testBlockPos) {
        this.testBlockPos = testBlockPos;
    }

    public GameTestState startCountdown(int additionalExpectedStopTime) {
        this.tick = -(this.instanceEntry.value().getSetupTicks() + additionalExpectedStopTime + 1);
        return this;
    }

    public void initializeImmediately() {
        if (this.initialized) {
            return;
        }
        TestInstanceBlockEntity testInstanceBlockEntity = this.getTestInstanceBlockEntity();
        if (!testInstanceBlockEntity.placeStructure()) {
            this.fail(Text.translatable("test.error.structure.failure", testInstanceBlockEntity.getTestName().getString()));
        }
        this.initialized = true;
        testInstanceBlockEntity.placeBarriers();
        BlockBox blockBox = testInstanceBlockEntity.getBlockBox();
        ((WorldTickScheduler)this.world.getBlockTickScheduler()).clearNextTicks(blockBox);
        this.world.clearUpdatesInArea(blockBox);
        this.listeners.forEach(listener -> listener.onStarted(this));
    }

    public void tick(TestRunContext context) {
        if (this.isCompleted()) {
            return;
        }
        if (!this.initialized) {
            this.fail(Text.translatable("test.error.ticking_without_structure"));
        }
        if (this.blockEntity == null) {
            this.fail(Text.translatable("test.error.missing_block_entity"));
        }
        if (this.exception != null) {
            this.complete();
        }
        if (!this.tickedOnce) {
            if (!this.blockEntity.getBlockBox().streamChunkPos().allMatch(this.world::shouldTickTestAt)) {
                return;
            }
        }
        this.tickedOnce = true;
        this.tickTests();
        if (this.isCompleted()) {
            if (this.exception != null) {
                this.listeners.forEach(listener -> listener.onFailed(this, context));
            } else {
                this.listeners.forEach(listener -> listener.onPassed(this, context));
            }
        }
    }

    private void tickTests() {
        ++this.tick;
        if (this.tick < 0) {
            return;
        }
        if (!this.started) {
            this.start();
        }
        ObjectIterator objectIterator = this.ticksByRunnables.object2LongEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2LongMap.Entry entry = (Object2LongMap.Entry)objectIterator.next();
            if (entry.getLongValue() > (long)this.tick) continue;
            try {
                ((Runnable)entry.getKey()).run();
            }
            catch (TestException testException) {
                this.fail(testException);
            }
            catch (Exception exception) {
                this.fail(new UnknownTestException(exception));
            }
            objectIterator.remove();
        }
        if (this.tick > this.tickLimit) {
            if (this.timedTaskRunners.isEmpty()) {
                this.fail(new TickLimitExceededException(Text.translatable("test.error.timeout.no_result", this.instanceEntry.value().getMaxTicks())));
            } else {
                this.timedTaskRunners.forEach(runner -> runner.runReported(this.tick));
                if (this.exception == null) {
                    this.fail(new TickLimitExceededException(Text.translatable("test.error.timeout.no_sequences_finished", this.instanceEntry.value().getMaxTicks())));
                }
            }
        } else {
            this.timedTaskRunners.forEach(runner -> runner.runSilently(this.tick));
        }
    }

    private void start() {
        if (this.started) {
            return;
        }
        this.started = true;
        this.stopwatch.start();
        this.getTestInstanceBlockEntity().setRunning();
        try {
            this.instanceEntry.value().start(new TestContext(this));
        }
        catch (TestException testException) {
            this.fail(testException);
        }
        catch (Exception exception) {
            this.fail(new UnknownTestException(exception));
        }
    }

    public void runAtTick(long tick, Runnable runnable) {
        this.ticksByRunnables.put((Object)runnable, tick);
    }

    public Identifier getId() {
        return this.instanceEntry.registryKey().getValue();
    }

    public @Nullable BlockPos getPos() {
        return this.testBlockPos;
    }

    public BlockPos getOrigin() {
        return this.blockEntity.getStartPos();
    }

    public Box getBoundingBox() {
        TestInstanceBlockEntity testInstanceBlockEntity = this.getTestInstanceBlockEntity();
        return testInstanceBlockEntity.getBox();
    }

    public TestInstanceBlockEntity getTestInstanceBlockEntity() {
        if (this.blockEntity == null) {
            if (this.testBlockPos == null) {
                throw new IllegalStateException("This GameTestInfo has no position");
            }
            BlockEntity blockEntity = this.world.getBlockEntity(this.testBlockPos);
            if (blockEntity instanceof TestInstanceBlockEntity) {
                TestInstanceBlockEntity testInstanceBlockEntity;
                this.blockEntity = testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
            }
            if (this.blockEntity == null) {
                throw new IllegalStateException("Could not find a test instance block entity at the given coordinate " + String.valueOf(this.testBlockPos));
            }
        }
        return this.blockEntity;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isPassed() {
        return this.completed && this.exception == null;
    }

    public boolean isFailed() {
        return this.exception != null;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public long getElapsedMilliseconds() {
        return this.stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private void complete() {
        if (!this.completed) {
            this.completed = true;
            if (this.stopwatch.isRunning()) {
                this.stopwatch.stop();
            }
        }
    }

    public void completeIfSuccessful() {
        if (this.exception == null) {
            this.complete();
            Box box = this.getBoundingBox();
            List<Entity> list = this.getWorld().getEntitiesByClass(Entity.class, box.expand(1.0), entity -> !(entity instanceof PlayerEntity));
            list.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
    }

    public void fail(Text message) {
        this.fail(new GameTestException(message, this.tick));
    }

    public void fail(TestException exception) {
        this.exception = exception;
    }

    public @Nullable TestException getThrowable() {
        return this.exception;
    }

    public String toString() {
        return this.getId().toString();
    }

    public void addListener(TestListener listener) {
        this.listeners.add(listener);
    }

    public @Nullable GameTestState init() {
        TestInstanceBlockEntity testInstanceBlockEntity = this.placeTestInstance(Objects.requireNonNull(this.testBlockPos), this.rotation, this.world);
        if (testInstanceBlockEntity != null) {
            this.blockEntity = testInstanceBlockEntity;
            this.initializeImmediately();
            return this;
        }
        return null;
    }

    private @Nullable TestInstanceBlockEntity placeTestInstance(BlockPos pos, BlockRotation rotation, ServerWorld world) {
        world.setBlockState(pos, Blocks.TEST_INSTANCE_BLOCK.getDefaultState());
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TestInstanceBlockEntity) {
            TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
            RegistryKey<TestInstance> registryKey = this.getInstanceEntry().registryKey();
            Vec3i vec3i = TestInstanceBlockEntity.getStructureSize(world, registryKey).orElse(new Vec3i(1, 1, 1));
            testInstanceBlockEntity.setData(new TestInstanceBlockEntity.Data(Optional.of(registryKey), vec3i, rotation, false, TestInstanceBlockEntity.Status.CLEARED, Optional.empty()));
            return testInstanceBlockEntity;
        }
        return null;
    }

    int getTick() {
        return this.tick;
    }

    TimedTaskRunner createTimedTaskRunner() {
        TimedTaskRunner timedTaskRunner = new TimedTaskRunner(this);
        this.timedTaskRunners.add(timedTaskRunner);
        return timedTaskRunner;
    }

    public boolean isRequired() {
        return this.instanceEntry.value().isRequired();
    }

    public boolean isOptional() {
        return !this.instanceEntry.value().isRequired();
    }

    public Identifier getStructure() {
        return this.instanceEntry.value().getStructure();
    }

    public BlockRotation getRotation() {
        return this.instanceEntry.value().getData().rotation().rotate(this.rotation);
    }

    public TestInstance getInstance() {
        return this.instanceEntry.value();
    }

    public RegistryEntry.Reference<TestInstance> getInstanceEntry() {
        return this.instanceEntry;
    }

    public int getTickLimit() {
        return this.tickLimit;
    }

    public boolean isFlaky() {
        return this.instanceEntry.value().getMaxAttempts() > 1;
    }

    public int getMaxAttempts() {
        return this.instanceEntry.value().getMaxAttempts();
    }

    public int getRequiredSuccesses() {
        return this.instanceEntry.value().getRequiredSuccesses();
    }

    public TestAttemptConfig getTestAttemptConfig() {
        return this.testAttemptConfig;
    }

    public Stream<TestListener> streamListeners() {
        return this.listeners.stream();
    }

    public GameTestState copy() {
        GameTestState gameTestState = new GameTestState(this.instanceEntry, this.rotation, this.world, this.getTestAttemptConfig());
        if (this.testBlockPos != null) {
            gameTestState.setTestBlockPos(this.testBlockPos);
        }
        return gameTestState;
    }
}

