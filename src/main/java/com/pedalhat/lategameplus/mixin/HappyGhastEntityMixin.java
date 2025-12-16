package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.tag.LGPItemTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HappyGhastEntity.class)
public abstract class HappyGhastEntityMixin extends AnimalEntity {
    protected HappyGhastEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$applyNetheriteHarnessBuffs(CallbackInfo ci) {
        if (this.getEntityWorld().isClient()) return;

        ItemStack harness = this.getEquippedStack(EquipmentSlot.BODY);
        if (!harness.isIn(LGPItemTags.NETHERITE_HARNESSES)) return;

        StatusEffectInstance current = this.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
        if (current == null || current.getDuration() <= 40) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 120, 0, true, false, true));
        }
    }
}
