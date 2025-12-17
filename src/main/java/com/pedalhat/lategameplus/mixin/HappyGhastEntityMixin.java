package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.mixinutil.LGPChestedGhast;
import com.pedalhat.lategameplus.mixinutil.LGPChestedGhastInternal;
import com.pedalhat.lategameplus.tag.LGPItemTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhastEntity.class)
public abstract class HappyGhastEntityMixin extends AnimalEntity implements LGPChestedGhast, LGPChestedGhastInternal {
    @Unique
    private static final TrackedData<Integer> LATEGAMEPLUS$CHEST_COUNT =
        DataTracker.registerData(HappyGhastEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private final DefaultedList<ItemStack> lategameplus$chestInventory =
        DefaultedList.ofSize(54, ItemStack.EMPTY);

    @Unique
    private int lategameplus$chestCount = 0;

    protected HappyGhastEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void lategameplus$trackChests(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(LATEGAMEPLUS$CHEST_COUNT, 0);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void lategameplus$readStorage(ReadView view, CallbackInfo ci) {
        this.lategameplus$setChestCount(view.getInt("lategameplus_chests", 0));
        view.getOptionalReadView("lategameplus_storage")
            .ifPresent(storage -> Inventories.readData(storage, this.lategameplus$chestInventory));
        this.getDataTracker().set(LATEGAMEPLUS$CHEST_COUNT, this.lategameplus$chestCount);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void lategameplus$writeStorage(WriteView view, CallbackInfo ci) {
        view.putInt("lategameplus_chests", this.lategameplus$chestCount);
        Inventories.writeData(view.get("lategameplus_storage"), this.lategameplus$chestInventory);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$applyNetheriteHarnessBuffs(CallbackInfo ci) {
        if (this.getEntityWorld().isClient()) {
            this.lategameplus$chestCount = MathHelper.clamp(this.getDataTracker().get(LATEGAMEPLUS$CHEST_COUNT), 0, 2);
            return;
        }

        ItemStack harness = this.getEquippedStack(EquipmentSlot.BODY);
        boolean hasHarness = harness.isIn(LGPItemTags.NETHERITE_HARNESSES);
        if (!hasHarness && (this.lategameplus$chestCount > 0 || !this.lategameplus$isStorageEmpty())) {
            this.lategameplus$dropStoredItems();
            this.lategameplus$setChestCount(0);
        }

        if (!hasHarness) {
            return;
        }

        StatusEffectInstance current = this.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
        if (current == null || current.getDuration() <= 40) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 120, 0, true, false, true));
        }
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void lategameplus$handleChestInteraction(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        boolean hasHarness = this.getEquippedStack(EquipmentSlot.BODY).isIn(LGPItemTags.NETHERITE_HARNESSES);

        if (!hasHarness || this.isBaby()) {
            return;
        }

        boolean sneaking = player.isSneaking();
        if (sneaking && this.lategameplus$chestCount > 0) {
            if (!this.getEntityWorld().isClient()) {
                this.lategameplus$openStorage(player);
            }
            cir.setReturnValue(this.getEntityWorld().isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER);
            return;
        }

        if (stack.isOf(Items.CHEST) && this.lategameplus$chestCount < 2) {
            if (!this.getEntityWorld().isClient()) {
                this.lategameplus$setChestCount(this.lategameplus$chestCount + 1);
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_DONKEY_CHEST, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            }
            cir.setReturnValue(this.getEntityWorld().isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER);
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
        this.lategameplus$chestCount = MathHelper.clamp(count, 0, 2);
        if (!this.getEntityWorld().isClient()) {
            this.getDataTracker().set(LATEGAMEPLUS$CHEST_COUNT, this.lategameplus$chestCount);
        }
    }

    @Unique
    private void lategameplus$dropStoredItems() {
        for (int i = 0; i < this.lategameplus$chestInventory.size(); i++) {
            ItemStack stack = this.lategameplus$chestInventory.get(i);
            if (stack.isEmpty()) continue;
            if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                this.dropStack(serverWorld, stack);
            }
            this.lategameplus$chestInventory.set(i, ItemStack.EMPTY);
        }
    }

    @Unique
    private void lategameplus$openStorage(PlayerEntity player) {
        if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer)) {
            return;
        }
        int slots = this.lategameplus$chestCount == 1 ? 27 : 54;
        Inventory view = this.lategameplus$wrapInventory(slots);
        Text fallback = this.lategameplus$chestCount == 1
            ? Text.translatable("container.chest")
            : Text.translatable("container.chestDouble");
        Text title = this.hasCustomName() ? this.getDisplayName() : fallback;

        serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, ignoredPlayer) -> this.lategameplus$chestCount == 1
                ? GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, view)
                : GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, view),
            title
        ));
    }

    @Unique
    private Inventory lategameplus$wrapInventory(final int viewSize) {
        final HappyGhastEntity self = (HappyGhastEntity)(Object)this;
        return new Inventory() {
            @Override
            public int size() {
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
            public ItemStack getStack(int slot) {
                return lategameplus$chestInventory.get(slot);
            }

            @Override
            public ItemStack removeStack(int slot, int amount) {
                ItemStack split = Inventories.splitStack(lategameplus$chestInventory, slot, amount);
                if (!split.isEmpty()) {
                    this.markDirty();
                }
                return split;
            }

            @Override
            public ItemStack removeStack(int slot) {
                ItemStack removed = Inventories.removeStack(lategameplus$chestInventory, slot);
                if (!removed.isEmpty()) {
                    this.markDirty();
                }
                return removed;
            }

            @Override
            public void setStack(int slot, ItemStack stack) {
                lategameplus$chestInventory.set(slot, stack);
                if (stack.getCount() > stack.getMaxCount()) {
                    stack.setCount(stack.getMaxCount());
                }
                this.markDirty();
            }

            @Override
            public void markDirty() {
                // No-op; storage is bound to the entity and persisted via writeCustomData.
            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return self.isAlive() && player.squaredDistanceTo(self) <= 64.0;
            }

            @Override
            public void clear() {
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
