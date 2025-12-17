/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBoxRendering;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class StructureBlockBlockEntity
extends BlockEntity
implements StructureBoxRendering {
    private static final int field_31367 = 5;
    public static final int field_31364 = 48;
    public static final int field_31365 = 48;
    public static final String AUTHOR_KEY = "author";
    private static final String DEFAULT_AUTHOR = "";
    private static final String DEFAULT_METADATA = "";
    private static final BlockPos DEFAULT_OFFSET = new BlockPos(0, 1, 0);
    private static final Vec3i DEFAULT_SIZE = Vec3i.ZERO;
    private static final BlockRotation DEFAULT_ROTATION = BlockRotation.NONE;
    private static final BlockMirror DEFAULT_MIRROR = BlockMirror.NONE;
    private static final boolean DEFAULT_IGNORE_ENTITIES = true;
    private static final boolean DEFAULT_STRICT = false;
    private static final boolean DEFAULT_POWERED = false;
    private static final boolean DEFAULT_SHOW_AIR = false;
    private static final boolean DEFAULT_SHOW_BOUNDING_BOX = true;
    private static final float DEFAULT_INTEGRITY = 1.0f;
    private static final long DEFAULT_SEED = 0L;
    private @Nullable Identifier templateName;
    private String author = "";
    private String metadata = "";
    private BlockPos offset = DEFAULT_OFFSET;
    private Vec3i size = DEFAULT_SIZE;
    private BlockMirror mirror = BlockMirror.NONE;
    private BlockRotation rotation = BlockRotation.NONE;
    private StructureBlockMode mode;
    private boolean ignoreEntities = true;
    private boolean strict = false;
    private boolean powered = false;
    private boolean showAir = false;
    private boolean showBoundingBox = true;
    private float integrity = 1.0f;
    private long seed = 0L;

    public StructureBlockBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.STRUCTURE_BLOCK, pos, state);
        this.mode = state.get(StructureBlock.MODE);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putString("name", this.getTemplateName());
        view.putString(AUTHOR_KEY, this.author);
        view.putString("metadata", this.metadata);
        view.putInt("posX", this.offset.getX());
        view.putInt("posY", this.offset.getY());
        view.putInt("posZ", this.offset.getZ());
        view.putInt("sizeX", this.size.getX());
        view.putInt("sizeY", this.size.getY());
        view.putInt("sizeZ", this.size.getZ());
        view.put("rotation", BlockRotation.ENUM_NAME_CODEC, this.rotation);
        view.put("mirror", BlockMirror.ENUM_NAME_CODEC, this.mirror);
        view.put("mode", StructureBlockMode.CODEC, this.mode);
        view.putBoolean("ignoreEntities", this.ignoreEntities);
        view.putBoolean("strict", this.strict);
        view.putBoolean("powered", this.powered);
        view.putBoolean("showair", this.showAir);
        view.putBoolean("showboundingbox", this.showBoundingBox);
        view.putFloat("integrity", this.integrity);
        view.putLong("seed", this.seed);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.setTemplateName(view.getString("name", ""));
        this.author = view.getString(AUTHOR_KEY, "");
        this.metadata = view.getString("metadata", "");
        int i = MathHelper.clamp(view.getInt("posX", DEFAULT_OFFSET.getX()), -48, 48);
        int j = MathHelper.clamp(view.getInt("posY", DEFAULT_OFFSET.getY()), -48, 48);
        int k = MathHelper.clamp(view.getInt("posZ", DEFAULT_OFFSET.getZ()), -48, 48);
        this.offset = new BlockPos(i, j, k);
        int l = MathHelper.clamp(view.getInt("sizeX", DEFAULT_SIZE.getX()), 0, 48);
        int m = MathHelper.clamp(view.getInt("sizeY", DEFAULT_SIZE.getY()), 0, 48);
        int n = MathHelper.clamp(view.getInt("sizeZ", DEFAULT_SIZE.getZ()), 0, 48);
        this.size = new Vec3i(l, m, n);
        this.rotation = view.read("rotation", BlockRotation.ENUM_NAME_CODEC).orElse(DEFAULT_ROTATION);
        this.mirror = view.read("mirror", BlockMirror.ENUM_NAME_CODEC).orElse(DEFAULT_MIRROR);
        this.mode = view.read("mode", StructureBlockMode.CODEC).orElse(StructureBlockMode.DATA);
        this.ignoreEntities = view.getBoolean("ignoreEntities", true);
        this.strict = view.getBoolean("strict", false);
        this.powered = view.getBoolean("powered", false);
        this.showAir = view.getBoolean("showair", false);
        this.showBoundingBox = view.getBoolean("showboundingbox", true);
        this.integrity = view.getFloat("integrity", 1.0f);
        this.seed = view.getLong("seed", 0L);
        this.updateBlockMode();
    }

    private void updateBlockMode() {
        if (this.world == null) {
            return;
        }
        BlockPos blockPos = this.getPos();
        BlockState blockState = this.world.getBlockState(blockPos);
        if (blockState.isOf(Blocks.STRUCTURE_BLOCK)) {
            this.world.setBlockState(blockPos, (BlockState)blockState.with(StructureBlock.MODE, this.mode), 2);
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    public boolean openScreen(PlayerEntity player) {
        if (!player.isCreativeLevelTwoOp()) {
            return false;
        }
        if (player.getEntityWorld().isClient()) {
            player.openStructureBlockScreen(this);
        }
        return true;
    }

    public String getTemplateName() {
        return this.templateName == null ? "" : this.templateName.toString();
    }

    public boolean hasStructureName() {
        return this.templateName != null;
    }

    public void setTemplateName(@Nullable String templateName) {
        this.setTemplateName(StringHelper.isEmpty(templateName) ? null : Identifier.tryParse(templateName));
    }

    public void setTemplateName(@Nullable Identifier templateName) {
        this.templateName = templateName;
    }

    public void setAuthor(LivingEntity entity) {
        this.author = entity.getStringifiedName();
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public void setOffset(BlockPos offset) {
        this.offset = offset;
    }

    public Vec3i getSize() {
        return this.size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
    }

    public BlockMirror getMirror() {
        return this.mirror;
    }

    public void setMirror(BlockMirror mirror) {
        this.mirror = mirror;
    }

    public BlockRotation getRotation() {
        return this.rotation;
    }

    public void setRotation(BlockRotation rotation) {
        this.rotation = rotation;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public StructureBlockMode getMode() {
        return this.mode;
    }

    public void setMode(StructureBlockMode mode) {
        this.mode = mode;
        BlockState blockState = this.world.getBlockState(this.getPos());
        if (blockState.isOf(Blocks.STRUCTURE_BLOCK)) {
            this.world.setBlockState(this.getPos(), (BlockState)blockState.with(StructureBlock.MODE, mode), 2);
        }
    }

    public boolean shouldIgnoreEntities() {
        return this.ignoreEntities;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public void setIgnoreEntities(boolean ignoreEntities) {
        this.ignoreEntities = ignoreEntities;
    }

    public void setStrict(boolean bl) {
        this.strict = bl;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float integrity) {
        this.integrity = integrity;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public boolean detectStructureSize() {
        if (this.mode != StructureBlockMode.SAVE) {
            return false;
        }
        BlockPos blockPos = this.getPos();
        int i = 80;
        BlockPos blockPos2 = new BlockPos(blockPos.getX() - 80, this.world.getBottomY(), blockPos.getZ() - 80);
        BlockPos blockPos3 = new BlockPos(blockPos.getX() + 80, this.world.getTopYInclusive(), blockPos.getZ() + 80);
        Stream<BlockPos> stream = this.streamCornerPos(blockPos2, blockPos3);
        return StructureBlockBlockEntity.getStructureBox(blockPos, stream).filter(box -> {
            int i = box.getMaxX() - box.getMinX();
            int j = box.getMaxY() - box.getMinY();
            int k = box.getMaxZ() - box.getMinZ();
            if (i > 1 && j > 1 && k > 1) {
                this.offset = new BlockPos(box.getMinX() - blockPos.getX() + 1, box.getMinY() - blockPos.getY() + 1, box.getMinZ() - blockPos.getZ() + 1);
                this.size = new Vec3i(i - 1, j - 1, k - 1);
                this.markDirty();
                BlockState blockState = this.world.getBlockState(blockPos);
                this.world.updateListeners(blockPos, blockState, blockState, 3);
                return true;
            }
            return false;
        }).isPresent();
    }

    private Stream<BlockPos> streamCornerPos(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end).filter(pos -> this.world.getBlockState((BlockPos)pos).isOf(Blocks.STRUCTURE_BLOCK)).map(this.world::getBlockEntity).filter(blockEntity -> blockEntity instanceof StructureBlockBlockEntity).map(blockEntity -> (StructureBlockBlockEntity)blockEntity).filter(blockEntity -> blockEntity.mode == StructureBlockMode.CORNER && Objects.equals(this.templateName, blockEntity.templateName)).map(BlockEntity::getPos);
    }

    private static Optional<BlockBox> getStructureBox(BlockPos pos, Stream<BlockPos> corners) {
        Iterator iterator = corners.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        BlockPos blockPos = (BlockPos)iterator.next();
        BlockBox blockBox = new BlockBox(blockPos);
        if (iterator.hasNext()) {
            iterator.forEachRemaining(blockBox::encompass);
        } else {
            blockBox.encompass(pos);
        }
        return Optional.of(blockBox);
    }

    public boolean saveStructure() {
        if (this.mode != StructureBlockMode.SAVE) {
            return false;
        }
        return this.saveStructure(true);
    }

    public boolean saveStructure(boolean toDisk) {
        World world;
        if (this.templateName == null || !((world = this.world) instanceof ServerWorld)) {
            return false;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        BlockPos blockPos = this.getPos().add(this.offset);
        return StructureBlockBlockEntity.saveStructure(serverWorld, this.templateName, blockPos, this.size, this.ignoreEntities, this.author, toDisk, List.of());
    }

    public static boolean saveStructure(ServerWorld world, Identifier templateId, BlockPos start, Vec3i size, boolean ignoreEntities, String author, boolean toDisk, List<Block> list) {
        StructureTemplate structureTemplate;
        StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
        try {
            structureTemplate = structureTemplateManager.getTemplateOrBlank(templateId);
        }
        catch (InvalidIdentifierException invalidIdentifierException) {
            return false;
        }
        structureTemplate.saveFromWorld(world, start, size, !ignoreEntities, Stream.concat(list.stream(), Stream.of(Blocks.STRUCTURE_VOID)).toList());
        structureTemplate.setAuthor(author);
        if (toDisk) {
            try {
                return structureTemplateManager.saveTemplate(templateId);
            }
            catch (InvalidIdentifierException invalidIdentifierException) {
                return false;
            }
        }
        return true;
    }

    public static Random createRandom(long seed) {
        if (seed == 0L) {
            return Random.create(Util.getMeasuringTimeMs());
        }
        return Random.create(seed);
    }

    public boolean loadAndTryPlaceStructure(ServerWorld world) {
        if (this.mode != StructureBlockMode.LOAD || this.templateName == null) {
            return false;
        }
        StructureTemplate structureTemplate = world.getStructureTemplateManager().getTemplate(this.templateName).orElse(null);
        if (structureTemplate == null) {
            return false;
        }
        if (structureTemplate.getSize().equals(this.size)) {
            this.loadAndPlaceStructure(world, structureTemplate);
            return true;
        }
        this.loadStructure(structureTemplate);
        return false;
    }

    public boolean loadStructure(ServerWorld world) {
        StructureTemplate structureTemplate = this.getStructureTemplate(world);
        if (structureTemplate == null) {
            return false;
        }
        this.loadStructure(structureTemplate);
        return true;
    }

    private void loadStructure(StructureTemplate template) {
        this.author = !StringHelper.isEmpty(template.getAuthor()) ? template.getAuthor() : "";
        this.size = template.getSize();
        this.markDirty();
    }

    public void loadAndPlaceStructure(ServerWorld world) {
        StructureTemplate structureTemplate = this.getStructureTemplate(world);
        if (structureTemplate != null) {
            this.loadAndPlaceStructure(world, structureTemplate);
        }
    }

    private @Nullable StructureTemplate getStructureTemplate(ServerWorld world) {
        if (this.templateName == null) {
            return null;
        }
        return world.getStructureTemplateManager().getTemplate(this.templateName).orElse(null);
    }

    private void loadAndPlaceStructure(ServerWorld world, StructureTemplate template) {
        this.loadStructure(template);
        StructurePlacementData structurePlacementData = new StructurePlacementData().setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setUpdateNeighbors(this.strict);
        if (this.integrity < 1.0f) {
            structurePlacementData.clearProcessors().addProcessor(new BlockRotStructureProcessor(MathHelper.clamp(this.integrity, 0.0f, 1.0f))).setRandom(StructureBlockBlockEntity.createRandom(this.seed));
        }
        BlockPos blockPos = this.getPos().add(this.offset);
        if (SharedConstants.STRUCTURE_EDIT_MODE) {
            BlockPos.iterate(blockPos, blockPos.add(this.size)).forEach(pos -> world.setBlockState((BlockPos)pos, Blocks.STRUCTURE_VOID.getDefaultState(), 2));
        }
        template.place(world, blockPos, blockPos, structurePlacementData, StructureBlockBlockEntity.createRandom(this.seed), 2 | (this.strict ? 816 : 0));
    }

    public void unloadStructure() {
        if (this.templateName == null) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)this.world;
        StructureTemplateManager structureTemplateManager = serverWorld.getStructureTemplateManager();
        structureTemplateManager.unloadTemplate(this.templateName);
    }

    public boolean isStructureAvailable() {
        if (this.mode != StructureBlockMode.LOAD || this.world.isClient() || this.templateName == null) {
            return false;
        }
        ServerWorld serverWorld = (ServerWorld)this.world;
        StructureTemplateManager structureTemplateManager = serverWorld.getStructureTemplateManager();
        try {
            return structureTemplateManager.getTemplate(this.templateName).isPresent();
        }
        catch (InvalidIdentifierException invalidIdentifierException) {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public boolean shouldShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean showAir) {
        this.showAir = showAir;
    }

    public boolean shouldShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean showBoundingBox) {
        this.showBoundingBox = showBoundingBox;
    }

    @Override
    public StructureBoxRendering.RenderMode getRenderMode() {
        if (this.mode != StructureBlockMode.SAVE && this.mode != StructureBlockMode.LOAD) {
            return StructureBoxRendering.RenderMode.NONE;
        }
        if (this.mode == StructureBlockMode.SAVE && this.showAir) {
            return StructureBoxRendering.RenderMode.BOX_AND_INVISIBLE_BLOCKS;
        }
        if (this.mode == StructureBlockMode.SAVE || this.showBoundingBox) {
            return StructureBoxRendering.RenderMode.BOX;
        }
        return StructureBoxRendering.RenderMode.NONE;
    }

    @Override
    public StructureBoxRendering.StructureBox getStructureBox() {
        int q;
        int p;
        int o;
        int m;
        BlockPos blockPos = this.getOffset();
        Vec3i vec3i = this.getSize();
        int i = blockPos.getX();
        int j = blockPos.getZ();
        int k = blockPos.getY();
        int l = k + vec3i.getY();
        return StructureBoxRendering.StructureBox.create(o, k, p, q, l, switch (this.rotation) {
            case BlockRotation.CLOCKWISE_90 -> {
                o = (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? i : i + 1;
                p = m < 0 ? j + 1 : j;
                q = o - (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
                yield p + m;
            }
            case BlockRotation.CLOCKWISE_180 -> {
                o = m < 0 ? i : i + 1;
                p = (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? j : j + 1;
                q = o - m;
                yield p - (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
            }
            case BlockRotation.COUNTERCLOCKWISE_90 -> {
                o = (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? i + 1 : i;
                p = m < 0 ? j : j + 1;
                q = o + (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
                yield p - m;
            }
            default -> {
                o = m < 0 ? i + 1 : i;
                p = (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? j + 1 : j;
                q = o + m;
                yield p + (switch (this.mirror) {
                    case BlockMirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case BlockMirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
            }
        });
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    public static final class Action
    extends Enum<Action> {
        public static final /* enum */ Action UPDATE_DATA = new Action();
        public static final /* enum */ Action SAVE_AREA = new Action();
        public static final /* enum */ Action LOAD_AREA = new Action();
        public static final /* enum */ Action SCAN_AREA = new Action();
        private static final /* synthetic */ Action[] field_12107;

        public static Action[] values() {
            return (Action[])field_12107.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private static /* synthetic */ Action[] method_36718() {
            return new Action[]{UPDATE_DATA, SAVE_AREA, LOAD_AREA, SCAN_AREA};
        }

        static {
            field_12107 = Action.method_36718();
        }
    }
}

