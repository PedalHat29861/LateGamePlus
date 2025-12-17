/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record FluidPredicate(Optional<RegistryEntryList<Fluid>> fluids, Optional<StatePredicate> state) {
    public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.FLUID).optionalFieldOf("fluids").forGetter(FluidPredicate::fluids), (App)StatePredicate.CODEC.optionalFieldOf("state").forGetter(FluidPredicate::state)).apply((Applicative)instance, FluidPredicate::new));

    public boolean test(ServerWorld world, BlockPos pos) {
        if (!world.isPosLoaded(pos)) {
            return false;
        }
        FluidState fluidState = world.getFluidState(pos);
        if (this.fluids.isPresent() && !fluidState.isIn(this.fluids.get())) {
            return false;
        }
        return !this.state.isPresent() || this.state.get().test(fluidState);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{FluidPredicate.class, "fluids;properties", "fluids", "state"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FluidPredicate.class, "fluids;properties", "fluids", "state"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FluidPredicate.class, "fluids;properties", "fluids", "state"}, this, object);
    }

    public static class Builder {
        private Optional<RegistryEntryList<Fluid>> tag = Optional.empty();
        private Optional<StatePredicate> state = Optional.empty();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder fluid(Fluid fluid) {
            this.tag = Optional.of(RegistryEntryList.of(fluid.getRegistryEntry()));
            return this;
        }

        public Builder tag(RegistryEntryList<Fluid> tag) {
            this.tag = Optional.of(tag);
            return this;
        }

        public Builder state(StatePredicate state) {
            this.state = Optional.of(state);
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.tag, this.state);
        }
    }
}

