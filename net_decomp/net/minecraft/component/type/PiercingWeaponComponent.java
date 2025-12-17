/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.RaycastContext;

public record PiercingWeaponComponent(boolean dealsKnockback, boolean dismounts, Optional<RegistryEntry<SoundEvent>> sound, Optional<RegistryEntry<SoundEvent>> hitSound) {
    public static final Codec<PiercingWeaponComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("deals_knockback", (Object)true).forGetter(PiercingWeaponComponent::dealsKnockback), (App)Codec.BOOL.optionalFieldOf("dismounts", (Object)false).forGetter(PiercingWeaponComponent::dismounts), (App)SoundEvent.ENTRY_CODEC.optionalFieldOf("sound").forGetter(PiercingWeaponComponent::sound), (App)SoundEvent.ENTRY_CODEC.optionalFieldOf("hit_sound").forGetter(PiercingWeaponComponent::hitSound)).apply((Applicative)instance, PiercingWeaponComponent::new));
    public static final PacketCodec<RegistryByteBuf, PiercingWeaponComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, PiercingWeaponComponent::dealsKnockback, PacketCodecs.BOOLEAN, PiercingWeaponComponent::dismounts, SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional), PiercingWeaponComponent::sound, SoundEvent.ENTRY_PACKET_CODEC.collect(PacketCodecs::optional), PiercingWeaponComponent::hitSound, PiercingWeaponComponent::new);

    public void playSound(Entity entity) {
        this.sound.ifPresent(sound -> entity.getEntityWorld().playSound(entity, entity.getX(), entity.getY(), entity.getZ(), (RegistryEntry<SoundEvent>)sound, entity.getSoundCategory(), 1.0f, 1.0f));
    }

    public void playHitSound(Entity entity) {
        this.hitSound.ifPresent(sound -> entity.getEntityWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), (RegistryEntry<SoundEvent>)sound, entity.getSoundCategory(), 1.0f, 1.0f));
    }

    public static boolean canHit(Entity attacker, Entity target) {
        if (target.isInvulnerable() || !target.isAlive()) {
            return false;
        }
        if (target instanceof InteractionEntity) {
            return true;
        }
        if (!target.canBeHitByProjectile()) {
            return false;
        }
        if (target instanceof PlayerEntity) {
            PlayerEntity playerEntity2;
            PlayerEntity playerEntity = (PlayerEntity)target;
            if (attacker instanceof PlayerEntity && !(playerEntity2 = (PlayerEntity)attacker).shouldDamagePlayer(playerEntity)) {
                return false;
            }
        }
        return !attacker.isConnectedThroughVehicle(target);
    }

    public void stab(LivingEntity attacker, EquipmentSlot slot) {
        float f = (float)attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        AttackRangeComponent attackRangeComponent = attacker.getAttackRange();
        boolean bl = false;
        for (EntityHitResult entityHitResult : (Collection)ProjectileUtil.collectPiercingCollisions(attacker, attackRangeComponent, target -> PiercingWeaponComponent.canHit(attacker, target), RaycastContext.ShapeType.COLLIDER).map(blockHit -> List.of(), entityHits -> entityHits)) {
            bl |= attacker.pierce(slot, entityHitResult.getEntity(), f, true, this.dealsKnockback, this.dismounts);
        }
        attacker.beforePlayerAttack();
        attacker.useAttackEnchantmentEffects();
        if (bl) {
            this.playHitSound(attacker);
        }
        this.playSound(attacker);
        attacker.swingHand(Hand.MAIN_HAND, false);
    }
}

