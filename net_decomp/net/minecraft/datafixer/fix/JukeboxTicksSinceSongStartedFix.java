/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class JukeboxTicksSinceSongStartedFix
extends ChoiceFix {
    public JukeboxTicksSinceSongStartedFix(Schema outputSchema) {
        super(outputSchema, false, "JukeboxTicksSinceSongStartedFix", TypeReferences.BLOCK_ENTITY, "minecraft:jukebox");
    }

    public Dynamic<?> fixTicksSinceSongStarted(Dynamic<?> dynamic) {
        long l = dynamic.get("TickCount").asLong(0L) - dynamic.get("RecordStartTick").asLong(0L);
        Dynamic dynamic2 = dynamic.remove("IsPlaying").remove("TickCount").remove("RecordStartTick");
        if (l > 0L) {
            return dynamic2.set("ticks_since_song_started", dynamic.createLong(l));
        }
        return dynamic2;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixTicksSinceSongStarted);
    }
}

