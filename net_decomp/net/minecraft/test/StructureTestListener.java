/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  org.apache.commons.lang3.exception.ExceptionUtils
 */
package net.minecraft.test;

import com.google.common.base.MoreObjects;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.NotEnoughSuccessesError;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestFailureLogger;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestRunContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.exception.ExceptionUtils;

class StructureTestListener
implements TestListener {
    private int attempt = 0;
    private int successes = 0;

    @Override
    public void onStarted(GameTestState test) {
        ++this.attempt;
    }

    private void retry(GameTestState state, TestRunContext context, boolean lastPassed) {
        TestAttemptConfig testAttemptConfig = state.getTestAttemptConfig();
        Object string = String.format(Locale.ROOT, "[Run: %4d, Ok: %4d, Fail: %4d", this.attempt, this.successes, this.attempt - this.successes);
        if (!testAttemptConfig.isDisabled()) {
            string = (String)string + String.format(Locale.ROOT, ", Left: %4d", testAttemptConfig.numberOfTries() - this.attempt);
        }
        string = (String)string + "]";
        String string2 = String.valueOf(state.getId()) + " " + (lastPassed ? "passed" : "failed") + "! " + state.getElapsedMilliseconds() + "ms";
        String string3 = String.format(Locale.ROOT, "%-53s%s", string, string2);
        if (lastPassed) {
            StructureTestListener.passTest(state, string3);
        } else {
            StructureTestListener.sendMessageToAllPlayers(state.getWorld(), Formatting.RED, string3);
        }
        if (testAttemptConfig.shouldTestAgain(this.attempt, this.successes)) {
            context.retry(state);
        }
    }

    @Override
    public void onPassed(GameTestState test, TestRunContext context) {
        ++this.successes;
        if (test.getTestAttemptConfig().needsMultipleAttempts()) {
            this.retry(test, context, true);
            return;
        }
        if (!test.isFlaky()) {
            StructureTestListener.passTest(test, String.valueOf(test.getId()) + " passed! (" + test.getElapsedMilliseconds() + "ms / " + test.getTick() + "gameticks)");
            return;
        }
        if (this.successes >= test.getRequiredSuccesses()) {
            StructureTestListener.passTest(test, String.valueOf(test) + " passed " + this.successes + " times of " + this.attempt + " attempts.");
        } else {
            StructureTestListener.sendMessageToAllPlayers(test.getWorld(), Formatting.GREEN, "Flaky test " + String.valueOf(test) + " succeeded, attempt: " + this.attempt + " successes: " + this.successes);
            context.retry(test);
        }
    }

    @Override
    public void onFailed(GameTestState test, TestRunContext context) {
        if (!test.isFlaky()) {
            StructureTestListener.failTest(test, test.getThrowable());
            if (test.getTestAttemptConfig().needsMultipleAttempts()) {
                this.retry(test, context, false);
            }
            return;
        }
        TestInstance testInstance = test.getInstance();
        String string = "Flaky test " + String.valueOf(test) + " failed, attempt: " + this.attempt + "/" + testInstance.getMaxAttempts();
        if (testInstance.getRequiredSuccesses() > 1) {
            string = string + ", successes: " + this.successes + " (" + testInstance.getRequiredSuccesses() + " required)";
        }
        StructureTestListener.sendMessageToAllPlayers(test.getWorld(), Formatting.YELLOW, string);
        if (test.getMaxAttempts() - this.attempt + this.successes >= test.getRequiredSuccesses()) {
            context.retry(test);
        } else {
            StructureTestListener.failTest(test, new NotEnoughSuccessesError(this.attempt, this.successes, test));
        }
    }

    @Override
    public void onRetry(GameTestState lastState, GameTestState nextState, TestRunContext context) {
        nextState.addListener(this);
    }

    public static void passTest(GameTestState test, String output) {
        StructureTestListener.getTestInstanceBlockEntity(test).ifPresent(testInstanceBlockEntity -> testInstanceBlockEntity.setFinished());
        StructureTestListener.finishPassedTest(test, output);
    }

    private static void finishPassedTest(GameTestState test, String output) {
        StructureTestListener.sendMessageToAllPlayers(test.getWorld(), Formatting.GREEN, output);
        TestFailureLogger.passTest(test);
    }

    protected static void failTest(GameTestState test, Throwable output) {
        Text text;
        if (output instanceof GameTestException) {
            GameTestException gameTestException = (GameTestException)output;
            text = gameTestException.getText();
        } else {
            text = Text.literal(Util.getInnermostMessage(output));
        }
        StructureTestListener.getTestInstanceBlockEntity(test).ifPresent(testInstanceBlockEntity -> testInstanceBlockEntity.setErrorMessage(text));
        StructureTestListener.finishFailedTest(test, output);
    }

    protected static void finishFailedTest(GameTestState test, Throwable output) {
        String string = output.getMessage() + (String)(output.getCause() == null ? "" : " cause: " + Util.getInnermostMessage(output.getCause()));
        String string2 = (test.isRequired() ? "" : "(optional) ") + String.valueOf(test.getId()) + " failed! " + string;
        StructureTestListener.sendMessageToAllPlayers(test.getWorld(), test.isRequired() ? Formatting.RED : Formatting.YELLOW, string2);
        Throwable throwable = (Throwable)MoreObjects.firstNonNull((Object)ExceptionUtils.getRootCause((Throwable)output), (Object)output);
        if (throwable instanceof PositionedException) {
            PositionedException positionedException = (PositionedException)throwable;
            test.getTestInstanceBlockEntity().addError(positionedException.getPos(), positionedException.getDebugMessage());
        }
        TestFailureLogger.failTest(test);
    }

    private static Optional<TestInstanceBlockEntity> getTestInstanceBlockEntity(GameTestState state) {
        ServerWorld serverWorld = state.getWorld();
        Optional<BlockPos> optional = Optional.ofNullable(state.getPos());
        Optional<TestInstanceBlockEntity> optional2 = optional.flatMap(pos -> serverWorld.getBlockEntity((BlockPos)pos, BlockEntityType.TEST_INSTANCE_BLOCK));
        return optional2;
    }

    protected static void sendMessageToAllPlayers(ServerWorld world, Formatting formatting, String message) {
        world.getPlayers(player -> true).forEach(player -> player.sendMessage(Text.literal(message).formatted(formatting)));
    }
}

