/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

@Name(value="minecraft.StructureGeneration")
@Label(value="Structure Generation")
@Category(value={"Minecraft", "World Generation"})
@StackTrace(value=false)
@Enabled(value=false)
@DontObfuscate
public class StructureGenerationEvent
extends Event {
    public static final String EVENT_NAME = "minecraft.StructureGeneration";
    public static final EventType TYPE = EventType.getEventType(StructureGenerationEvent.class);
    @Name(value="chunkPosX")
    @Label(value="Chunk X Position")
    public final int chunkPosX;
    @Name(value="chunkPosZ")
    @Label(value="Chunk Z Position")
    public final int chunkPosZ;
    @Name(value="structure")
    @Label(value="Structure")
    public final String structure;
    @Name(value="level")
    @Label(value="Level")
    public final String level;
    @Name(value="success")
    @Label(value="Success")
    public boolean success;

    public StructureGenerationEvent(ChunkPos chunkPos, RegistryEntry<Structure> structure, RegistryKey<World> dimension) {
        this.chunkPosX = chunkPos.x;
        this.chunkPosZ = chunkPos.z;
        this.structure = structure.getIdAsString();
        this.level = dimension.getValue().toString();
    }

    public static interface Names {
        public static final String CHUNK_POS_X = "chunkPosX";
        public static final String CHUNK_POS_Z = "chunkPosZ";
        public static final String STRUCTURE = "structure";
        public static final String LEVEL = "level";
        public static final String SUCCESS = "success";
    }
}

