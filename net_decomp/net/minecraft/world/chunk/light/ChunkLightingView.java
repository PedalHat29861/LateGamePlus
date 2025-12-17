/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.LightingView;
import org.jspecify.annotations.Nullable;

public interface ChunkLightingView
extends LightingView {
    public @Nullable ChunkNibbleArray getLightSection(ChunkSectionPos var1);

    public int getLightLevel(BlockPos var1);

    public static final class Empty
    extends Enum<Empty>
    implements ChunkLightingView {
        public static final /* enum */ Empty INSTANCE = new Empty();
        private static final /* synthetic */ Empty[] field_15811;

        public static Empty[] values() {
            return (Empty[])field_15811.clone();
        }

        public static Empty valueOf(String string) {
            return Enum.valueOf(Empty.class, string);
        }

        @Override
        public @Nullable ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
            return null;
        }

        @Override
        public int getLightLevel(BlockPos pos) {
            return 0;
        }

        @Override
        public void checkBlock(BlockPos pos) {
        }

        @Override
        public boolean hasUpdates() {
            return false;
        }

        @Override
        public int doLightUpdates() {
            return 0;
        }

        @Override
        public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
        }

        @Override
        public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        }

        @Override
        public void propagateLight(ChunkPos chunkPos) {
        }

        private static /* synthetic */ Empty[] method_36763() {
            return new Empty[]{INSTANCE};
        }

        static {
            field_15811 = Empty.method_36763();
        }
    }
}

