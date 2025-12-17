/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.world.event.listener;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.Vibration;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
    public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Vibration.CODEC.lenientOptionalFieldOf("event").forGetter(vibrationSelector -> vibrationSelector.current.map(Pair::getLeft)), (App)Codec.LONG.fieldOf("tick").forGetter(vibrationSelector -> vibrationSelector.current.map(Pair::getRight).orElse(-1L))).apply((Applicative)instance, VibrationSelector::new));
    private Optional<Pair<Vibration, Long>> current;

    public VibrationSelector(Optional<Vibration> vibration, long tick) {
        this.current = vibration.map(vibration2 -> Pair.of((Object)vibration2, (Object)tick));
    }

    public VibrationSelector() {
        this.current = Optional.empty();
    }

    public void tryAccept(Vibration vibration, long tick) {
        if (this.shouldSelect(vibration, tick)) {
            this.current = Optional.of(Pair.of((Object)vibration, (Object)tick));
        }
    }

    private boolean shouldSelect(Vibration vibration, long tick) {
        if (this.current.isEmpty()) {
            return true;
        }
        Pair<Vibration, Long> pair = this.current.get();
        long l = (Long)pair.getRight();
        if (tick != l) {
            return false;
        }
        Vibration vibration2 = (Vibration)pair.getLeft();
        if (vibration.distance() < vibration2.distance()) {
            return true;
        }
        if (vibration.distance() > vibration2.distance()) {
            return false;
        }
        return Vibrations.getFrequency(vibration.gameEvent()) > Vibrations.getFrequency(vibration2.gameEvent());
    }

    public Optional<Vibration> getVibrationToTick(long currentTick) {
        if (this.current.isEmpty()) {
            return Optional.empty();
        }
        if ((Long)this.current.get().getRight() < currentTick) {
            return Optional.of((Vibration)this.current.get().getLeft());
        }
        return Optional.empty();
    }

    public void clear() {
        this.current = Optional.empty();
    }
}

