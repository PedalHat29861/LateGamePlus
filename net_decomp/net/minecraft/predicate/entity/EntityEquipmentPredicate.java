/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.village.raid.Raid;
import org.jspecify.annotations.Nullable;

public record EntityEquipmentPredicate(Optional<ItemPredicate> head, Optional<ItemPredicate> chest, Optional<ItemPredicate> legs, Optional<ItemPredicate> feet, Optional<ItemPredicate> body, Optional<ItemPredicate> mainhand, Optional<ItemPredicate> offhand) {
    public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head), (App)ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest), (App)ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs), (App)ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet), (App)ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body), (App)ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand), (App)ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)).apply((Applicative)instance, EntityEquipmentPredicate::new));

    public static EntityEquipmentPredicate ominousBannerOnHead(RegistryEntryLookup<Item> itemLookup, RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        return Builder.create().head(ItemPredicate.Builder.create().items(itemLookup, Items.WHITE_BANNER).components(ComponentsPredicate.Builder.create().exact(ComponentMapPredicate.ofFiltered(Raid.createOminousBanner(bannerPatternLookup).getComponents(), DataComponentTypes.BANNER_PATTERNS, DataComponentTypes.ITEM_NAME)).build())).build();
    }

    public boolean test(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (this.head.isPresent() && !this.head.get().test(livingEntity.getEquippedStack(EquipmentSlot.HEAD))) {
            return false;
        }
        if (this.chest.isPresent() && !this.chest.get().test(livingEntity.getEquippedStack(EquipmentSlot.CHEST))) {
            return false;
        }
        if (this.legs.isPresent() && !this.legs.get().test(livingEntity.getEquippedStack(EquipmentSlot.LEGS))) {
            return false;
        }
        if (this.feet.isPresent() && !this.feet.get().test(livingEntity.getEquippedStack(EquipmentSlot.FEET))) {
            return false;
        }
        if (this.body.isPresent() && !this.body.get().test(livingEntity.getEquippedStack(EquipmentSlot.BODY))) {
            return false;
        }
        if (this.mainhand.isPresent() && !this.mainhand.get().test(livingEntity.getEquippedStack(EquipmentSlot.MAINHAND))) {
            return false;
        }
        return !this.offhand.isPresent() || this.offhand.get().test(livingEntity.getEquippedStack(EquipmentSlot.OFFHAND));
    }

    public static class Builder {
        private Optional<ItemPredicate> head = Optional.empty();
        private Optional<ItemPredicate> chest = Optional.empty();
        private Optional<ItemPredicate> legs = Optional.empty();
        private Optional<ItemPredicate> feet = Optional.empty();
        private Optional<ItemPredicate> body = Optional.empty();
        private Optional<ItemPredicate> mainhand = Optional.empty();
        private Optional<ItemPredicate> offhand = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public Builder head(ItemPredicate.Builder item) {
            this.head = Optional.of(item.build());
            return this;
        }

        public Builder chest(ItemPredicate.Builder item) {
            this.chest = Optional.of(item.build());
            return this;
        }

        public Builder legs(ItemPredicate.Builder item) {
            this.legs = Optional.of(item.build());
            return this;
        }

        public Builder feet(ItemPredicate.Builder item) {
            this.feet = Optional.of(item.build());
            return this;
        }

        public Builder body(ItemPredicate.Builder item) {
            this.body = Optional.of(item.build());
            return this;
        }

        public Builder mainhand(ItemPredicate.Builder item) {
            this.mainhand = Optional.of(item.build());
            return this;
        }

        public Builder offhand(ItemPredicate.Builder item) {
            this.offhand = Optional.of(item.build());
            return this;
        }

        public EntityEquipmentPredicate build() {
            return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
        }
    }
}

