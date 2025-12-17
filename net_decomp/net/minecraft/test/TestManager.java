/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestRunContext;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TestManager {
    public static final TestManager INSTANCE = new TestManager();
    private static final Logger field_61884 = LogUtils.getLogger();
    private final Collection<GameTestState> tests = Lists.newCopyOnWriteArrayList();
    private @Nullable TestRunContext runContext;
    private State state = State.IDLE;

    private TestManager() {
    }

    public void start(GameTestState test) {
        this.tests.add(test);
    }

    public void clear() {
        if (this.state != State.IDLE) {
            this.state = State.HALTING;
            return;
        }
        this.tests.clear();
        if (this.runContext != null) {
            this.runContext.clear();
            this.runContext = null;
        }
    }

    public void setRunContext(TestRunContext runContext) {
        if (this.runContext != null) {
            Util.logErrorOrPause("The runner was already set in GameTestTicker");
        }
        this.runContext = runContext;
    }

    public void tick() {
        if (this.runContext == null) {
            return;
        }
        this.state = State.RUNNING;
        this.tests.forEach(test -> test.tick(this.runContext));
        this.tests.removeIf(GameTestState::isCompleted);
        State state = this.state;
        this.state = State.IDLE;
        if (state == State.HALTING) {
            this.clear();
        }
    }

    static final class State
    extends Enum<State> {
        public static final /* enum */ State IDLE = new State();
        public static final /* enum */ State RUNNING = new State();
        public static final /* enum */ State HALTING = new State();
        private static final /* synthetic */ State[] field_57045;

        public static State[] values() {
            return (State[])field_57045.clone();
        }

        public static State valueOf(String string) {
            return Enum.valueOf(State.class, string);
        }

        private static /* synthetic */ State[] method_68078() {
            return new State[]{IDLE, RUNNING, HALTING};
        }

        static {
            field_57045 = State.method_68078();
        }
    }
}

