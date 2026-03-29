package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfEntityMixin extends TamableAnimal {
    private static final int FIRE_RESISTANCE_REFRESH_TICKS = 40;
    private static final int FIRE_RESISTANCE_DURATION_TICKS = 120;

    protected WolfEntityMixin(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "canArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void lategameplus$allowNetheriteWolfArmor(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        ItemStack armor = this.getBodyArmorItem();
        if (armor.is(ModItems.NETHERITE_WOLF_ARMOR)) {
            cir.setReturnValue(!source.is(DamageTypeTags.BYPASSES_WOLF_ARMOR));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lategameplus$applyNetheriteWolfArmorBonuses(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack armor = this.getBodyArmorItem();
        if (!armor.is(ModItems.NETHERITE_WOLF_ARMOR)) {
            return;
        }
        MobEffectInstance active = this.getEffect(MobEffects.FIRE_RESISTANCE);
        if (active == null || active.getDuration() <= FIRE_RESISTANCE_REFRESH_TICKS) {
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION_TICKS, 0, false, false, true));
        }
    }
}
