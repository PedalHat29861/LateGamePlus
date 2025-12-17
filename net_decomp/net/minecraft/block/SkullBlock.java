/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SkullBlock
extends AbstractSkullBlock {
    public static final MapCodec<SkullBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SkullType.CODEC.fieldOf("kind").forGetter(AbstractSkullBlock::getSkullType), SkullBlock.createSettingsCodec()).apply((Applicative)instance, SkullBlock::new));
    public static final int MAX_ROTATION_INDEX = RotationPropertyHelper.getMax();
    private static final int MAX_ROTATIONS = MAX_ROTATION_INDEX + 1;
    public static final IntProperty ROTATION = Properties.ROTATION;
    private static final VoxelShape SHAPE = Block.createColumnShape(8.0, 0.0, 8.0);
    private static final VoxelShape PIGLIN_SHAPE = Block.createColumnShape(10.0, 0.0, 8.0);

    public MapCodec<? extends SkullBlock> getCodec() {
        return CODEC;
    }

    protected SkullBlock(SkullType skullType, AbstractBlock.Settings settings) {
        super(skullType, settings);
        this.setDefaultState((BlockState)this.getDefaultState().with(ROTATION, 0));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getSkullType() == Type.PIGLIN ? PIGLIN_SHAPE : SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)super.getPlacementState(ctx).with(ROTATION, RotationPropertyHelper.fromYaw(ctx.getPlayerYaw()));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(ROTATION, rotation.rotate(state.get(ROTATION), MAX_ROTATIONS));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)state.with(ROTATION, mirror.mirror(state.get(ROTATION), MAX_ROTATIONS));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ROTATION);
    }

    public static interface SkullType
    extends StringIdentifiable {
        public static final Map<String, SkullType> TYPES = new Object2ObjectArrayMap();
        public static final Codec<SkullType> CODEC = Codec.stringResolver(StringIdentifiable::asString, TYPES::get);
    }

    public static final class Type
    extends Enum<Type>
    implements SkullType {
        public static final /* enum */ Type SKELETON = new Type("skeleton");
        public static final /* enum */ Type WITHER_SKELETON = new Type("wither_skeleton");
        public static final /* enum */ Type PLAYER = new Type("player");
        public static final /* enum */ Type ZOMBIE = new Type("zombie");
        public static final /* enum */ Type CREEPER = new Type("creeper");
        public static final /* enum */ Type PIGLIN = new Type("piglin");
        public static final /* enum */ Type DRAGON = new Type("dragon");
        private final String id;
        private static final /* synthetic */ Type[] field_11509;

        public static Type[] values() {
            return (Type[])field_11509.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(String id) {
            this.id = id;
            TYPES.put(id, this);
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Type[] method_36710() {
            return new Type[]{SKELETON, WITHER_SKELETON, PLAYER, ZOMBIE, CREEPER, PIGLIN, DRAGON};
        }

        static {
            field_11509 = Type.method_36710();
        }
    }
}

