/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.GameMode;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.timer.Timer;
import org.jspecify.annotations.Nullable;

public interface ServerWorldProperties
extends MutableWorldProperties {
    public String getLevelName();

    public void setThundering(boolean var1);

    public int getRainTime();

    public void setRainTime(int var1);

    public void setThunderTime(int var1);

    public int getThunderTime();

    @Override
    default public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
        MutableWorldProperties.super.populateCrashReport(reportSection, world);
        reportSection.add("Level name", this::getLevelName);
        reportSection.add("Level game mode", () -> String.format(Locale.ROOT, "Game mode: %s (ID %d). Hardcore: %b. Commands: %b", this.getGameMode().getId(), this.getGameMode().getIndex(), this.isHardcore(), this.areCommandsAllowed()));
        reportSection.add("Level weather", () -> String.format(Locale.ROOT, "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()));
    }

    public int getClearWeatherTime();

    public void setClearWeatherTime(int var1);

    public int getWanderingTraderSpawnDelay();

    public void setWanderingTraderSpawnDelay(int var1);

    public int getWanderingTraderSpawnChance();

    public void setWanderingTraderSpawnChance(int var1);

    public @Nullable UUID getWanderingTraderId();

    public void setWanderingTraderId(UUID var1);

    public GameMode getGameMode();

    @Deprecated
    public Optional<WorldBorder.Properties> getWorldBorder();

    @Deprecated
    public void setWorldBorder(Optional<WorldBorder.Properties> var1);

    public boolean isInitialized();

    public void setInitialized(boolean var1);

    public boolean areCommandsAllowed();

    public void setGameMode(GameMode var1);

    public Timer<MinecraftServer> getScheduledEvents();

    public void setTime(long var1);

    public void setTimeOfDay(long var1);

    public GameRules getGameRules();
}

