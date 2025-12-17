/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeamEmitter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.entity.StructureBoxRendering;
import net.minecraft.data.DataWriter;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.TestCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTestState;
import net.minecraft.test.RuntimeTestInstances;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestInstanceUtil;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestRunContext;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.path.PathUtil;
import net.minecraft.world.World;

public class TestInstanceBlockEntity
extends BlockEntity
implements BeamEmitter,
StructureBoxRendering {
    private static final Text INVALID_TEST_TEXT = Text.translatable("test_instance_block.invalid_test");
    private static final List<BeamEmitter.BeamSegment> CLEARED_BEAM_SEGMENTS = List.of();
    private static final List<BeamEmitter.BeamSegment> RUNNING_BEAM_SEGMENTS = List.of(new BeamEmitter.BeamSegment(ColorHelper.getArgb(128, 128, 128)));
    private static final List<BeamEmitter.BeamSegment> SUCCESS_BEAM_SEGMENTS = List.of(new BeamEmitter.BeamSegment(ColorHelper.getArgb(0, 255, 0)));
    private static final List<BeamEmitter.BeamSegment> REQUIRED_FAIL_BEAM_SEGMENTS = List.of(new BeamEmitter.BeamSegment(ColorHelper.getArgb(255, 0, 0)));
    private static final List<BeamEmitter.BeamSegment> OPTIONAL_FAIL_BEAM_SEGMENTS = List.of(new BeamEmitter.BeamSegment(ColorHelper.getArgb(255, 128, 0)));
    private static final Vec3i STRUCTURE_OFFSET = new Vec3i(0, 1, 1);
    private Data data;
    private final List<Error> errors = new ArrayList<Error>();

    public TestInstanceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.TEST_INSTANCE_BLOCK, pos, state);
        this.data = new Data(Optional.empty(), Vec3i.ZERO, BlockRotation.NONE, false, Status.CLEARED, Optional.empty());
    }

    public void setData(Data data) {
        this.data = data;
        this.markDirty();
    }

    public static Optional<Vec3i> getStructureSize(ServerWorld world, RegistryKey<TestInstance> testInstance) {
        return TestInstanceBlockEntity.getStructureTemplate(world, testInstance).map(StructureTemplate::getSize);
    }

    public BlockBox getBlockBox() {
        BlockPos blockPos = this.getStructurePos();
        BlockPos blockPos2 = blockPos.add(this.getTransformedSize()).add(-1, -1, -1);
        return BlockBox.create(blockPos, blockPos2);
    }

    public Box getBox() {
        return Box.from(this.getBlockBox());
    }

    private static Optional<StructureTemplate> getStructureTemplate(ServerWorld world, RegistryKey<TestInstance> testInstance) {
        return world.getRegistryManager().getOptionalEntry(testInstance).map(entry -> ((TestInstance)entry.value()).getStructure()).flatMap(structureId -> world.getStructureTemplateManager().getTemplate((Identifier)structureId));
    }

    public Optional<RegistryKey<TestInstance>> getTestKey() {
        return this.data.test();
    }

    public Text getTestName() {
        return this.getTestKey().map(key -> Text.literal(key.getValue().toString())).orElse(INVALID_TEST_TEXT);
    }

    private Optional<RegistryEntry.Reference<TestInstance>> getTestEntry() {
        return this.getTestKey().flatMap(this.world.getRegistryManager()::getOptionalEntry);
    }

    public boolean shouldIgnoreEntities() {
        return this.data.ignoreEntities();
    }

    public Vec3i getSize() {
        return this.data.size();
    }

    public BlockRotation getRotation() {
        return this.getTestEntry().map(RegistryEntry::value).map(TestInstance::getRotation).orElse(BlockRotation.NONE).rotate(this.data.rotation());
    }

    public Optional<Text> getErrorMessage() {
        return this.data.errorMessage();
    }

    public void setErrorMessage(Text errorMessage) {
        this.setData(this.data.withErrorMessage(errorMessage));
    }

    public void setFinished() {
        this.setData(this.data.withStatus(Status.FINISHED));
    }

    public void setRunning() {
        this.setData(this.data.withStatus(Status.RUNNING));
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world instanceof ServerWorld) {
            this.world.updateListeners(this.getPos(), Blocks.AIR.getDefaultState(), this.getCachedState(), 3);
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    @Override
    protected void readData(ReadView view) {
        view.read("data", Data.CODEC).ifPresent(this::setData);
        this.errors.clear();
        this.errors.addAll(view.read("errors", Error.LIST_CODEC).orElse(List.of()));
    }

    @Override
    protected void writeData(WriteView view) {
        view.put("data", Data.CODEC, this.data);
        if (!this.errors.isEmpty()) {
            view.put("errors", Error.LIST_CODEC, this.errors);
        }
    }

    @Override
    public StructureBoxRendering.RenderMode getRenderMode() {
        return StructureBoxRendering.RenderMode.BOX;
    }

    public BlockPos getStructurePos() {
        return TestInstanceBlockEntity.getStructurePos(this.getPos());
    }

    public static BlockPos getStructurePos(BlockPos pos) {
        return pos.add(STRUCTURE_OFFSET);
    }

    @Override
    public StructureBoxRendering.StructureBox getStructureBox() {
        return new StructureBoxRendering.StructureBox(new BlockPos(STRUCTURE_OFFSET), this.getTransformedSize());
    }

    @Override
    public List<BeamEmitter.BeamSegment> getBeamSegments() {
        return switch (this.data.status().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> CLEARED_BEAM_SEGMENTS;
            case 1 -> RUNNING_BEAM_SEGMENTS;
            case 2 -> this.getErrorMessage().isEmpty() ? SUCCESS_BEAM_SEGMENTS : (this.getTestEntry().map(RegistryEntry::value).map(TestInstance::isRequired).orElse(true) != false ? REQUIRED_FAIL_BEAM_SEGMENTS : OPTIONAL_FAIL_BEAM_SEGMENTS);
        };
    }

    private Vec3i getTransformedSize() {
        Vec3i vec3i = this.getSize();
        BlockRotation blockRotation = this.getRotation();
        boolean bl = blockRotation == BlockRotation.CLOCKWISE_90 || blockRotation == BlockRotation.COUNTERCLOCKWISE_90;
        int i = bl ? vec3i.getZ() : vec3i.getX();
        int j = bl ? vec3i.getX() : vec3i.getZ();
        return new Vec3i(i, vec3i.getY(), j);
    }

    public void reset(Consumer<Text> messageConsumer) {
        this.clearBarriers();
        this.clearErrors();
        boolean bl = this.placeStructure();
        if (bl) {
            messageConsumer.accept(Text.translatable("test_instance_block.reset_success", this.getTestName()).formatted(Formatting.GREEN));
        }
        this.setData(this.data.withStatus(Status.CLEARED));
    }

    public Optional<Identifier> saveStructure(Consumer<Text> messageConsumer) {
        Optional<RegistryEntry.Reference<TestInstance>> optional = this.getTestEntry();
        Optional<Identifier> optional2 = optional.isPresent() ? Optional.of(optional.get().value().getStructure()) : this.getTestKey().map(RegistryKey::getValue);
        if (optional2.isEmpty()) {
            BlockPos blockPos = this.getPos();
            messageConsumer.accept(Text.translatable("test_instance_block.error.unable_to_save", blockPos.getX(), blockPos.getY(), blockPos.getZ()).formatted(Formatting.RED));
            return optional2;
        }
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            StructureBlockBlockEntity.saveStructure(serverWorld, optional2.get(), this.getStructurePos(), this.getSize(), this.shouldIgnoreEntities(), "", true, List.of(Blocks.AIR));
        }
        return optional2;
    }

    public boolean export(Consumer<Text> messageConsumer) {
        World world;
        Optional<Identifier> optional = this.saveStructure(messageConsumer);
        if (optional.isEmpty() || !((world = this.world) instanceof ServerWorld)) {
            return false;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        return TestInstanceBlockEntity.exportData(serverWorld, optional.get(), messageConsumer);
    }

    public static boolean exportData(ServerWorld world, Identifier structureId, Consumer<Text> messageConsumer) {
        Path path = TestInstanceUtil.testStructuresDirectoryName;
        Path path2 = world.getStructureTemplateManager().getTemplatePath(structureId, ".nbt");
        Path path3 = NbtProvider.convertNbtToSnbt(DataWriter.UNCACHED, path2, structureId.getPath(), path.resolve(structureId.getNamespace()).resolve("structure"));
        if (path3 == null) {
            messageConsumer.accept(Text.literal("Failed to export " + String.valueOf(path2)).formatted(Formatting.RED));
            return true;
        }
        try {
            PathUtil.createDirectories(path3.getParent());
        }
        catch (IOException iOException) {
            messageConsumer.accept(Text.literal("Could not create folder " + String.valueOf(path3.getParent())).formatted(Formatting.RED));
            return true;
        }
        messageConsumer.accept(Text.literal("Exported " + String.valueOf(structureId) + " to " + String.valueOf(path3.toAbsolutePath())));
        return false;
    }

    public void start(Consumer<Text> messageConsumer) {
        World world = this.world;
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        Optional<RegistryEntry.Reference<TestInstance>> optional = this.getTestEntry();
        BlockPos blockPos = this.getPos();
        if (optional.isEmpty()) {
            messageConsumer.accept(Text.translatable("test_instance_block.error.no_test", blockPos.getX(), blockPos.getY(), blockPos.getZ()).formatted(Formatting.RED));
            return;
        }
        if (!this.placeStructure()) {
            messageConsumer.accept(Text.translatable("test_instance_block.error.no_test_structure", blockPos.getX(), blockPos.getY(), blockPos.getZ()).formatted(Formatting.RED));
            return;
        }
        this.clearErrors();
        TestManager.INSTANCE.clear();
        RuntimeTestInstances.clear();
        messageConsumer.accept(Text.translatable("test_instance_block.starting", optional.get().getIdAsString()));
        GameTestState gameTestState = new GameTestState(optional.get(), this.data.rotation(), serverWorld, TestAttemptConfig.once());
        gameTestState.setTestBlockPos(blockPos);
        TestRunContext testRunContext = TestRunContext.Builder.ofStates(List.of(gameTestState), serverWorld).build();
        TestCommand.start(serverWorld.getServer().getCommandSource(), testRunContext);
    }

    public boolean placeStructure() {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            Optional optional = this.data.test().flatMap(template -> TestInstanceBlockEntity.getStructureTemplate(serverWorld, template));
            if (optional.isPresent()) {
                this.placeStructure(serverWorld, (StructureTemplate)optional.get());
                return true;
            }
        }
        return false;
    }

    private void placeStructure(ServerWorld world, StructureTemplate template) {
        StructurePlacementData structurePlacementData = new StructurePlacementData().setRotation(this.getRotation()).setIgnoreEntities(this.data.ignoreEntities()).setUpdateNeighbors(true);
        BlockPos blockPos = this.getStartPos();
        this.setChunksForced();
        TestInstanceUtil.clearArea(this.getBlockBox(), world);
        this.discardEntities();
        template.place(world, blockPos, blockPos, structurePlacementData, world.getRandom(), 818);
    }

    private void discardEntities() {
        this.world.getOtherEntities(null, this.getBox()).stream().filter(entity -> !(entity instanceof PlayerEntity)).forEach(Entity::discard);
    }

    private void setChunksForced() {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.getBlockBox().streamChunkPos().forEach(pos -> serverWorld.setChunkForced(pos.x, pos.z, true));
        }
    }

    public BlockPos getStartPos() {
        Vec3i vec3i = this.getSize();
        BlockRotation blockRotation = this.getRotation();
        BlockPos blockPos = this.getStructurePos();
        return switch (blockRotation) {
            default -> throw new MatchException(null, null);
            case BlockRotation.NONE -> blockPos;
            case BlockRotation.CLOCKWISE_90 -> blockPos.add(vec3i.getZ() - 1, 0, 0);
            case BlockRotation.CLOCKWISE_180 -> blockPos.add(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
            case BlockRotation.COUNTERCLOCKWISE_90 -> blockPos.add(0, 0, vec3i.getX() - 1);
        };
    }

    public void placeBarriers() {
        this.forEachPos(pos -> {
            if (!this.world.getBlockState((BlockPos)pos).isOf(Blocks.TEST_INSTANCE_BLOCK)) {
                this.world.setBlockState((BlockPos)pos, Blocks.BARRIER.getDefaultState());
            }
        });
    }

    public void clearBarriers() {
        this.forEachPos(pos -> {
            if (this.world.getBlockState((BlockPos)pos).isOf(Blocks.BARRIER)) {
                this.world.setBlockState((BlockPos)pos, Blocks.AIR.getDefaultState());
            }
        });
    }

    public void forEachPos(Consumer<BlockPos> posConsumer) {
        Box box = this.getBox();
        boolean bl = this.getTestEntry().map(entry -> ((TestInstance)entry.value()).requiresSkyAccess()).orElse(false) == false;
        BlockPos blockPos = BlockPos.ofFloored(box.minX, box.minY, box.minZ).add(-1, -1, -1);
        BlockPos blockPos2 = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);
        BlockPos.stream(blockPos, blockPos2).forEach(pos -> {
            boolean bl3;
            boolean bl2 = pos.getX() == blockPos.getX() || pos.getX() == blockPos2.getX() || pos.getZ() == blockPos.getZ() || pos.getZ() == blockPos2.getZ() || pos.getY() == blockPos.getY();
            boolean bl4 = bl3 = pos.getY() == blockPos2.getY();
            if (bl2 || bl3 && bl) {
                posConsumer.accept((BlockPos)pos);
            }
        });
    }

    public void addError(BlockPos pos, Text message) {
        this.errors.add(new Error(pos, message));
        this.markDirty();
    }

    public void clearErrors() {
        if (!this.errors.isEmpty()) {
            this.errors.clear();
            this.markDirty();
        }
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    public record Data(Optional<RegistryKey<TestInstance>> test, Vec3i size, BlockRotation rotation, boolean ignoreEntities, Status status, Optional<Text> errorMessage) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryKey.createCodec(RegistryKeys.TEST_INSTANCE).optionalFieldOf("test").forGetter(Data::test), (App)Vec3i.CODEC.fieldOf("size").forGetter(Data::size), (App)BlockRotation.CODEC.fieldOf("rotation").forGetter(Data::rotation), (App)Codec.BOOL.fieldOf("ignore_entities").forGetter(Data::ignoreEntities), (App)Status.CODEC.fieldOf("status").forGetter(Data::status), (App)TextCodecs.CODEC.optionalFieldOf("error_message").forGetter(Data::errorMessage)).apply((Applicative)instance, Data::new));
        public static final PacketCodec<RegistryByteBuf, Data> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.optional(RegistryKey.createPacketCodec(RegistryKeys.TEST_INSTANCE)), Data::test, Vec3i.PACKET_CODEC, Data::size, BlockRotation.PACKET_CODEC, Data::rotation, PacketCodecs.BOOLEAN, Data::ignoreEntities, Status.PACKET_CODEC, Data::status, PacketCodecs.optional(TextCodecs.REGISTRY_PACKET_CODEC), Data::errorMessage, Data::new);

        public Data withSize(Vec3i size) {
            return new Data(this.test, size, this.rotation, this.ignoreEntities, this.status, this.errorMessage);
        }

        public Data withStatus(Status status) {
            return new Data(this.test, this.size, this.rotation, this.ignoreEntities, status, Optional.empty());
        }

        public Data withErrorMessage(Text errorMessage) {
            return new Data(this.test, this.size, this.rotation, this.ignoreEntities, Status.FINISHED, Optional.of(errorMessage));
        }
    }

    public static final class Status
    extends Enum<Status>
    implements StringIdentifiable {
        public static final /* enum */ Status CLEARED = new Status("cleared", 0);
        public static final /* enum */ Status RUNNING = new Status("running", 1);
        public static final /* enum */ Status FINISHED = new Status("finished", 2);
        private static final IntFunction<Status> INDEX_MAPPER;
        public static final Codec<Status> CODEC;
        public static final PacketCodec<ByteBuf, Status> PACKET_CODEC;
        private final String id;
        private final int index;
        private static final /* synthetic */ Status[] field_56022;

        public static Status[] values() {
            return (Status[])field_56022.clone();
        }

        public static Status valueOf(String string) {
            return Enum.valueOf(Status.class, string);
        }

        private Status(String id, int index) {
            this.id = id;
            this.index = index;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public static Status fromIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        private static /* synthetic */ Status[] method_66777() {
            return new Status[]{CLEARED, RUNNING, FINISHED};
        }

        static {
            field_56022 = Status.method_66777();
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(status -> status.index, Status.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = StringIdentifiable.createCodec(Status::values);
            PACKET_CODEC = PacketCodecs.indexed(Status::fromIndex, status -> status.index);
        }
    }

    public record Error(BlockPos pos, Text text) {
        public static final Codec<Error> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(Error::pos), (App)TextCodecs.CODEC.fieldOf("text").forGetter(Error::text)).apply((Applicative)instance, Error::new));
        public static final Codec<List<Error>> LIST_CODEC = CODEC.listOf();
    }
}

