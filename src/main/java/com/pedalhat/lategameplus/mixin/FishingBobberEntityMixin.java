package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.registry.ModItems;
import com.pedalhat.lategameplus.mixinutil.LGPLavaImmuneItemEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
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

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
    @Unique
    private static final RegistryKey<LootTable> LATEGAMEPLUS$LAVA_FISHING_LOOT =
        RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(LateGamePlus.MOD_ID, "gameplay/fishing/lava"));

    @Unique
    private static final RegistryKey<LootTable> LATEGAMEPLUS$LAVA_FISHING_LOOT_OVERWORLD =
        RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(LateGamePlus.MOD_ID, "gameplay/fishing/lava_overworld"));

    @Unique
    private static final RegistryKey<LootTable> LATEGAMEPLUS$LAVA_FISHING_LOOT_NETHER =
        RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(LateGamePlus.MOD_ID, "gameplay/fishing/lava_nether"));

    @Unique
    private boolean lategameplus$lavaFishing;

    @Unique
    private RegistryKey<LootTable> lategameplus$selectedLavaLoot = LATEGAMEPLUS$LAVA_FISHING_LOOT;

    @Shadow
    @Final
    private int waitTimeReductionTicks;

    @Shadow
    private int hookCountdown;

    @Shadow
    private boolean caughtFish;

    @Unique
    private boolean lategameplus$autoReelTriggered;

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"
        )
    )
    private boolean lategameplus$allowLavaFishing(FluidState state, TagKey<Fluid> tag) {
        if (state.isIn(tag)) {
            return true;
        }
        return tag.equals(FluidTags.WATER) && this.lategameplus$canFishInLava() && this.lategameplus$isInLava();
    }

    @Redirect(
        method = "removeIfInvalid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"
        )
    )
    private boolean lategameplus$allowNetheriteRod(ItemStack stack, Item item) {
        return stack.isOf(item) || stack.isOf(ModItems.NETHERITE_FISHING_ROD);
    }

    @Redirect(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/registry/ReloadableRegistries$Lookup;getLootTable(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/loot/LootTable;"
        )
    )
    private LootTable lategameplus$selectFishingLoot(
        ReloadableRegistries.Lookup lookup,
        RegistryKey<LootTable> key,
        ItemStack usedItem
    ) {
        if (usedItem.isOf(ModItems.NETHERITE_FISHING_ROD)
            && (this.lategameplus$lavaFishing || this.lategameplus$isInLava())) {
            RegistryKey<LootTable> tableKey = this.lategameplus$lavaFishing
                ? this.lategameplus$selectedLavaLoot
                : this.lategameplus$selectLavaLootTable(((FishingBobberEntity)(Object)this).getBlockPos());
            return lookup.getLootTable(tableKey);
        }
        return lookup.getLootTable(key);
    }

    @ModifyVariable(method = "use", at = @At(value = "STORE"), ordinal = 0)
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
            ((FishingBobberEntity)(Object)this).extinguish();
        }
    }

    @Redirect(
        method = "tickFishingLogic",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
        )
    )
    private boolean lategameplus$allowLavaParticles(BlockState state, Block block) {
        if (state.isOf(block)) {
            return true;
        }
        return block == Blocks.WATER && this.lategameplus$canFishInLava()
            && this.lategameplus$isInLava() && state.isOf(Blocks.LAVA);
    }

    @ModifyArg(
        method = "tickFishingLogic",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"
        ),
        index = 0
    )
    private ParticleEffect lategameplus$swapFishingParticles(ParticleEffect effect) {
        if (this.lategameplus$canFishInLava() && this.lategameplus$isInLava()) {
            return ParticleTypes.LAVA;
        }
        return effect;
    }

    @Inject(method = "tickFishingLogic", at = @At("HEAD"))
    private void lategameplus$trackLavaFishing(BlockPos pos, CallbackInfo ci) {
        if (this.lategameplus$canFishInLava() && this.lategameplus$isInLava()) {
            this.lategameplus$lavaFishing = true;
            this.lategameplus$selectedLavaLoot = this.lategameplus$selectLavaLootTable(pos);
        }
    }

    @Inject(method = "tickFishingLogic", at = @At("TAIL"))
    private void lategameplus$autoReelOnBite(BlockPos pos, CallbackInfo ci) {
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        if (self.getEntityWorld().isClient()) {
            return;
        }
        if (!this.caughtFish || this.hookCountdown <= 0) {
            this.lategameplus$autoReelTriggered = false;
            return;
        }
        if (this.lategameplus$autoReelTriggered) {
            return;
        }
        PlayerEntity owner = self.getPlayerOwner();
        if (owner == null) {
            return;
        }
        if (!owner.getMainHandStack().isOf(ModItems.NETHERITE_FISHING_ROD)) {
            return;
        }
        if (!owner.getOffHandStack().isOf(ModItems.POMPEII_WORM)) {
            return;
        }
        this.lategameplus$autoReelTriggered = true;
        this.lategameplus$autoReelAndRecast(owner);
    }

    @Redirect(
        method = "tickFishingLogic",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;nextInt(Lnet/minecraft/util/math/random/Random;II)I"
        )
    )
    private int lategameplus$scaleFishingCountdowns(Random random, int min, int max) {
        int value = MathHelper.nextInt(random, min, max);
        if (min == 20 && max == 80) {
            return lategameplus$applyFishingTimeMultiplier(value);
        }
        if (min == 100 && max == 600) {
            int scaled = lategameplus$applyFishingTimeMultiplier(value);
            return Math.max(scaled, this.waitTimeReductionTicks + 1);
        }
        return value;
    }

    @Unique
    private int lategameplus$applyFishingTimeMultiplier(int value) {
        float multiplier = ConfigManager.get().fishingTimeMultiplier;
        if (!Float.isFinite(multiplier) || multiplier <= 0f) {
            return value;
        }
        float clamped = MathHelper.clamp(multiplier, 0.25f, 4f);
        return Math.max(1, Math.round(value / clamped));
    }

    @Unique
    private boolean lategameplus$canFishInLava() {
        PlayerEntity owner = ((FishingBobberEntity)(Object)this).getPlayerOwner();
        if (owner == null) {
            return false;
        }
        return owner.getMainHandStack().isOf(ModItems.NETHERITE_FISHING_ROD)
            || owner.getOffHandStack().isOf(ModItems.NETHERITE_FISHING_ROD);
    }

    @Unique
    private RegistryKey<LootTable> lategameplus$selectLavaLootTable(BlockPos pos) {
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        if (self.getEntityWorld().getRegistryKey() == World.NETHER) {
            return this.lategameplus$isNetherLavaHotspot(pos)
                ? LATEGAMEPLUS$LAVA_FISHING_LOOT
                : LATEGAMEPLUS$LAVA_FISHING_LOOT_NETHER;
        }
        return LATEGAMEPLUS$LAVA_FISHING_LOOT_OVERWORLD;
    }

    @Unique
    private void lategameplus$autoReelAndRecast(PlayerEntity owner) {
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        World world = self.getEntityWorld();
        ItemStack rodStack = owner.getMainHandStack();
        int damage = self.use(rodStack);
        rodStack.damage(damage, (LivingEntity)owner, Hand.MAIN_HAND.getEquipmentSlot());
        world.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
            SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL,
            1.0f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        rodStack.emitUseGameEvent(owner, GameEvent.ITEM_INTERACT_FINISH);

        ItemStack baitStack = owner.getOffHandStack();
        if (baitStack.isOf(ModItems.POMPEII_WORM) && world.getRandom().nextFloat() < 0.5f) {
            baitStack.decrement(1);
        }
        if (!owner.getOffHandStack().isOf(ModItems.POMPEII_WORM)) {
            return;
        }
        world.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
            SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.NEUTRAL,
            0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        if (world instanceof ServerWorld serverWorld) {
            int reduction = (int)(EnchantmentHelper.getFishingTimeReduction(serverWorld, rodStack, owner) * 20.0f);
            int luck = EnchantmentHelper.getFishingLuckBonus(serverWorld, rodStack, owner);
            ProjectileEntity.spawn(new FishingBobberEntity(owner, world, luck, reduction), serverWorld, rodStack);
        }
        owner.incrementStat(Stats.USED.getOrCreateStat(rodStack.getItem()));
        rodStack.emitUseGameEvent(owner, GameEvent.ITEM_INTERACT_START);
    }

    @Unique
    private boolean lategameplus$isNetherLavaHotspot(BlockPos pos) {
        int y = pos.getY();
        if (y < 28 || y > 32) {
            return false;
        }
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        World world = self.getEntityWorld();
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        int sourceCount = 0;
        for (int dy = -1; dy <= 0; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dx = -2; dx <= 2; dx++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    FluidState fluidState = world.getFluidState(cursor);
                    if (fluidState.isIn(FluidTags.LAVA) && fluidState.isStill()) {
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
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        return self.getEntityWorld().getFluidState(self.getBlockPos()).isIn(FluidTags.LAVA) || self.isInLava();
    }
}
