/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.ValueLookupTagProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.FluidTags;

public class VanillaFluidTagProvider
extends ValueLookupTagProvider<Fluid> {
    public VanillaFluidTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.FLUID, registriesFuture, (T fluid) -> fluid.getRegistryEntry().registryKey());
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(FluidTags.WATER).add((Fluid[])new Fluid[]{Fluids.WATER, Fluids.FLOWING_WATER});
        this.builder(FluidTags.LAVA).add((Fluid[])new Fluid[]{Fluids.LAVA, Fluids.FLOWING_LAVA});
    }
}

