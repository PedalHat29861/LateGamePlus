package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.registry.ModItems;
import com.pedalhat.lategameplus.mixinutil.LGPLavaImmuneItemEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
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
import org.spongepowered.asm.mixin.Mixin;
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
        if (usedItem.isOf(ModItems.NETHERITE_FISHING_ROD) && this.lategameplus$isInLava()) {
            return lookup.getLootTable(LATEGAMEPLUS$LAVA_FISHING_LOOT);
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
    private boolean lategameplus$isInLava() {
        FishingBobberEntity self = (FishingBobberEntity)(Object)this;
        return self.getEntityWorld().getFluidState(self.getBlockPos()).isIn(FluidTags.LAVA) || self.isInLava();
    }
}
