/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public interface StructureBoxRendering {
    public RenderMode getRenderMode();

    public StructureBox getStructureBox();

    public static final class RenderMode
    extends Enum<RenderMode> {
        public static final /* enum */ RenderMode NONE = new RenderMode();
        public static final /* enum */ RenderMode BOX = new RenderMode();
        public static final /* enum */ RenderMode BOX_AND_INVISIBLE_BLOCKS = new RenderMode();
        private static final /* synthetic */ RenderMode[] field_55997;

        public static RenderMode[] values() {
            return (RenderMode[])field_55997.clone();
        }

        public static RenderMode valueOf(String string) {
            return Enum.valueOf(RenderMode.class, string);
        }

        private static /* synthetic */ RenderMode[] method_66715() {
            return new RenderMode[]{NONE, BOX, BOX_AND_INVISIBLE_BLOCKS};
        }

        static {
            field_55997 = RenderMode.method_66715();
        }
    }

    public record StructureBox(BlockPos localPos, Vec3i size) {
        public static StructureBox create(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            int i = Math.min(minX, maxX);
            int j = Math.min(minY, maxY);
            int k = Math.min(minZ, maxZ);
            return new StructureBox(new BlockPos(i, j, k), new Vec3i(Math.max(minX, maxX) - i, Math.max(minY, maxY) - j, Math.max(minZ, maxZ) - k));
        }
    }
}

