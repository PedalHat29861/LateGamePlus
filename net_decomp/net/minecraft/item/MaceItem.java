/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class MaceItem
extends Item {
    private static final int ATTACK_DAMAGE_MODIFIER_VALUE = 5;
    private static final float ATTACK_SPEED_MODIFIER_VALUE = -3.4f;
    public static final float MINING_SPEED_MULTIPLIER = 1.5f;
    private static final float HEAVY_SMASH_SOUND_FALL_DISTANCE_THRESHOLD = 5.0f;
    public static final float KNOCKBACK_RANGE = 3.5f;
    private static final float KNOCKBACK_POWER = 0.7f;

    public MaceItem(Item.Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder().add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.4f, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build();
    }

    public static ToolComponent createToolComponent() {
        return new ToolComponent(List.of(), 1.0f, 2, false);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (MaceItem.shouldDealAdditionalDamage(attacker)) {
            ServerPlayerEntity serverPlayerEntity;
            ServerWorld serverWorld = (ServerWorld)attacker.getEntityWorld();
            attacker.setVelocity(attacker.getVelocity().withAxis(Direction.Axis.Y, 0.01f));
            if (attacker instanceof ServerPlayerEntity) {
                serverPlayerEntity = (ServerPlayerEntity)attacker;
                serverPlayerEntity.currentExplosionImpactPos = this.getCurrentExplosionImpactPos(serverPlayerEntity);
                serverPlayerEntity.setIgnoreFallDamageFromCurrentExplosion(true);
                serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
            }
            if (target.isOnGround()) {
                if (attacker instanceof ServerPlayerEntity) {
                    serverPlayerEntity = (ServerPlayerEntity)attacker;
                    serverPlayerEntity.setSpawnExtraParticlesOnFall(true);
                }
                SoundEvent soundEvent = attacker.fallDistance > 5.0 ? SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY : SoundEvents.ITEM_MACE_SMASH_GROUND;
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), soundEvent, attacker.getSoundCategory(), 1.0f, 1.0f);
            } else {
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ITEM_MACE_SMASH_AIR, attacker.getSoundCategory(), 1.0f, 1.0f);
            }
            MaceItem.knockbackNearbyEntities(serverWorld, attacker, target);
        }
    }

    private Vec3d getCurrentExplosionImpactPos(ServerPlayerEntity player) {
        if (player.shouldIgnoreFallDamageFromCurrentExplosion() && player.currentExplosionImpactPos != null && player.currentExplosionImpactPos.y <= player.getEntityPos().y) {
            return player.currentExplosionImpactPos;
        }
        return player.getEntityPos();
    }

    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (MaceItem.shouldDealAdditionalDamage(attacker)) {
            attacker.onLanding();
        }
    }

    @Override
    public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource) {
        Entity entity = damageSource.getSource();
        if (!(entity instanceof LivingEntity)) {
            return 0.0f;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (!MaceItem.shouldDealAdditionalDamage(livingEntity)) {
            return 0.0f;
        }
        double d = 3.0;
        double e = 8.0;
        double f = livingEntity.fallDistance;
        double g = f <= 3.0 ? 4.0 * f : (f <= 8.0 ? 12.0 + 2.0 * (f - 3.0) : 22.0 + f - 8.0);
        World world = livingEntity.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            return (float)(g + (double)EnchantmentHelper.getSmashDamagePerFallenBlock(serverWorld, livingEntity.getWeaponStack(), target, damageSource, 0.0f) * f);
        }
        return (float)g;
    }

    private static void knockbackNearbyEntities(World world, Entity attacker, Entity attacked) {
        world.syncWorldEvent(2013, attacked.getSteppingPos(), 750);
        world.getEntitiesByClass(LivingEntity.class, attacked.getBoundingBox().expand(3.5), MaceItem.getKnockbackPredicate(attacker, attacked)).forEach(entity -> {
            Vec3d vec3d = entity.getEntityPos().subtract(attacked.getEntityPos());
            double d = MaceItem.getKnockback(attacker, entity, vec3d);
            Vec3d vec3d2 = vec3d.normalize().multiply(d);
            if (d > 0.0) {
                entity.addVelocity(vec3d2.x, 0.7f, vec3d2.z);
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                    serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
                }
            }
        });
    }

    private static Predicate<LivingEntity> getKnockbackPredicate(Entity attacker, Entity attacked) {
        return arg_0 -> MaceItem.method_58661(attacker, attacked, arg_0);
    }

    private static double getKnockback(Entity attacker, LivingEntity attacked, Vec3d distance) {
        return (3.5 - distance.length()) * (double)0.7f * (double)(attacker.fallDistance > 5.0 ? 2 : 1) * (1.0 - attacked.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean shouldDealAdditionalDamage(LivingEntity attacker) {
        return attacker.fallDistance > 1.5 && !attacker.isGliding();
    }

    @Override
    public @Nullable DamageSource getDamageSource(LivingEntity user) {
        if (MaceItem.shouldDealAdditionalDamage(user)) {
            return user.getDamageSources().maceSmash(user);
        }
        return super.getDamageSource(user);
    }

    /*
     * Unable to fully structure code
     */
    private static /* synthetic */ boolean method_58661(Entity entity2, Entity entity3, LivingEntity entity) {
        bl = entity.isSpectator() == false;
        bl2 = entity != entity2 && entity != entity3;
        v0 = bl3 = entity2.isTeammate(entity) == false;
        if (!(entity instanceof TameableEntity)) ** GOTO lbl-1000
        tameableEntity = (TameableEntity)entity;
        if (!(entity3 instanceof LivingEntity)) ** GOTO lbl-1000
        livingEntity = (LivingEntity)entity3;
        if (tameableEntity.isTamed() && tameableEntity.isOwner(livingEntity)) {
            v1 = true;
        } else lbl-1000:
        // 3 sources

        {
            v1 = false;
        }
        bl4 = v1 == false;
        bl5 = entity instanceof ArmorStandEntity == false || (armorStandEntity = (ArmorStandEntity)entity).isMarker() == false;
        bl6 = entity3.squaredDistanceTo(entity) <= Math.pow(3.5, 2.0);
        bl7 = (entity instanceof PlayerEntity != false && (playerEntity = (PlayerEntity)entity).isCreative() != false && playerEntity.getAbilities().flying != false) == false;
        return bl != false && bl2 != false && bl3 != false && bl4 != false && bl5 != false && bl6 != false && bl7 != false;
    }
}

