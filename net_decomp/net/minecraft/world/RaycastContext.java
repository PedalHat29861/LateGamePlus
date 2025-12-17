/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.rule.GameRules;

public class RaycastContext {
    private final Vec3d start;
    private final Vec3d end;
    private final ShapeType shapeType;
    private final FluidHandling fluid;
    private final ShapeContext shapeContext;

    public RaycastContext(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, Entity entity) {
        this(start, end, shapeType, fluidHandling, ShapeContext.of(entity));
    }

    public RaycastContext(Vec3d start, Vec3d end, ShapeType shapeType, FluidHandling fluidHandling, ShapeContext shapeContext) {
        this.start = start;
        this.end = end;
        this.shapeType = shapeType;
        this.fluid = fluidHandling;
        this.shapeContext = shapeContext;
    }

    public Vec3d getEnd() {
        return this.end;
    }

    public Vec3d getStart() {
        return this.start;
    }

    public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
        return this.shapeType.get(state, world, pos, this.shapeContext);
    }

    public VoxelShape getFluidShape(FluidState state, BlockView world, BlockPos pos) {
        return this.fluid.handled(state) ? state.getShape(world, pos) : VoxelShapes.empty();
    }

    public static final class ShapeType
    extends Enum<ShapeType>
    implements ShapeProvider {
        public static final /* enum */ ShapeType COLLIDER = new ShapeType(AbstractBlock.AbstractBlockState::getCollisionShape);
        public static final /* enum */ ShapeType OUTLINE = new ShapeType(AbstractBlock.AbstractBlockState::getOutlineShape);
        public static final /* enum */ ShapeType VISUAL = new ShapeType(AbstractBlock.AbstractBlockState::getCameraCollisionShape);
        public static final /* enum */ ShapeType FALLDAMAGE_RESETTING = new ShapeType((state, world, pos, context) -> {
            EntityShapeContext entityShapeContext;
            if (state.isIn(BlockTags.FALL_DAMAGE_RESETTING)) {
                return VoxelShapes.fullCube();
            }
            if (context instanceof EntityShapeContext && (entityShapeContext = (EntityShapeContext)context).getEntity() != null && entityShapeContext.getEntity().getType() == EntityType.PLAYER) {
                if (state.isOf(Blocks.END_GATEWAY) || state.isOf(Blocks.END_PORTAL)) {
                    return VoxelShapes.fullCube();
                }
                if (world instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld)world;
                    if (state.isOf(Blocks.NETHER_PORTAL) && serverWorld.getGameRules().getValue(GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY) == 0) {
                        return VoxelShapes.fullCube();
                    }
                }
            }
            return VoxelShapes.empty();
        });
        private final ShapeProvider provider;
        private static final /* synthetic */ ShapeType[] field_17561;

        public static ShapeType[] values() {
            return (ShapeType[])field_17561.clone();
        }

        public static ShapeType valueOf(String string) {
            return Enum.valueOf(ShapeType.class, string);
        }

        private ShapeType(ShapeProvider provider) {
            this.provider = provider;
        }

        @Override
        public VoxelShape get(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {
            return this.provider.get(blockState, blockView, blockPos, shapeContext);
        }

        private static /* synthetic */ ShapeType[] method_36690() {
            return new ShapeType[]{COLLIDER, OUTLINE, VISUAL, FALLDAMAGE_RESETTING};
        }

        static {
            field_17561 = ShapeType.method_36690();
        }
    }

    public static final class FluidHandling
    extends Enum<FluidHandling> {
        public static final /* enum */ FluidHandling NONE = new FluidHandling(state -> false);
        public static final /* enum */ FluidHandling SOURCE_ONLY = new FluidHandling(FluidState::isStill);
        public static final /* enum */ FluidHandling ANY = new FluidHandling(state -> !state.isEmpty());
        public static final /* enum */ FluidHandling WATER = new FluidHandling(state -> state.isIn(FluidTags.WATER));
        private final Predicate<FluidState> predicate;
        private static final /* synthetic */ FluidHandling[] field_1349;

        public static FluidHandling[] values() {
            return (FluidHandling[])field_1349.clone();
        }

        public static FluidHandling valueOf(String string) {
            return Enum.valueOf(FluidHandling.class, string);
        }

        private FluidHandling(Predicate<FluidState> predicate) {
            this.predicate = predicate;
        }

        public boolean handled(FluidState state) {
            return this.predicate.test(state);
        }

        private static /* synthetic */ FluidHandling[] method_36691() {
            return new FluidHandling[]{NONE, SOURCE_ONLY, ANY, WATER};
        }

        static {
            field_1349 = FluidHandling.method_36691();
        }
    }

    public static interface ShapeProvider {
        public VoxelShape get(BlockState var1, BlockView var2, BlockPos var3, ShapeContext var4);
    }
}

