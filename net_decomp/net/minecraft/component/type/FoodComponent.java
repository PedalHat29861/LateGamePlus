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
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public record FoodComponent(int nutrition, float saturation, boolean canAlwaysEat) implements Consumable
{
    public static final Codec<FoodComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodComponent::nutrition), (App)Codec.FLOAT.fieldOf("saturation").forGetter(FoodComponent::saturation), (App)Codec.BOOL.optionalFieldOf("can_always_eat", (Object)false).forGetter(FoodComponent::canAlwaysEat)).apply((Applicative)instance, FoodComponent::new));
    public static final PacketCodec<RegistryByteBuf, FoodComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, FoodComponent::nutrition, PacketCodecs.FLOAT, FoodComponent::saturation, PacketCodecs.BOOLEAN, FoodComponent::canAlwaysEat, FoodComponent::new);

    @Override
    public void onConsume(World world, LivingEntity user, ItemStack stack, ConsumableComponent consumable) {
        Random random = user.getRandom();
        world.playSound(null, user.getX(), user.getY(), user.getZ(), consumable.sound().value(), SoundCategory.NEUTRAL, 1.0f, random.nextTriangular(1.0f, 0.4f));
        if (user instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)user;
            playerEntity.getHungerManager().eat(this);
            world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, MathHelper.nextBetween(random, 0.9f, 1.0f));
        }
    }

    public static class Builder {
        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;

        public Builder nutrition(int nutrition) {
            this.nutrition = nutrition;
            return this;
        }

        public Builder saturationModifier(float saturationModifier) {
            this.saturationModifier = saturationModifier;
            return this;
        }

        public Builder alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public FoodComponent build() {
            float f = HungerConstants.calculateSaturation(this.nutrition, this.saturationModifier);
            return new FoodComponent(this.nutrition, f, this.canAlwaysEat);
        }
    }
}

