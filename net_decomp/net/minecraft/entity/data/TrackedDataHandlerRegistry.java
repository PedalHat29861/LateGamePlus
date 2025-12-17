/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.data;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.mob.ZombieNautilusVariant;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.ChickenVariant;
import net.minecraft.entity.passive.CopperGolemState;
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.passive.WolfSoundVariant;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.VillagerData;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class TrackedDataHandlerRegistry {
    private static final Int2ObjectBiMap<TrackedDataHandler<?>> DATA_HANDLERS = Int2ObjectBiMap.create(16);
    public static final TrackedDataHandler<Byte> BYTE = TrackedDataHandler.create(PacketCodecs.BYTE);
    public static final TrackedDataHandler<Integer> INTEGER = TrackedDataHandler.create(PacketCodecs.VAR_INT);
    public static final TrackedDataHandler<Long> LONG = TrackedDataHandler.create(PacketCodecs.VAR_LONG);
    public static final TrackedDataHandler<Float> FLOAT = TrackedDataHandler.create(PacketCodecs.FLOAT);
    public static final TrackedDataHandler<String> STRING = TrackedDataHandler.create(PacketCodecs.STRING);
    public static final TrackedDataHandler<Text> TEXT_COMPONENT = TrackedDataHandler.create(TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC);
    public static final TrackedDataHandler<Optional<Text>> OPTIONAL_TEXT_COMPONENT = TrackedDataHandler.create(TextCodecs.OPTIONAL_UNLIMITED_REGISTRY_PACKET_CODEC);
    public static final TrackedDataHandler<ItemStack> ITEM_STACK = new TrackedDataHandler<ItemStack>(){

        @Override
        public PacketCodec<? super RegistryByteBuf, ItemStack> codec() {
            return ItemStack.OPTIONAL_PACKET_CODEC;
        }

        @Override
        public ItemStack copy(ItemStack itemStack) {
            return itemStack.copy();
        }

        @Override
        public /* synthetic */ Object copy(Object object) {
            return this.copy((ItemStack)object);
        }
    };
    public static final TrackedDataHandler<BlockState> BLOCK_STATE = TrackedDataHandler.create(PacketCodecs.entryOf(Block.STATE_IDS));
    private static final PacketCodec<ByteBuf, Optional<BlockState>> OPTIONAL_BLOCK_STATE_CODEC = new PacketCodec<ByteBuf, Optional<BlockState>>(){

        @Override
        public void encode(ByteBuf byteBuf, Optional<BlockState> optional) {
            if (optional.isPresent()) {
                VarInts.write(byteBuf, Block.getRawIdFromState(optional.get()));
            } else {
                VarInts.write(byteBuf, 0);
            }
        }

        @Override
        public Optional<BlockState> decode(ByteBuf byteBuf) {
            int i = VarInts.read(byteBuf);
            if (i == 0) {
                return Optional.empty();
            }
            return Optional.of(Block.getStateFromRawId(i));
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Optional)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final TrackedDataHandler<Optional<BlockState>> OPTIONAL_BLOCK_STATE = TrackedDataHandler.create(OPTIONAL_BLOCK_STATE_CODEC);
    public static final TrackedDataHandler<Boolean> BOOLEAN = TrackedDataHandler.create(PacketCodecs.BOOLEAN);
    public static final TrackedDataHandler<ParticleEffect> PARTICLE = TrackedDataHandler.create(ParticleTypes.PACKET_CODEC);
    public static final TrackedDataHandler<List<ParticleEffect>> PARTICLE_LIST = TrackedDataHandler.create(ParticleTypes.PACKET_CODEC.collect(PacketCodecs.toList()));
    public static final TrackedDataHandler<EulerAngle> ROTATION = TrackedDataHandler.create(EulerAngle.PACKET_CODEC);
    public static final TrackedDataHandler<BlockPos> BLOCK_POS = TrackedDataHandler.create(BlockPos.PACKET_CODEC);
    public static final TrackedDataHandler<Optional<BlockPos>> OPTIONAL_BLOCK_POS = TrackedDataHandler.create(BlockPos.PACKET_CODEC.collect(PacketCodecs::optional));
    public static final TrackedDataHandler<Direction> FACING = TrackedDataHandler.create(Direction.PACKET_CODEC);
    public static final TrackedDataHandler<Optional<LazyEntityReference<LivingEntity>>> LAZY_ENTITY_REFERENCE = TrackedDataHandler.create(LazyEntityReference.createPacketCodec().collect(PacketCodecs::optional));
    public static final TrackedDataHandler<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = TrackedDataHandler.create(GlobalPos.PACKET_CODEC.collect(PacketCodecs::optional));
    public static final TrackedDataHandler<VillagerData> VILLAGER_DATA = TrackedDataHandler.create(VillagerData.PACKET_CODEC);
    private static final PacketCodec<ByteBuf, OptionalInt> OPTIONAL_INT_CODEC = new PacketCodec<ByteBuf, OptionalInt>(){

        @Override
        public OptionalInt decode(ByteBuf byteBuf) {
            int i = VarInts.read(byteBuf);
            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }

        @Override
        public void encode(ByteBuf byteBuf, OptionalInt optionalInt) {
            VarInts.write(byteBuf, optionalInt.orElse(-1) + 1);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (OptionalInt)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final TrackedDataHandler<OptionalInt> OPTIONAL_INT = TrackedDataHandler.create(OPTIONAL_INT_CODEC);
    public static final TrackedDataHandler<EntityPose> ENTITY_POSE = TrackedDataHandler.create(EntityPose.PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<CatVariant>> CAT_VARIANT = TrackedDataHandler.create(CatVariant.PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<ChickenVariant>> CHICKEN_VARIANT = TrackedDataHandler.create(ChickenVariant.ENTRY_PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<CowVariant>> COW_VARIANT = TrackedDataHandler.create(CowVariant.ENTRY_PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<WolfVariant>> WOLF_VARIANT = TrackedDataHandler.create(WolfVariant.ENTRY_PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<WolfSoundVariant>> WOLF_SOUND_VARIANT = TrackedDataHandler.create(WolfSoundVariant.PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<FrogVariant>> FROG_VARIANT = TrackedDataHandler.create(FrogVariant.PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<PigVariant>> PIG_VARIANT = TrackedDataHandler.create(PigVariant.ENTRY_PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<ZombieNautilusVariant>> ZOMBIE_NAUTILUS_VARIANT = TrackedDataHandler.create(ZombieNautilusVariant.ENTRY_PACKET_CODEC);
    public static final TrackedDataHandler<RegistryEntry<PaintingVariant>> PAINTING_VARIANT = TrackedDataHandler.create(PaintingVariant.ENTRY_PACKET_CODEC);
    public static final TrackedDataHandler<ArmadilloEntity.State> ARMADILLO_STATE = TrackedDataHandler.create(ArmadilloEntity.State.PACKET_CODEC);
    public static final TrackedDataHandler<SnifferEntity.State> SNIFFER_STATE = TrackedDataHandler.create(SnifferEntity.State.PACKET_CODEC);
    public static final TrackedDataHandler<Oxidizable.OxidationLevel> OXIDATION_LEVEL = TrackedDataHandler.create(Oxidizable.OxidationLevel.PACKET_CODEC);
    public static final TrackedDataHandler<CopperGolemState> COPPER_GOLEM_STATE = TrackedDataHandler.create(CopperGolemState.PACKET_CODEC);
    public static final TrackedDataHandler<Vector3fc> VECTOR_3F = TrackedDataHandler.create(PacketCodecs.VECTOR_3F);
    public static final TrackedDataHandler<Quaternionfc> QUATERNION_F = TrackedDataHandler.create(PacketCodecs.QUATERNION_F);
    public static final TrackedDataHandler<ProfileComponent> PROFILE = TrackedDataHandler.create(ProfileComponent.PACKET_CODEC);
    public static final TrackedDataHandler<Arm> ARM = TrackedDataHandler.create(Arm.PACKET_CODEC);

    public static void register(TrackedDataHandler<?> handler) {
        DATA_HANDLERS.add(handler);
    }

    public static @Nullable TrackedDataHandler<?> get(int id) {
        return DATA_HANDLERS.get(id);
    }

    public static int getId(TrackedDataHandler<?> handler) {
        return DATA_HANDLERS.getRawId(handler);
    }

    private TrackedDataHandlerRegistry() {
    }

    static {
        TrackedDataHandlerRegistry.register(BYTE);
        TrackedDataHandlerRegistry.register(INTEGER);
        TrackedDataHandlerRegistry.register(LONG);
        TrackedDataHandlerRegistry.register(FLOAT);
        TrackedDataHandlerRegistry.register(STRING);
        TrackedDataHandlerRegistry.register(TEXT_COMPONENT);
        TrackedDataHandlerRegistry.register(OPTIONAL_TEXT_COMPONENT);
        TrackedDataHandlerRegistry.register(ITEM_STACK);
        TrackedDataHandlerRegistry.register(BOOLEAN);
        TrackedDataHandlerRegistry.register(ROTATION);
        TrackedDataHandlerRegistry.register(BLOCK_POS);
        TrackedDataHandlerRegistry.register(OPTIONAL_BLOCK_POS);
        TrackedDataHandlerRegistry.register(FACING);
        TrackedDataHandlerRegistry.register(LAZY_ENTITY_REFERENCE);
        TrackedDataHandlerRegistry.register(BLOCK_STATE);
        TrackedDataHandlerRegistry.register(OPTIONAL_BLOCK_STATE);
        TrackedDataHandlerRegistry.register(PARTICLE);
        TrackedDataHandlerRegistry.register(PARTICLE_LIST);
        TrackedDataHandlerRegistry.register(VILLAGER_DATA);
        TrackedDataHandlerRegistry.register(OPTIONAL_INT);
        TrackedDataHandlerRegistry.register(ENTITY_POSE);
        TrackedDataHandlerRegistry.register(CAT_VARIANT);
        TrackedDataHandlerRegistry.register(COW_VARIANT);
        TrackedDataHandlerRegistry.register(WOLF_VARIANT);
        TrackedDataHandlerRegistry.register(WOLF_SOUND_VARIANT);
        TrackedDataHandlerRegistry.register(FROG_VARIANT);
        TrackedDataHandlerRegistry.register(PIG_VARIANT);
        TrackedDataHandlerRegistry.register(CHICKEN_VARIANT);
        TrackedDataHandlerRegistry.register(ZOMBIE_NAUTILUS_VARIANT);
        TrackedDataHandlerRegistry.register(OPTIONAL_GLOBAL_POS);
        TrackedDataHandlerRegistry.register(PAINTING_VARIANT);
        TrackedDataHandlerRegistry.register(SNIFFER_STATE);
        TrackedDataHandlerRegistry.register(ARMADILLO_STATE);
        TrackedDataHandlerRegistry.register(COPPER_GOLEM_STATE);
        TrackedDataHandlerRegistry.register(OXIDATION_LEVEL);
        TrackedDataHandlerRegistry.register(VECTOR_3F);
        TrackedDataHandlerRegistry.register(QUATERNION_F);
        TrackedDataHandlerRegistry.register(PROFILE);
        TrackedDataHandlerRegistry.register(ARM);
    }
}

