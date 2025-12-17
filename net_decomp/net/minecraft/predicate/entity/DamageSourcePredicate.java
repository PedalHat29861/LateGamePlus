/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.predicate.TagPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record DamageSourcePredicate(List<TagPredicate<DamageType>> tags, Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity, Optional<Boolean> isDirect) {
    public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TagPredicate.createCodec(RegistryKeys.DAMAGE_TYPE).listOf().optionalFieldOf("tags", List.of()).forGetter(DamageSourcePredicate::tags), (App)EntityPredicate.CODEC.optionalFieldOf("direct_entity").forGetter(DamageSourcePredicate::directEntity), (App)EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamageSourcePredicate::sourceEntity), (App)Codec.BOOL.optionalFieldOf("is_direct").forGetter(DamageSourcePredicate::isDirect)).apply((Applicative)instance, DamageSourcePredicate::new));

    public boolean test(ServerPlayerEntity player, DamageSource damageSource) {
        return this.test(player.getEntityWorld(), player.getEntityPos(), damageSource);
    }

    public boolean test(ServerWorld world, Vec3d pos, DamageSource damageSource) {
        for (TagPredicate<DamageType> tagPredicate : this.tags) {
            if (tagPredicate.test(damageSource.getTypeRegistryEntry())) continue;
            return false;
        }
        if (this.directEntity.isPresent() && !this.directEntity.get().test(world, pos, damageSource.getSource())) {
            return false;
        }
        if (this.sourceEntity.isPresent() && !this.sourceEntity.get().test(world, pos, damageSource.getAttacker())) {
            return false;
        }
        return !this.isDirect.isPresent() || this.isDirect.get().booleanValue() == damageSource.isDirect();
    }

    public static class Builder {
        private final ImmutableList.Builder<TagPredicate<DamageType>> tagPredicates = ImmutableList.builder();
        private Optional<EntityPredicate> directEntity = Optional.empty();
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> isDirect = Optional.empty();

        public static Builder create() {
            return new Builder();
        }

        public Builder tag(TagPredicate<DamageType> tagPredicate) {
            this.tagPredicates.add(tagPredicate);
            return this;
        }

        public Builder directEntity(EntityPredicate.Builder entity) {
            this.directEntity = Optional.of(entity.build());
            return this;
        }

        public Builder sourceEntity(EntityPredicate.Builder entity) {
            this.sourceEntity = Optional.of(entity.build());
            return this;
        }

        public Builder isDirect(boolean direct) {
            this.isDirect = Optional.of(direct);
            return this;
        }

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate((List<TagPredicate<DamageType>>)this.tagPredicates.build(), this.directEntity, this.sourceEntity, this.isDirect);
        }
    }
}

