/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityEffectPredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.MovementPredicate;
import net.minecraft.predicate.entity.SlotsPredicate;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public record EntityPredicate(Optional<EntityTypePredicate> type, Optional<DistancePredicate> distance, Optional<MovementPredicate> movement, PositionalPredicates location, Optional<EntityEffectPredicate> effects, Optional<NbtPredicate> nbt, Optional<EntityFlagsPredicate> flags, Optional<EntityEquipmentPredicate> equipment, Optional<EntitySubPredicate> typeSpecific, Optional<Integer> periodicTick, Optional<EntityPredicate> vehicle, Optional<EntityPredicate> passenger, Optional<EntityPredicate> targetedEntity, Optional<String> team, Optional<SlotsPredicate> slots, ComponentsPredicate components) {
    public static final Codec<EntityPredicate> CODEC = Codec.recursive((String)"EntityPredicate", entityPredicateCodec -> RecordCodecBuilder.create(instance -> instance.group((App)EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::type), (App)DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distance), (App)MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement), (App)PositionalPredicates.CODEC.forGetter(EntityPredicate::location), (App)EntityEffectPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects), (App)NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt), (App)EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags), (App)EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment), (App)EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::typeSpecific), (App)Codecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick), (App)entityPredicateCodec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle), (App)entityPredicateCodec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger), (App)entityPredicateCodec.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity), (App)Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team), (App)SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots), (App)ComponentsPredicate.CODEC.forGetter(EntityPredicate::components)).apply((Applicative)instance, EntityPredicate::new)));
    public static final Codec<LootContextPredicate> LOOT_CONTEXT_PREDICATE_CODEC = Codec.withAlternative(LootContextPredicate.CODEC, CODEC, EntityPredicate::asLootContextPredicate);

    public static LootContextPredicate contextPredicateFromEntityPredicate(Builder builder) {
        return EntityPredicate.asLootContextPredicate(builder.build());
    }

    public static Optional<LootContextPredicate> contextPredicateFromEntityPredicate(Optional<EntityPredicate> entityPredicate) {
        return entityPredicate.map(EntityPredicate::asLootContextPredicate);
    }

    public static List<LootContextPredicate> contextPredicateFromEntityPredicates(Builder ... builders) {
        return Stream.of(builders).map(EntityPredicate::contextPredicateFromEntityPredicate).toList();
    }

    public static LootContextPredicate asLootContextPredicate(EntityPredicate predicate) {
        LootCondition lootCondition = EntityPropertiesLootCondition.builder(LootContext.EntityReference.THIS, predicate).build();
        return new LootContextPredicate(List.of(lootCondition));
    }

    public boolean test(ServerPlayerEntity player, @Nullable Entity entity) {
        return this.test(player.getEntityWorld(), player.getEntityPos(), entity);
    }

    public boolean test(ServerWorld world, @Nullable Vec3d pos, @Nullable Entity entity) {
        Team abstractTeam;
        Vec3d vec3d;
        if (entity == null) {
            return false;
        }
        if (this.type.isPresent() && !this.type.get().matches(entity.getType())) {
            return false;
        }
        if (pos == null ? this.distance.isPresent() : this.distance.isPresent() && !this.distance.get().test(pos.x, pos.y, pos.z, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (this.movement.isPresent()) {
            vec3d = entity.getMovement();
            Vec3d vec3d2 = vec3d.multiply(20.0);
            if (!this.movement.get().test(vec3d2.x, vec3d2.y, vec3d2.z, entity.fallDistance)) {
                return false;
            }
        }
        if (this.location.located.isPresent() && !this.location.located.get().test(world, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (this.location.steppingOn.isPresent()) {
            vec3d = Vec3d.ofCenter(entity.getSteppingPos());
            if (!entity.isOnGround() || !this.location.steppingOn.get().test(world, vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
                return false;
            }
        }
        if (this.location.affectsMovement.isPresent()) {
            vec3d = Vec3d.ofCenter(entity.getVelocityAffectingPos());
            if (!this.location.affectsMovement.get().test(world, vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
                return false;
            }
        }
        if (this.effects.isPresent() && !this.effects.get().test(entity)) {
            return false;
        }
        if (this.flags.isPresent() && !this.flags.get().test(entity)) {
            return false;
        }
        if (this.equipment.isPresent() && !this.equipment.get().test(entity)) {
            return false;
        }
        if (this.typeSpecific.isPresent() && !this.typeSpecific.get().test(entity, world, pos)) {
            return false;
        }
        if (this.vehicle.isPresent() && !this.vehicle.get().test(world, pos, entity.getVehicle())) {
            return false;
        }
        if (this.passenger.isPresent() && entity.getPassengerList().stream().noneMatch(entityx -> this.passenger.get().test(world, pos, (Entity)entityx))) {
            return false;
        }
        if (this.targetedEntity.isPresent() && !this.targetedEntity.get().test(world, pos, entity instanceof MobEntity ? ((MobEntity)entity).getTarget() : null)) {
            return false;
        }
        if (this.periodicTick.isPresent() && entity.age % this.periodicTick.get() != 0) {
            return false;
        }
        if (this.team.isPresent() && ((abstractTeam = entity.getScoreboardTeam()) == null || !this.team.get().equals(((AbstractTeam)abstractTeam).getName()))) {
            return false;
        }
        if (this.slots.isPresent() && !this.slots.get().matches(entity)) {
            return false;
        }
        if (!this.components.test(entity)) {
            return false;
        }
        return this.nbt.isEmpty() || this.nbt.get().test(entity);
    }

    public static LootContext createAdvancementEntityLootContext(ServerPlayerEntity player, Entity target) {
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(player.getEntityWorld()).add(LootContextParameters.THIS_ENTITY, target).add(LootContextParameters.ORIGIN, player.getEntityPos()).build(LootContextTypes.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootWorldContext).build(Optional.empty());
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityPredicate.class, "entityType;distanceToPlayer;movement;location;effects;nbt;flags;equipment;subPredicate;periodicTick;vehicle;passenger;targetedEntity;team;slots;components", "type", "distance", "movement", "location", "effects", "nbt", "flags", "equipment", "typeSpecific", "periodicTick", "vehicle", "passenger", "targetedEntity", "team", "slots", "components"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityPredicate.class, "entityType;distanceToPlayer;movement;location;effects;nbt;flags;equipment;subPredicate;periodicTick;vehicle;passenger;targetedEntity;team;slots;components", "type", "distance", "movement", "location", "effects", "nbt", "flags", "equipment", "typeSpecific", "periodicTick", "vehicle", "passenger", "targetedEntity", "team", "slots", "components"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityPredicate.class, "entityType;distanceToPlayer;movement;location;effects;nbt;flags;equipment;subPredicate;periodicTick;vehicle;passenger;targetedEntity;team;slots;components", "type", "distance", "movement", "location", "effects", "nbt", "flags", "equipment", "typeSpecific", "periodicTick", "vehicle", "passenger", "targetedEntity", "team", "slots", "components"}, this, object);
    }

    public static final class PositionalPredicates
    extends Record {
        final Optional<LocationPredicate> located;
        final Optional<LocationPredicate> steppingOn;
        final Optional<LocationPredicate> affectsMovement;
        public static final MapCodec<PositionalPredicates> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LocationPredicate.CODEC.optionalFieldOf("location").forGetter(PositionalPredicates::located), (App)LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(PositionalPredicates::steppingOn), (App)LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(PositionalPredicates::affectsMovement)).apply((Applicative)instance, PositionalPredicates::new));

        public PositionalPredicates(Optional<LocationPredicate> located, Optional<LocationPredicate> steppingOn, Optional<LocationPredicate> affectsMovement) {
            this.located = located;
            this.steppingOn = steppingOn;
            this.affectsMovement = affectsMovement;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PositionalPredicates.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PositionalPredicates.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PositionalPredicates.class, "located;steppingOn;affectsMovement", "located", "steppingOn", "affectsMovement"}, this, object);
        }

        public Optional<LocationPredicate> located() {
            return this.located;
        }

        public Optional<LocationPredicate> steppingOn() {
            return this.steppingOn;
        }

        public Optional<LocationPredicate> affectsMovement() {
            return this.affectsMovement;
        }
    }

    public static class Builder {
        private Optional<EntityTypePredicate> type = Optional.empty();
        private Optional<DistancePredicate> distance = Optional.empty();
        private Optional<MovementPredicate> movement = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<LocationPredicate> steppingOn = Optional.empty();
        private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
        private Optional<EntityEffectPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> typeSpecific = Optional.empty();
        private Optional<Integer> periodicTick = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<SlotsPredicate> slots = Optional.empty();
        private ComponentsPredicate components = ComponentsPredicate.EMPTY;

        public static Builder create() {
            return new Builder();
        }

        public Builder type(RegistryEntryLookup<EntityType<?>> entityTypeRegistry, EntityType<?> type) {
            this.type = Optional.of(EntityTypePredicate.create(entityTypeRegistry, type));
            return this;
        }

        public Builder type(RegistryEntryLookup<EntityType<?>> entityTypeRegistry, TagKey<EntityType<?>> tag) {
            this.type = Optional.of(EntityTypePredicate.create(entityTypeRegistry, tag));
            return this;
        }

        public Builder type(EntityTypePredicate type) {
            this.type = Optional.of(type);
            return this;
        }

        public Builder distance(DistancePredicate distance) {
            this.distance = Optional.of(distance);
            return this;
        }

        public Builder movement(MovementPredicate movement) {
            this.movement = Optional.of(movement);
            return this;
        }

        public Builder location(LocationPredicate.Builder location) {
            this.location = Optional.of(location.build());
            return this;
        }

        public Builder steppingOn(LocationPredicate.Builder steppingOn) {
            this.steppingOn = Optional.of(steppingOn.build());
            return this;
        }

        public Builder movementAffectedBy(LocationPredicate.Builder movementAffectedBy) {
            this.movementAffectedBy = Optional.of(movementAffectedBy.build());
            return this;
        }

        public Builder effects(EntityEffectPredicate.Builder effects) {
            this.effects = effects.build();
            return this;
        }

        public Builder nbt(NbtPredicate nbt) {
            this.nbt = Optional.of(nbt);
            return this;
        }

        public Builder flags(EntityFlagsPredicate.Builder flags) {
            this.flags = Optional.of(flags.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate.Builder equipment) {
            this.equipment = Optional.of(equipment.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate equipment) {
            this.equipment = Optional.of(equipment);
            return this;
        }

        public Builder typeSpecific(EntitySubPredicate typeSpecific) {
            this.typeSpecific = Optional.of(typeSpecific);
            return this;
        }

        public Builder periodicTick(int periodicTick) {
            this.periodicTick = Optional.of(periodicTick);
            return this;
        }

        public Builder vehicle(Builder vehicle) {
            this.vehicle = Optional.of(vehicle.build());
            return this;
        }

        public Builder passenger(Builder passenger) {
            this.passenger = Optional.of(passenger.build());
            return this;
        }

        public Builder targetedEntity(Builder targetedEntity) {
            this.targetedEntity = Optional.of(targetedEntity.build());
            return this;
        }

        public Builder team(String team) {
            this.team = Optional.of(team);
            return this;
        }

        public Builder slots(SlotsPredicate slots) {
            this.slots = Optional.of(slots);
            return this;
        }

        public Builder components(ComponentsPredicate components) {
            this.components = components;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.type, this.distance, this.movement, new PositionalPredicates(this.location, this.steppingOn, this.movementAffectedBy), this.effects, this.nbt, this.flags, this.equipment, this.typeSpecific, this.periodicTick, this.vehicle, this.passenger, this.targetedEntity, this.team, this.slots, this.components);
        }
    }
}

