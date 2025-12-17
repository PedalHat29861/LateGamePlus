/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractCowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.EffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class MooshroomEntity
extends AbstractCowEntity
implements Shearable {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(MooshroomEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int MUTATION_CHANCE = 1024;
    private static final String STEW_EFFECTS_NBT_KEY = "stew_effects";
    private @Nullable SuspiciousStewEffectsComponent stewEffects;
    private @Nullable UUID lightningId;

    public MooshroomEntity(EntityType<? extends MooshroomEntity> entityType, World world) {
        super((EntityType<? extends AbstractCowEntity>)entityType, world);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getBlockState(pos.down()).isOf(Blocks.MYCELIUM)) {
            return 10.0f;
        }
        return world.getPhototaxisFavor(pos);
    }

    public static boolean canSpawn(EntityType<MooshroomEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && MooshroomEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        UUID uUID = lightning.getUuid();
        if (!uUID.equals(this.lightningId)) {
            this.setVariant(this.getVariant() == Variant.RED ? Variant.BROWN : Variant.RED);
            this.lightningId = uUID;
            this.playSound(SoundEvents.ENTITY_MOOSHROOM_CONVERT, 2.0f, 1.0f);
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variant.DEFAULT.index);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.BOWL) && !this.isBaby()) {
            ItemStack itemStack2;
            boolean bl = false;
            if (this.stewEffects != null) {
                bl = true;
                itemStack2 = new ItemStack(Items.SUSPICIOUS_STEW);
                itemStack2.set(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
                this.stewEffects = null;
            } else {
                itemStack2 = new ItemStack(Items.MUSHROOM_STEW);
            }
            ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, player, itemStack2, false);
            player.setStackInHand(hand, itemStack3);
            SoundEvent soundEvent = bl ? SoundEvents.ENTITY_MOOSHROOM_SUSPICIOUS_MILK : SoundEvents.ENTITY_MOOSHROOM_MILK;
            this.playSound(soundEvent, 1.0f, 1.0f);
            return ActionResult.SUCCESS;
        }
        if (itemStack.isOf(Items.SHEARS) && this.isShearable()) {
            World bl = this.getEntityWorld();
            if (bl instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)bl;
                this.sheared(serverWorld, SoundCategory.PLAYERS, itemStack);
                this.emitGameEvent(GameEvent.SHEAR, player);
                itemStack.damage(1, (LivingEntity)player, hand.getEquipmentSlot());
            }
            return ActionResult.SUCCESS;
        }
        if (this.getVariant() == Variant.BROWN) {
            Optional<SuspiciousStewEffectsComponent> optional = this.getStewEffectFrom(itemStack);
            if (optional.isEmpty()) {
                return super.interactMob(player, hand);
            }
            if (this.stewEffects != null) {
                for (int i = 0; i < 2; ++i) {
                    this.getEntityWorld().addParticleClient(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0, this.getBodyY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
            } else {
                itemStack.decrementUnlessCreative(1, player);
                EffectParticleEffect effectParticleEffect = EffectParticleEffect.of(ParticleTypes.EFFECT, -1, 1.0f);
                for (int j = 0; j < 4; ++j) {
                    this.getEntityWorld().addParticleClient(effectParticleEffect, this.getX() + this.random.nextDouble() / 2.0, this.getBodyY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
                this.stewEffects = optional.get();
                this.playSound(SoundEvents.ENTITY_MOOSHROOM_EAT, 2.0f, 1.0f);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity(null, this, SoundEvents.ENTITY_MOOSHROOM_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
        this.convertTo(EntityType.COW, EntityConversionContext.create(this, false, false), cow -> {
            world.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            this.forEachShearedItem(world, LootTables.MOOSHROOM_SHEARING, shears, (worldx, stack) -> {
                for (int i = 0; i < stack.getCount(); ++i) {
                    worldx.spawnEntity(new ItemEntity(this.getEntityWorld(), this.getX(), this.getBodyY(1.0), this.getZ(), stack.copyWithCount(1)));
                }
            });
        });
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("Type", Variant.CODEC, this.getVariant());
        view.putNullable(STEW_EFFECTS_NBT_KEY, SuspiciousStewEffectsComponent.CODEC, this.stewEffects);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.read("Type", Variant.CODEC).orElse(Variant.DEFAULT));
        this.stewEffects = view.read(STEW_EFFECTS_NBT_KEY, SuspiciousStewEffectsComponent.CODEC).orElse(null);
    }

    private Optional<SuspiciousStewEffectsComponent> getStewEffectFrom(ItemStack flower) {
        SuspiciousStewIngredient suspiciousStewIngredient = SuspiciousStewIngredient.of(flower.getItem());
        if (suspiciousStewIngredient != null) {
            return Optional.of(suspiciousStewIngredient.getStewEffects());
        }
        return Optional.empty();
    }

    private void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.index);
    }

    public Variant getVariant() {
        return Variant.fromIndex(this.dataTracker.get(VARIANT));
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.MOOSHROOM_VARIANT) {
            return MooshroomEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.MOOSHROOM_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.MOOSHROOM_VARIANT) {
            this.setVariant(MooshroomEntity.castComponentValue(DataComponentTypes.MOOSHROOM_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public @Nullable MooshroomEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        MooshroomEntity mooshroomEntity = EntityType.MOOSHROOM.create(serverWorld, SpawnReason.BREEDING);
        if (mooshroomEntity != null) {
            mooshroomEntity.setVariant(this.chooseBabyVariant((MooshroomEntity)passiveEntity));
        }
        return mooshroomEntity;
    }

    private Variant chooseBabyVariant(MooshroomEntity mooshroom) {
        Variant variant2;
        Variant variant = this.getVariant();
        Variant variant3 = variant == (variant2 = mooshroom.getVariant()) && this.random.nextInt(1024) == 0 ? (variant == Variant.BROWN ? Variant.RED : Variant.BROWN) : (this.random.nextBoolean() ? variant : variant2);
        return variant3;
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    public static final class Variant
    extends Enum<Variant>
    implements StringIdentifiable {
        public static final /* enum */ Variant RED = new Variant("red", 0, Blocks.RED_MUSHROOM.getDefaultState());
        public static final /* enum */ Variant BROWN = new Variant("brown", 1, Blocks.BROWN_MUSHROOM.getDefaultState());
        public static final Variant DEFAULT;
        public static final Codec<Variant> CODEC;
        private static final IntFunction<Variant> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        private final String name;
        final int index;
        private final BlockState mushroom;
        private static final /* synthetic */ Variant[] field_18113;

        public static Variant[] values() {
            return (Variant[])field_18113.clone();
        }

        public static Variant valueOf(String string) {
            return Enum.valueOf(Variant.class, string);
        }

        private Variant(String name, int index, BlockState mushroom) {
            this.name = name;
            this.index = index;
            this.mushroom = mushroom;
        }

        public BlockState getMushroomState() {
            return this.mushroom;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private int getIndex() {
            return this.index;
        }

        static Variant fromIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        private static /* synthetic */ Variant[] method_36639() {
            return new Variant[]{RED, BROWN};
        }

        static {
            field_18113 = Variant.method_36639();
            DEFAULT = RED;
            CODEC = StringIdentifiable.createCodec(Variant::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.CLAMP);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Variant::getIndex);
        }
    }
}

