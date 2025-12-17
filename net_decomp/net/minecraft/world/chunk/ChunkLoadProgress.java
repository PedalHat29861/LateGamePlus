/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface ChunkLoadProgress {
    public static ChunkLoadProgress compose(final ChunkLoadProgress first, final ChunkLoadProgress second) {
        return new ChunkLoadProgress(){

            @Override
            public void init(Stage stage, int chunks) {
                first.init(stage, chunks);
                second.init(stage, chunks);
            }

            @Override
            public void progress(Stage stage, int fullChunks, int totalChunks) {
                first.progress(stage, fullChunks, totalChunks);
                second.progress(stage, fullChunks, totalChunks);
            }

            @Override
            public void finish(Stage stage) {
                first.finish(stage);
                second.finish(stage);
            }

            @Override
            public void initSpawnPos(RegistryKey<World> worldKey, ChunkPos spawnChunk) {
                first.initSpawnPos(worldKey, spawnChunk);
                second.initSpawnPos(worldKey, spawnChunk);
            }
        };
    }

    public void init(Stage var1, int var2);

    public void progress(Stage var1, int var2, int var3);

    public void finish(Stage var1);

    public void initSpawnPos(RegistryKey<World> var1, ChunkPos var2);

    public static final class Stage
    extends Enum<Stage> {
        public static final /* enum */ Stage START_SERVER = new Stage();
        public static final /* enum */ Stage PREPARE_GLOBAL_SPAWN = new Stage();
        public static final /* enum */ Stage LOAD_INITIAL_CHUNKS = new Stage();
        public static final /* enum */ Stage LOAD_PLAYER_CHUNKS = new Stage();
        private static final /* synthetic */ Stage[] field_61109;

        public static Stage[] values() {
            return (Stage[])field_61109.clone();
        }

        public static Stage valueOf(String string) {
            return Enum.valueOf(Stage.class, string);
        }

        private static /* synthetic */ Stage[] method_72284() {
            return new Stage[]{START_SERVER, PREPARE_GLOBAL_SPAWN, LOAD_INITIAL_CHUNKS, LOAD_PLAYER_CHUNKS};
        }

        static {
            field_61109 = Stage.method_72284();
        }
    }
}

