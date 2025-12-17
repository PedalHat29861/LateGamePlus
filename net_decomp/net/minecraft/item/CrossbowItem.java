/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class CrossbowItem
extends RangedWeaponItem {
    private static final float DEFAULT_PULL_TIME = 1.25f;
    public static final int RANGE = 8;
    private boolean charged = false;
    private boolean loaded = false;
    private static final float CHARGE_PROGRESS = 0.2f;
    private static final float LOAD_PROGRESS = 0.5f;
    private static final float DEFAULT_SPEED = 3.15f;
    private static final float FIREWORK_ROCKET_SPEED = 1.6f;
    public static final float field_49258 = 1.6f;
    private static final LoadingSounds DEFAULT_LOADING_SOUNDS = new LoadingSounds(Optional.of(SoundEvents.ITEM_CROSSBOW_LOADING_START), Optional.of(SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.ITEM_CROSSBOW_LOADING_END));

    public CrossbowItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Predicate<ItemStack> getHeldProjectiles() {
        return CROSSBOW_HELD_PROJECTILES;
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        ChargedProjectilesComponent chargedProjectilesComponent = itemStack.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (chargedProjectilesComponent != null && !chargedProjectilesComponent.isEmpty()) {
            this.shootAll(world, user, hand, itemStack, CrossbowItem.getSpeed(chargedProjectilesComponent), 1.0f, null);
            return ActionResult.CONSUME;
        }
        if (!user.getProjectileType(itemStack).isEmpty()) {
            this.charged = false;
            this.loaded = false;
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }
        return ActionResult.FAIL;
    }

    private static float getSpeed(ChargedProjectilesComponent stack) {
        if (stack.contains(Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
        return CrossbowItem.getPullProgress(i, stack, user) >= 1.0f && CrossbowItem.isCharged(stack);
    }

    private static boolean loadProjectiles(LivingEntity shooter, ItemStack crossbow) {
        List<ItemStack> list = CrossbowItem.load(crossbow, shooter.getProjectileType(crossbow), shooter);
        if (!list.isEmpty()) {
            crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(list));
            return true;
        }
        return false;
    }

    public static boolean isCharged(ItemStack stack) {
        ChargedProjectilesComponent chargedProjectilesComponent = stack.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        return !chargedProjectilesComponent.isEmpty();
    }

    @Override
    protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target) {
        Vector3f vector3f;
        if (target != null) {
            double d = target.getX() - shooter.getX();
            double e = target.getZ() - shooter.getZ();
            double f = Math.sqrt(d * d + e * e);
            double g = target.getBodyY(0.3333333333333333) - projectile.getY() + f * (double)0.2f;
            vector3f = CrossbowItem.calcVelocity(shooter, new Vec3d(d, g, e), yaw);
        } else {
            Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(yaw * ((float)Math.PI / 180)), vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = shooter.getRotationVec(1.0f);
            vector3f = vec3d2.toVector3f().rotate((Quaternionfc)quaternionf);
        }
        projectile.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
        float h = CrossbowItem.getSoundPitch(shooter.getRandom(), index);
        shooter.getEntityWorld().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, shooter.getSoundCategory(), 1.0f, h);
    }

    private static Vector3f calcVelocity(LivingEntity shooter, Vec3d direction, float yaw) {
        Vector3f vector3f = direction.toVector3f().normalize();
        Vector3f vector3f2 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f));
        if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
            Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
            vector3f2 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)vec3d.toVector3f());
        }
        Vector3f vector3f3 = new Vector3f((Vector3fc)vector3f).rotateAxis(1.5707964f, vector3f2.x, vector3f2.y, vector3f2.z);
        return new Vector3f((Vector3fc)vector3f).rotateAxis(yaw * ((float)Math.PI / 180), vector3f3.x, vector3f3.y, vector3f3.z);
    }

    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        if (projectileStack.isOf(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(world, projectileStack, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15f, shooter.getZ(), true);
        }
        ProjectileEntity projectileEntity = super.createArrowEntity(world, shooter, weaponStack, projectileStack, critical);
        if (projectileEntity instanceof PersistentProjectileEntity) {
            PersistentProjectileEntity persistentProjectileEntity = (PersistentProjectileEntity)projectileEntity;
            persistentProjectileEntity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
        }
        return projectileEntity;
    }

    @Override
    protected int getWeaponStackDamage(ItemStack projectile) {
        return projectile.isOf(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    public void shootAll(World world, LivingEntity shooter, Hand hand, ItemStack stack, float speed, float divergence, @Nullable LivingEntity target) {
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        ChargedProjectilesComponent chargedProjectilesComponent = stack.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
        if (chargedProjectilesComponent == null || chargedProjectilesComponent.isEmpty()) {
            return;
        }
        this.shootAll(serverWorld, shooter, hand, stack, chargedProjectilesComponent.getProjectiles(), speed, divergence, shooter instanceof PlayerEntity, target);
        if (shooter instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)shooter;
            Criteria.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        }
    }

    private static float getSoundPitch(Random random, int index) {
        if (index == 0) {
            return 1.0f;
        }
        return CrossbowItem.getSoundPitch((index & 1) == 1, random);
    }

    private static float getSoundPitch(boolean flag, Random random) {
        float f = flag ? 0.63f : 0.43f;
        return 1.0f / (random.nextFloat() * 0.5f + 1.8f) + f;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient()) {
            LoadingSounds loadingSounds = this.getLoadingSounds(stack);
            float f = (float)(stack.getMaxUseTime(user) - remainingUseTicks) / (float)CrossbowItem.getPullTime(stack, user);
            if (f < 0.2f) {
                this.charged = false;
                this.loaded = false;
            }
            if (f >= 0.2f && !this.charged) {
                this.charged = true;
                loadingSounds.start().ifPresent(sound -> world.playSound(null, user.getX(), user.getY(), user.getZ(), (SoundEvent)sound.value(), SoundCategory.PLAYERS, 0.5f, 1.0f));
            }
            if (f >= 0.5f && !this.loaded) {
                this.loaded = true;
                loadingSounds.mid().ifPresent(sound -> world.playSound(null, user.getX(), user.getY(), user.getZ(), (SoundEvent)sound.value(), SoundCategory.PLAYERS, 0.5f, 1.0f));
            }
            if (f >= 1.0f && !CrossbowItem.isCharged(stack) && CrossbowItem.loadProjectiles(user, stack)) {
                loadingSounds.end().ifPresent(sound -> world.playSound(null, user.getX(), user.getY(), user.getZ(), (SoundEvent)sound.value(), user.getSoundCategory(), 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f));
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    public static int getPullTime(ItemStack stack, LivingEntity user) {
        float f = EnchantmentHelper.getCrossbowChargeTime(stack, user, 1.25f);
        return MathHelper.floor(f * 20.0f);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }

    LoadingSounds getLoadingSounds(ItemStack stack) {
        return EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.CROSSBOW_CHARGING_SOUNDS).orElse(DEFAULT_LOADING_SOUNDS);
    }

    private static float getPullProgress(int useTicks, ItemStack stack, LivingEntity user) {
        float f = (float)useTicks / (float)CrossbowItem.getPullTime(stack, user);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return stack.isOf(this);
    }

    @Override
    public int getRange() {
        return 8;
    }

    public record LoadingSounds(Optional<RegistryEntry<SoundEvent>> start, Optional<RegistryEntry<SoundEvent>> mid, Optional<RegistryEntry<SoundEvent>> end) {
        public static final Codec<LoadingSounds> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.ENTRY_CODEC.optionalFieldOf("start").forGetter(LoadingSounds::start), (App)SoundEvent.ENTRY_CODEC.optionalFieldOf("mid").forGetter(LoadingSounds::mid), (App)SoundEvent.ENTRY_CODEC.optionalFieldOf("end").forGetter(LoadingSounds::end)).apply((Applicative)instance, LoadingSounds::new));
    }

    public static final class ChargeType
    extends Enum<ChargeType>
    implements StringIdentifiable {
        public static final /* enum */ ChargeType NONE = new ChargeType("none");
        public static final /* enum */ ChargeType ARROW = new ChargeType("arrow");
        public static final /* enum */ ChargeType ROCKET = new ChargeType("rocket");
        public static final Codec<ChargeType> CODEC;
        private final String name;
        private static final /* synthetic */ ChargeType[] field_55211;

        public static ChargeType[] values() {
            return (ChargeType[])field_55211.clone();
        }

        public static ChargeType valueOf(String string) {
            return Enum.valueOf(ChargeType.class, string);
        }

        private ChargeType(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ ChargeType[] method_65354() {
            return new ChargeType[]{NONE, ARROW, ROCKET};
        }

        static {
            field_55211 = ChargeType.method_65354();
            CODEC = StringIdentifiable.createCodec(ChargeType::values);
        }
    }
}

