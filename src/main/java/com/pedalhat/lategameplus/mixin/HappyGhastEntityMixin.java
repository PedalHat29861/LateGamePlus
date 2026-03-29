package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.mixinutil.LGPChestedGhast;
import com.pedalhat.lategameplus.mixinutil.LGPChestedGhastInternal;
import com.pedalhat.lategameplus.tag.LGPItemTags;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhast.class)
public abstract class HappyGhastEntityMixin extends Animal implements LGPChestedGhast, LGPChestedGhastInternal {
    @Unique
    private static final EntityDataAccessor<Integer> LATEGAMEPLUS$CHEST_COUNT =
        SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.INT);

    @Unique
    private final NonNullList<ItemStack> lategameplus$chestInventory =
        NonNullList.withSize(54, ItemStack.EMPTY);

    @Unique
    private int lategameplus$chestCount = 0;

    protected HappyGhastEntityMixin(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void lategameplus$trackChests(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(LATEGAMEPLUS$CHEST_COUNT, 0);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void lategameplus$readStorage(ValueInput view, CallbackInfo ci) {
        this.lategameplus$setChestCount(view.getIntOr("lategameplus_chests", 0));
        view.child("lategameplus_storage")
            .ifPresent(storage -> ContainerHelper.loadAllItems(storage, this.lategameplus$chestInventory));
        this.getEntityData().set(LATEGAMEPLUS$CHEST_COUNT, this.lategameplus$chestCount);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void lategameplus$writeStorage(ValueOutput view, CallbackInfo ci) {
        view.putInt("lategameplus_chests", this.lategameplus$chestCount);
        ContainerHelper.saveAllItems(view.child("lategameplus_storage"), this.lategameplus$chestInventory);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$applyNetheriteHarnessBuffs(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            this.lategameplus$chestCount = Mth.clamp(this.getEntityData().get(LATEGAMEPLUS$CHEST_COUNT), 0, 2);
            return;
        }

        ItemStack harness = this.getItemBySlot(EquipmentSlot.BODY);
        boolean hasHarness = harness.is(LGPItemTags.NETHERITE_HARNESSES);
        if (!hasHarness && (this.lategameplus$chestCount > 0 || !this.lategameplus$isStorageEmpty())) {
            this.lategameplus$dropStoredItems();
            this.lategameplus$setChestCount(0);
        }

        if (!hasHarness) {
            return;
        }

        MobEffectInstance current = this.getEffect(MobEffects.FIRE_RESISTANCE);
        if (current == null || current.getDuration() <= 40) {
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, true, false, true));
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void lategameplus$handleChestInteraction(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        boolean hasHarness = this.getItemBySlot(EquipmentSlot.BODY).is(LGPItemTags.NETHERITE_HARNESSES);

        if (!hasHarness || this.isBaby()) {
            return;
        }

        boolean sneaking = player.isShiftKeyDown();
        if (sneaking && this.lategameplus$chestCount > 0) {
            if (!this.level().isClientSide()) {
                this.lategameplus$openStorage(player);
            }
            cir.setReturnValue(this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
            return;
        }

        if (stack.is(Items.CHEST) && this.lategameplus$chestCount < 2) {
            if (!this.level().isClientSide()) {
                this.lategameplus$setChestCount(this.lategameplus$chestCount + 1);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.DONKEY_CHEST, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            cir.setReturnValue(this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
        }
    }

    @Unique
    private boolean lategameplus$isStorageEmpty() {
        for (ItemStack stack : this.lategameplus$chestInventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private void lategameplus$setChestCount(int count) {
        this.lategameplus$chestCount = Mth.clamp(count, 0, 2);
        if (!this.level().isClientSide()) {
            this.getEntityData().set(LATEGAMEPLUS$CHEST_COUNT, this.lategameplus$chestCount);
        }
    }

    @Unique
    private void lategameplus$dropStoredItems() {
        for (int i = 0; i < this.lategameplus$chestInventory.size(); i++) {
            ItemStack stack = this.lategameplus$chestInventory.get(i);
            if (stack.isEmpty()) continue;
            if (this.level() instanceof ServerLevel serverWorld) {
                this.spawnAtLocation(serverWorld, stack);
            }
            this.lategameplus$chestInventory.set(i, ItemStack.EMPTY);
        }
    }

    @Unique
    private void lategameplus$openStorage(Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        int slots = this.lategameplus$chestCount == 1 ? 27 : 54;
        Container view = this.lategameplus$wrapInventory(slots);
        Component fallback = this.lategameplus$chestCount == 1
            ? Component.translatable("container.chest")
            : Component.translatable("container.chestDouble");
        Component title = this.hasCustomName() ? this.getDisplayName() : fallback;

        serverPlayer.openMenu(new SimpleMenuProvider(
            (syncId, playerInventory, ignoredPlayer) -> this.lategameplus$chestCount == 1
                ? ChestMenu.threeRows(syncId, playerInventory, view)
                : ChestMenu.sixRows(syncId, playerInventory, view),
            title
        ));
    }

    @Unique
    private Container lategameplus$wrapInventory(final int viewSize) {
        final HappyGhast self = (HappyGhast)(Object)this;
        return new Container() {
            @Override
            public int getContainerSize() {
                return viewSize;
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < viewSize; i++) {
                    if (!lategameplus$chestInventory.get(i).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public ItemStack getItem(int slot) {
                return lategameplus$chestInventory.get(slot);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack split = ContainerHelper.removeItem(lategameplus$chestInventory, slot, amount);
                if (!split.isEmpty()) {
                    this.setChanged();
                }
                return split;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack removed = ContainerHelper.takeItem(lategameplus$chestInventory, slot);
                if (!removed.isEmpty()) {
                    this.setChanged();
                }
                return removed;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                lategameplus$chestInventory.set(slot, stack);
                if (stack.getCount() > stack.getMaxStackSize()) {
                    stack.setCount(stack.getMaxStackSize());
                }
                this.setChanged();
            }

            @Override
            public void setChanged() {
                // No-op; storage is bound to the entity and persisted via writeCustomData.
            }

            @Override
            public boolean stillValid(Player player) {
                return self.isAlive() && player.distanceToSqr(self) <= 64.0;
            }

            @Override
            public void clearContent() {
                for (int i = 0; i < viewSize; i++) {
                    lategameplus$chestInventory.set(i, ItemStack.EMPTY);
                }
            }
        };
    }

    @Override
    public int lategameplus$getChestCount() {
        return this.lategameplus$chestCount;
    }

    @Override
    public void lategameplus$dropStorageContents() {
        if (this.lategameplus$chestCount > 0 || !this.lategameplus$isStorageEmpty()) {
            this.lategameplus$dropStoredItems();
            this.lategameplus$setChestCount(0);
        }
    }
}
