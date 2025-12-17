package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntity.class)
public abstract class WolfEntityMixin extends TameableEntity {
    private static final int FIRE_RESISTANCE_REFRESH_TICKS = 40;
    private static final int FIRE_RESISTANCE_DURATION_TICKS = 120;

    protected WolfEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "shouldArmorAbsorbDamage", at = @At("HEAD"), cancellable = true)
    private void lategameplus$allowNetheriteWolfArmor(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        ItemStack armor = this.getBodyArmor();
        if (armor.isOf(ModItems.NETHERITE_WOLF_ARMOR)) {
            cir.setReturnValue(!source.isIn(DamageTypeTags.BYPASSES_WOLF_ARMOR));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$applyNetheriteWolfArmorBonuses(CallbackInfo ci) {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        ItemStack armor = this.getBodyArmor();
        if (!armor.isOf(ModItems.NETHERITE_WOLF_ARMOR)) {
            return;
        }
        StatusEffectInstance active = this.getStatusEffect(StatusEffects.FIRE_RESISTANCE);
        if (active == null || active.getDuration() <= FIRE_RESISTANCE_REFRESH_TICKS) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION_TICKS, 0, false, false, true));
        }
    }
}
