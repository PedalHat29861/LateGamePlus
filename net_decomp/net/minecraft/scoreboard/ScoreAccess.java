/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public interface ScoreAccess {
    public int getScore();

    public void setScore(int var1);

    default public int incrementScore(int amount) {
        int i = this.getScore() + amount;
        this.setScore(i);
        return i;
    }

    default public int incrementScore() {
        return this.incrementScore(1);
    }

    default public void resetScore() {
        this.setScore(0);
    }

    public boolean isLocked();

    public void unlock();

    public void lock();

    public @Nullable Text getDisplayText();

    public void setDisplayText(@Nullable Text var1);

    public void setNumberFormat(@Nullable NumberFormat var1);
}

