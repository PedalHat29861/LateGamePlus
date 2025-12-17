/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.damage;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class DamageSource {
    private final RegistryEntry<DamageType> type;
    private final @Nullable Entity attacker;
    private final @Nullable Entity source;
    private final @Nullable Vec3d position;

    public String toString() {
        return "DamageSource (" + this.getType().msgId() + ")";
    }

    public float getExhaustion() {
        return this.getType().exhaustion();
    }

    public boolean isDirect() {
        return this.attacker == this.source;
    }

    private DamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, @Nullable Vec3d position) {
        this.type = type;
        this.attacker = attacker;
        this.source = source;
        this.position = position;
    }

    public DamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker) {
        this(type, source, attacker, null);
    }

    public DamageSource(RegistryEntry<DamageType> type, Vec3d position) {
        this(type, null, null, position);
    }

    public DamageSource(RegistryEntry<DamageType> type, @Nullable Entity attacker) {
        this(type, attacker, attacker);
    }

    public DamageSource(RegistryEntry<DamageType> type) {
        this(type, null, null, null);
    }

    public @Nullable Entity getSource() {
        return this.source;
    }

    public @Nullable Entity getAttacker() {
        return this.attacker;
    }

    public @Nullable ItemStack getWeaponStack() {
        return this.source != null ? this.source.getWeaponStack() : null;
    }

    public Text getDeathMessage(LivingEntity killed) {
        String string = "death.attack." + this.getType().msgId();
        if (this.attacker != null || this.source != null) {
            ItemStack itemStack;
            Text text = this.attacker == null ? this.source.getDisplayName() : this.attacker.getDisplayName();
            Entity entity = this.attacker;
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                v0 = livingEntity.getMainHandStack();
            } else {
                v0 = itemStack = ItemStack.EMPTY;
            }
            if (!itemStack.isEmpty() && itemStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                return Text.translatable(string + ".item", killed.getDisplayName(), text, itemStack.toHoverableText());
            }
            return Text.translatable(string, killed.getDisplayName(), text);
        }
        LivingEntity livingEntity2 = killed.getPrimeAdversary();
        String string2 = string + ".player";
        if (livingEntity2 != null) {
            return Text.translatable(string2, killed.getDisplayName(), livingEntity2.getDisplayName());
        }
        return Text.translatable(string, killed.getDisplayName());
    }

    public String getName() {
        return this.getType().msgId();
    }

    public boolean isScaledWithDifficulty() {
        return switch (this.getType().scaling()) {
            default -> throw new MatchException(null, null);
            case DamageScaling.NEVER -> false;
            case DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER -> {
                if (this.attacker instanceof LivingEntity && !(this.attacker instanceof PlayerEntity)) {
                    yield true;
                }
                yield false;
            }
            case DamageScaling.ALWAYS -> true;
        };
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isSourceCreativePlayer() {
        Entity entity = this.getAttacker();
        if (!(entity instanceof PlayerEntity)) return false;
        PlayerEntity playerEntity = (PlayerEntity)entity;
        if (!playerEntity.getAbilities().creativeMode) return false;
        return true;
    }

    public @Nullable Vec3d getPosition() {
        if (this.position != null) {
            return this.position;
        }
        if (this.source != null) {
            return this.source.getEntityPos();
        }
        return null;
    }

    public @Nullable Vec3d getStoredPosition() {
        return this.position;
    }

    public boolean isIn(TagKey<DamageType> tag) {
        return this.type.isIn(tag);
    }

    public boolean isOf(RegistryKey<DamageType> typeKey) {
        return this.type.matchesKey(typeKey);
    }

    public DamageType getType() {
        return this.type.value();
    }

    public RegistryEntry<DamageType> getTypeRegistryEntry() {
        return this.type;
    }
}

