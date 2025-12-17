/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CommandBlockExecutor;

public class CommandBlockBlockEntity
extends BlockEntity {
    private static final boolean DEFAULT_POWERED = false;
    private static final boolean DEFAULT_AUTO = false;
    private static final boolean DEFAULT_CONDITION_MET = false;
    private boolean powered = false;
    private boolean auto = false;
    private boolean conditionMet = false;
    private final CommandBlockExecutor commandExecutor = new CommandBlockExecutor(){

        @Override
        public void setCommand(String command) {
            super.setCommand(command);
            CommandBlockBlockEntity.this.markDirty();
        }

        @Override
        public void markDirty(ServerWorld world) {
            BlockState blockState = world.getBlockState(CommandBlockBlockEntity.this.pos);
            world.updateListeners(CommandBlockBlockEntity.this.pos, blockState, blockState, 3);
        }

        @Override
        public ServerCommandSource getSource(ServerWorld world, CommandOutput output) {
            Direction direction = CommandBlockBlockEntity.this.getCachedState().get(CommandBlock.FACING);
            return new ServerCommandSource(output, Vec3d.ofCenter(CommandBlockBlockEntity.this.pos), new Vec2f(0.0f, direction.getPositiveHorizontalDegrees()), world, LeveledPermissionPredicate.GAMEMASTERS, this.getName().getString(), this.getName(), world.getServer(), null);
        }

        @Override
        public boolean isEditable() {
            return !CommandBlockBlockEntity.this.isRemoved();
        }
    };

    public CommandBlockBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.COMMAND_BLOCK, pos, state);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        this.commandExecutor.writeData(view);
        view.putBoolean("powered", this.isPowered());
        view.putBoolean("conditionMet", this.isConditionMet());
        view.putBoolean("auto", this.isAuto());
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.commandExecutor.readData(view);
        this.powered = view.getBoolean("powered", false);
        this.conditionMet = view.getBoolean("conditionMet", false);
        this.setAuto(view.getBoolean("auto", false));
    }

    public CommandBlockExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean isAuto() {
        return this.auto;
    }

    public void setAuto(boolean auto) {
        boolean bl = this.auto;
        this.auto = auto;
        if (!bl && auto && !this.powered && this.world != null && this.getCommandBlockType() != Type.SEQUENCE) {
            this.scheduleAutoTick();
        }
    }

    public void updateCommandBlock() {
        Type type = this.getCommandBlockType();
        if (type == Type.AUTO && (this.powered || this.auto) && this.world != null) {
            this.scheduleAutoTick();
        }
    }

    private void scheduleAutoTick() {
        Block block = this.getCachedState().getBlock();
        if (block instanceof CommandBlock) {
            this.updateConditionMet();
            this.world.scheduleBlockTick(this.pos, block, 1);
        }
    }

    public boolean isConditionMet() {
        return this.conditionMet;
    }

    public boolean updateConditionMet() {
        this.conditionMet = true;
        if (this.isConditionalCommandBlock()) {
            BlockEntity blockEntity;
            BlockPos blockPos = this.pos.offset(this.world.getBlockState(this.pos).get(CommandBlock.FACING).getOpposite());
            this.conditionMet = this.world.getBlockState(blockPos).getBlock() instanceof CommandBlock ? (blockEntity = this.world.getBlockEntity(blockPos)) instanceof CommandBlockBlockEntity && ((CommandBlockBlockEntity)blockEntity).getCommandExecutor().getSuccessCount() > 0 : false;
        }
        return this.conditionMet;
    }

    public Type getCommandBlockType() {
        BlockState blockState = this.getCachedState();
        if (blockState.isOf(Blocks.COMMAND_BLOCK)) {
            return Type.REDSTONE;
        }
        if (blockState.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
            return Type.AUTO;
        }
        if (blockState.isOf(Blocks.CHAIN_COMMAND_BLOCK)) {
            return Type.SEQUENCE;
        }
        return Type.REDSTONE;
    }

    public boolean isConditionalCommandBlock() {
        BlockState blockState = this.world.getBlockState(this.getPos());
        if (blockState.getBlock() instanceof CommandBlock) {
            return blockState.get(CommandBlock.CONDITIONAL);
        }
        return false;
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.commandExecutor.setCustomName(components.get(DataComponentTypes.CUSTOM_NAME));
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CUSTOM_NAME, this.commandExecutor.getCustomName());
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("CustomName");
        view.remove("conditionMet");
        view.remove("powered");
    }

    public static final class Type
    extends Enum<Type> {
        public static final /* enum */ Type SEQUENCE = new Type();
        public static final /* enum */ Type AUTO = new Type();
        public static final /* enum */ Type REDSTONE = new Type();
        private static final /* synthetic */ Type[] field_11925;

        public static Type[] values() {
            return (Type[])field_11925.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private static /* synthetic */ Type[] method_36715() {
            return new Type[]{SEQUENCE, AUTO, REDSTONE};
        }

        static {
            field_11925 = Type.method_36715();
        }
    }
}

