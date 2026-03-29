package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import com.pedalhat.lategameplus.mixinutil.LGPLavaImmuneItemEntity;
import com.pedalhat.lategameplus.mixinutil.AutoReelDamageContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FishingHook.class)
public class FishingBobberEntityMixin {
    @Unique
    private static final ResourceKey<LootTable> LATEGAMEPLUS$LAVA_FISHING_LOOT =
        ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "gameplay/fishing/lava"));

    @Unique
    private static final ResourceKey<LootTable> LATEGAMEPLUS$LAVA_FISHING_LOOT_OVERWORLD =
        ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "gameplay/fishing/lava_overworld"));

    @Unique
    private static final ResourceKey<LootTable> LATEGAMEPLUS$LAVA_FISHING_LOOT_NETHER =
        ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "gameplay/fishing/lava_nether"));

    @Unique
    private static final Identifier LATEGAMEPLUS$AUTO_REEL_ID =
        Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "auto_reel");

    @Unique
    private boolean lategameplus$lavaFishing;

    @Unique
    private ResourceKey<LootTable> lategameplus$selectedLavaLoot = LATEGAMEPLUS$LAVA_FISHING_LOOT;

    @Shadow
    @Final
    private int lureSpeed;

    @Shadow
    private int nibble;

    @Shadow
    private boolean biting;

    @Unique
    private boolean lategameplus$autoReelTriggered;

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"
        )
    )
    private boolean lategameplus$allowLavaFishing(FluidState state, TagKey<Fluid> tag) {
        if (state.is(tag)) {
            return true;
        }
        return tag.equals(FluidTags.WATER) && this.lategameplus$canFishInLava() && this.lategameplus$isInLava();
    }

    @Redirect(
        method = "shouldStopFishing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"
        )
    )
    private boolean lategameplus$allowNetheriteRod(ItemStack stack, Object item) {
        return (item instanceof Item typedItem && stack.is(typedItem)) || stack.is(ModItems.NETHERITE_FISHING_ROD);
    }

    @Redirect(
        method = "retrieve",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/ReloadableServerRegistries$Holder;getLootTable(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/level/storage/loot/LootTable;"
        )
    )
    private LootTable lategameplus$selectFishingLoot(
        ReloadableServerRegistries.Holder lookup,
        ResourceKey<LootTable> key,
        ItemStack usedItem
    ) {
        if (usedItem.is(ModItems.NETHERITE_FISHING_ROD)
            && (this.lategameplus$lavaFishing || this.lategameplus$isInLava())) {
            ResourceKey<LootTable> tableKey = this.lategameplus$lavaFishing
                ? this.lategameplus$selectedLavaLoot
                : this.lategameplus$selectLavaLootTable(((FishingHook)(Object)this).blockPosition());
            return lookup.getLootTable(tableKey);
        }
        return lookup.getLootTable(key);
    }

    @ModifyVariable(method = "retrieve", at = @At(value = "STORE"), ordinal = 0)
    private ItemEntity lategameplus$protectLavaCatch(ItemEntity itemEntity) {
        if (this.lategameplus$canFishInLava() && this.lategameplus$isInLava()) {
            if (itemEntity instanceof LGPLavaImmuneItemEntity lavaImmune) {
                lavaImmune.lategameplus$setLavaProtectionTicks(40);
            }
        }
        return itemEntity;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$extinguishInLava(CallbackInfo ci) {
        if (this.lategameplus$canFishInLava() && this.lategameplus$isInLava()) {
            ((FishingHook)(Object)this).clearFire();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$autoReelAndRecast(CallbackInfo ci) {
        FishingHook self = (FishingHook)(Object)this;
        if (self.level().isClientSide()) {
            return;
        }
        if (this.nibble <= 0 || !this.biting || this.lategameplus$autoReelTriggered) {
            return;
        }

        Player owner = self.getPlayerOwner();
        if (owner == null || owner.fishing != self) {
            return;
        }

        InteractionHand hand = this.lategameplus$getAutoReelHand(owner);
        if (hand == null) {
            return;
        }

        ItemStack rodStack = owner.getItemInHand(hand);
        int autoReelLevel = this.lategameplus$getAutoReelLevel(rodStack);
        if (autoReelLevel <= 0) {
            return;
        }

        this.lategameplus$autoReelTriggered = true;
        int extraDamage = this.lategameplus$getAutoReelExtraDamage(autoReelLevel);

        AutoReelDamageContext.setExtraDamage(extraDamage);
        try {
            rodStack.use(owner.level(), owner, hand);
        } finally {
            AutoReelDamageContext.clear();
        }

        if (!owner.isAlive() || owner.isRemoved()) {
            return;
        }

        ItemStack updatedRodStack = owner.getItemInHand(hand);
        if (updatedRodStack.isEmpty()
            || !updatedRodStack.is(ItemTags.FISHING_ENCHANTABLE)
            || owner.fishing != null) {
            return;
        }

        updatedRodStack.use(owner.level(), owner, hand);
    }

    @Redirect(
        method = "catchingFish",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
        )
    )
    private boolean lategameplus$allowLavaParticles(BlockState state, Object block) {
        if (block instanceof Block typedBlock && state.is(typedBlock)) {
            return true;
        }
        return block == Blocks.WATER && this.lategameplus$canFishInLava()
            && this.lategameplus$isInLava() && state.is(Blocks.LAVA);
    }

    @ModifyArg(
        method = "catchingFish",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
        ),
        index = 0
    )
    private ParticleOptions lategameplus$swapFishingParticles(ParticleOptions effect) {
        if (this.lategameplus$canFishInLava() && this.lategameplus$isInLava()) {
            return ParticleTypes.LAVA;
        }
        return effect;
    }

    @Inject(method = "catchingFish", at = @At("HEAD"))
    private void lategameplus$trackLavaFishing(BlockPos pos, CallbackInfo ci) {
        if (this.lategameplus$canFishInLava() && this.lategameplus$isInLava()) {
            this.lategameplus$lavaFishing = true;
            this.lategameplus$selectedLavaLoot = this.lategameplus$selectLavaLootTable(pos);
        }
    }

    @Redirect(
        method = "catchingFish",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"
        )
    )
    private int lategameplus$scaleFishingCountdowns(RandomSource random, int min, int max) {
        int value = Mth.nextInt(random, min, max);
        if (min == 20 && max == 80) {
            return lategameplus$applyFishingTimeMultiplier(value);
        }
        if (min == 100 && max == 600) {
            int scaled = lategameplus$applyFishingTimeMultiplier(value);
            return Math.max(scaled, this.lureSpeed + 1);
        }
        return value;
    }

    @Unique
    private int lategameplus$applyFishingTimeMultiplier(int value) {
        float multiplier = ConfigManager.get().fishingTimeMultiplier;
        if (!Float.isFinite(multiplier) || multiplier <= 0f) {
            return value;
        }
        float clamped = Mth.clamp(multiplier, 0.25f, 4f);
        return Math.max(1, Math.round(value / clamped));
    }

    @Unique
    private boolean lategameplus$canFishInLava() {
        Player owner = ((FishingHook)(Object)this).getPlayerOwner();
        if (owner == null) {
            return false;
        }
        return owner.getMainHandItem().is(ModItems.NETHERITE_FISHING_ROD)
            || owner.getOffhandItem().is(ModItems.NETHERITE_FISHING_ROD);
    }

    @Unique
    private ResourceKey<LootTable> lategameplus$selectLavaLootTable(BlockPos pos) {
        FishingHook self = (FishingHook)(Object)this;
        if (self.level().dimension() == Level.NETHER) {
            return this.lategameplus$isNetherLavaHotspot(pos)
                ? LATEGAMEPLUS$LAVA_FISHING_LOOT
                : LATEGAMEPLUS$LAVA_FISHING_LOOT_NETHER;
        }
        return LATEGAMEPLUS$LAVA_FISHING_LOOT_OVERWORLD;
    }

    @Unique
    private boolean lategameplus$isNetherLavaHotspot(BlockPos pos) {
        int y = pos.getY();
        if (y < 28 || y > 32) {
            return false;
        }
        FishingHook self = (FishingHook)(Object)this;
        Level world = self.level();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int sourceCount = 0;
        for (int dy = -1; dy <= 0; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dx = -2; dx <= 2; dx++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    FluidState fluidState = world.getFluidState(cursor);
                    if (fluidState.is(FluidTags.LAVA) && fluidState.isSource()) {
                        sourceCount++;
                        if (sourceCount > 20) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private boolean lategameplus$isInLava() {
        FishingHook self = (FishingHook)(Object)this;
        return self.level().getFluidState(self.blockPosition()).is(FluidTags.LAVA) || self.isInLava();
    }

    @Unique
    private InteractionHand lategameplus$getAutoReelHand(Player owner) {
        ItemStack main = owner.getMainHandItem();
        if (main.is(ItemTags.FISHING_ENCHANTABLE) && this.lategameplus$getAutoReelLevel(main) > 0) {
            return InteractionHand.MAIN_HAND;
        }

        ItemStack off = owner.getOffhandItem();
        if (off.is(ItemTags.FISHING_ENCHANTABLE) && this.lategameplus$getAutoReelLevel(off) > 0) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }

    @Unique
    private int lategameplus$getAutoReelLevel(ItemStack stack) {
        FishingHook self = (FishingHook)(Object)this;
        return self.level().registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .get(LATEGAMEPLUS$AUTO_REEL_ID)
            .map(entry -> EnchantmentHelper.getItemEnchantmentLevel(entry, stack))
            .orElse(0);
    }

    @Unique
    private int lategameplus$getAutoReelExtraDamage(int level) {
        return Math.max(1, 6 - Math.max(1, level));
    }
}
