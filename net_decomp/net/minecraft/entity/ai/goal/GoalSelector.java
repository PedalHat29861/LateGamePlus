/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 */
package net.minecraft.entity.ai.goal;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public class GoalSelector {
    private static final PrioritizedGoal REPLACEABLE_GOAL = new PrioritizedGoal(Integer.MAX_VALUE, new Goal(){

        @Override
        public boolean canStart() {
            return false;
        }
    }){

        @Override
        public boolean isRunning() {
            return false;
        }
    };
    private final Map<Goal.Control, PrioritizedGoal> goalsByControl = new EnumMap<Goal.Control, PrioritizedGoal>(Goal.Control.class);
    private final Set<PrioritizedGoal> goals = new ObjectLinkedOpenHashSet();
    private final EnumSet<Goal.Control> disabledControls = EnumSet.noneOf(Goal.Control.class);

    public void add(int priority, Goal goal) {
        this.goals.add(new PrioritizedGoal(priority, goal));
    }

    public void clear(Predicate<Goal> predicate) {
        this.goals.removeIf(goal -> predicate.test(goal.getGoal()));
    }

    public void remove(Goal goal) {
        for (PrioritizedGoal prioritizedGoal2 : this.goals) {
            if (prioritizedGoal2.getGoal() != goal || !prioritizedGoal2.isRunning()) continue;
            prioritizedGoal2.stop();
        }
        this.goals.removeIf(prioritizedGoal -> prioritizedGoal.getGoal() == goal);
    }

    private static boolean usesAny(PrioritizedGoal goal, EnumSet<Goal.Control> controls) {
        for (Goal.Control control : goal.getControls()) {
            if (!controls.contains((Object)control)) continue;
            return true;
        }
        return false;
    }

    private static boolean canReplaceAll(PrioritizedGoal goal, Map<Goal.Control, PrioritizedGoal> goalsByControl) {
        for (Goal.Control control : goal.getControls()) {
            if (goalsByControl.getOrDefault((Object)control, REPLACEABLE_GOAL).canBeReplacedBy(goal)) continue;
            return false;
        }
        return true;
    }

    public void tick() {
        Profiler profiler = Profilers.get();
        profiler.push("goalCleanup");
        for (PrioritizedGoal prioritizedGoal : this.goals) {
            if (!prioritizedGoal.isRunning() || !GoalSelector.usesAny(prioritizedGoal, this.disabledControls) && prioritizedGoal.shouldContinue()) continue;
            prioritizedGoal.stop();
        }
        this.goalsByControl.entrySet().removeIf(entry -> !((PrioritizedGoal)entry.getValue()).isRunning());
        profiler.pop();
        profiler.push("goalUpdate");
        for (PrioritizedGoal prioritizedGoal : this.goals) {
            if (prioritizedGoal.isRunning() || GoalSelector.usesAny(prioritizedGoal, this.disabledControls) || !GoalSelector.canReplaceAll(prioritizedGoal, this.goalsByControl) || !prioritizedGoal.canStart()) continue;
            for (Goal.Control control : prioritizedGoal.getControls()) {
                PrioritizedGoal prioritizedGoal2 = this.goalsByControl.getOrDefault((Object)control, REPLACEABLE_GOAL);
                prioritizedGoal2.stop();
                this.goalsByControl.put(control, prioritizedGoal);
            }
            prioritizedGoal.start();
        }
        profiler.pop();
        this.tickGoals(true);
    }

    public void tickGoals(boolean tickAll) {
        Profiler profiler = Profilers.get();
        profiler.push("goalTick");
        for (PrioritizedGoal prioritizedGoal : this.goals) {
            if (!prioritizedGoal.isRunning() || !tickAll && !prioritizedGoal.shouldRunEveryTick()) continue;
            prioritizedGoal.tick();
        }
        profiler.pop();
    }

    public Set<PrioritizedGoal> getGoals() {
        return this.goals;
    }

    public void disableControl(Goal.Control control) {
        this.disabledControls.add(control);
    }

    public void enableControl(Goal.Control control) {
        this.disabledControls.remove((Object)control);
    }

    public void setControlEnabled(Goal.Control control, boolean enabled) {
        if (enabled) {
            this.enableControl(control);
        } else {
            this.disableControl(control);
        }
    }
}

