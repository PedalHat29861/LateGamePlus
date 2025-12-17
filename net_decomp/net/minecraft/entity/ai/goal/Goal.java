/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class Goal {
    private final EnumSet<Control> controls = EnumSet.noneOf(Control.class);

    public abstract boolean canStart();

    public boolean shouldContinue() {
        return this.canStart();
    }

    public boolean canStop() {
        return true;
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean shouldRunEveryTick() {
        return false;
    }

    public void tick() {
    }

    public void setControls(EnumSet<Control> controls) {
        this.controls.clear();
        this.controls.addAll(controls);
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public EnumSet<Control> getControls() {
        return this.controls;
    }

    protected int getTickCount(int ticks) {
        return this.shouldRunEveryTick() ? ticks : Goal.toGoalTicks(ticks);
    }

    protected static int toGoalTicks(int serverTicks) {
        return MathHelper.ceilDiv(serverTicks, 2);
    }

    protected static ServerWorld getServerWorld(Entity entity) {
        return (ServerWorld)entity.getEntityWorld();
    }

    protected static ServerWorld castToServerWorld(World world) {
        return (ServerWorld)world;
    }

    public static final class Control
    extends Enum<Control> {
        public static final /* enum */ Control MOVE = new Control();
        public static final /* enum */ Control LOOK = new Control();
        public static final /* enum */ Control JUMP = new Control();
        public static final /* enum */ Control TARGET = new Control();
        private static final /* synthetic */ Control[] field_18409;

        public static Control[] values() {
            return (Control[])field_18409.clone();
        }

        public static Control valueOf(String string) {
            return Enum.valueOf(Control.class, string);
        }

        private static /* synthetic */ Control[] method_36621() {
            return new Control[]{MOVE, LOOK, JUMP, TARGET};
        }

        static {
            field_18409 = Control.method_36621();
        }
    }
}

