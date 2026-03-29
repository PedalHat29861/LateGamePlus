package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.registry.ModEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityLavaTravelMixin {
    private static float LAVA_TRAVEL_MULTIPLIER_TIER1 = 3.0f;
    private static float LAVA_TRAVEL_MULTIPLIER_TIER2 = 6.0f;
    private static float LAVA_VERTICAL_MULTIPLIER_TIER1 = 1.0f;
    private static float LAVA_VERTICAL_MULTIPLIER_TIER2 = 1.1f;
    private static float LAVA_SINK_MULTIPLIER_TIER1 = 1.8f;
    private static float LAVA_SINK_MULTIPLIER_TIER2 = 2.6f;
    private static float LAVA_SWIM_UPWARD_MULTIPLIER_TIER1 = 1.8f;
    private static float LAVA_SWIM_UPWARD_MULTIPLIER_TIER2 = 2.6f;
    private double lategameplus$lastLavaInputY;
    @Shadow
    protected boolean jumping;

    @Inject(method = "travelInLava", at = @At("HEAD"))
    private void lategameplus$captureLavaInput(Vec3 movementInput, double gravity, boolean falling, double y, CallbackInfo ci) {
        this.lategameplus$lastLavaInputY = movementInput.y;
        if (this.jumping && this.lategameplus$lastLavaInputY <= 0.0) {
            this.lategameplus$lastLavaInputY = 1.0;
        }
    }

    @ModifyConstant(method = "travelInLava", constant = @Constant(floatValue = 0.02f))
    private float lategameplus$boostLavaTravel(float original) {
        MobEffectInstance effect = ((LivingEntity) (Object) this).getEffect(ModEffects.VOLCANIC_INFUSION);
        if (effect == null) {
            return original;
        }
        return original * (effect.getAmplifier() >= 1 ? LAVA_TRAVEL_MULTIPLIER_TIER2 : LAVA_TRAVEL_MULTIPLIER_TIER1);
    }

    @ModifyArgs(
        method = "travelInLava",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;")
    )
    private void lategameplus$boostLavaVerticalShallow(Args args) {
        MobEffectInstance effect = ((LivingEntity) (Object) this).getEffect(ModEffects.VOLCANIC_INFUSION);
        if (effect == null) {
            return;
        }
        if (this.lategameplus$lastLavaInputY <= 0.0) {
            return;
        }
        if (((LivingEntity) (Object) this).getDeltaMovement().y <= 0.0) {
            return;
        }
        double yMultiplier = (double) args.get(1);
        float multiplier = effect.getAmplifier() >= 1 ? LAVA_VERTICAL_MULTIPLIER_TIER2 : LAVA_VERTICAL_MULTIPLIER_TIER1;
        args.set(1, yMultiplier * multiplier);
    }

    @ModifyArgs(
        method = "travelInLava",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;")
    )
    private void lategameplus$boostLavaSink(Args args) {
        MobEffectInstance effect = ((LivingEntity) (Object) this).getEffect(ModEffects.VOLCANIC_INFUSION);
        if (effect == null) {
            return;
        }
        if (this.lategameplus$lastLavaInputY > 0.0) {
            return;
        }
        if (((LivingEntity) (Object) this).getDeltaMovement().y > 0.0) {
            return;
        }
        double yValue = (double) args.get(1);
        float multiplier = effect.getAmplifier() >= 1 ? LAVA_SINK_MULTIPLIER_TIER2 : LAVA_SINK_MULTIPLIER_TIER1;
        args.set(1, yValue * multiplier);
    }

    @ModifyArgs(
        method = "jumpInLiquid",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;")
    )
    private void lategameplus$boostLavaSwimUpward(Args args, TagKey<Fluid> fluid) {
        if (!FluidTags.LAVA.equals(fluid)) {
            return;
        }
        MobEffectInstance effect = ((LivingEntity) (Object) this).getEffect(ModEffects.VOLCANIC_INFUSION);
        if (effect == null) {
            return;
        }
        double yValue = (double) args.get(1);
        float multiplier = effect.getAmplifier() >= 1 ? LAVA_SWIM_UPWARD_MULTIPLIER_TIER2 : LAVA_SWIM_UPWARD_MULTIPLIER_TIER1;
        args.set(1, yValue * multiplier);
    }

    @Redirect(
        method = "travelInLava",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 lategameplus$boostLavaVerticalDeep(Vec3 velocity, double value) {
        MobEffectInstance effect = ((LivingEntity) (Object) this).getEffect(ModEffects.VOLCANIC_INFUSION);
        if (effect == null) {
            return velocity.scale(value);
        }
        double baseY = velocity.y * value;
        if (baseY <= 0.0 || this.lategameplus$lastLavaInputY <= 0.0) {
            return new Vec3(velocity.x * value, baseY, velocity.z * value);
        }
        float multiplier = effect.getAmplifier() >= 1 ? LAVA_VERTICAL_MULTIPLIER_TIER2 : LAVA_VERTICAL_MULTIPLIER_TIER1;
        return new Vec3(velocity.x * value, baseY * multiplier, velocity.z * value);
    }
}
