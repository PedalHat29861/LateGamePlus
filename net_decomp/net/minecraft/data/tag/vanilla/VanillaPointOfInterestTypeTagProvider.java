/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class VanillaPointOfInterestTypeTagProvider
extends SimpleTagProvider<PointOfInterestType> {
    public VanillaPointOfInterestTypeTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.POINT_OF_INTEREST_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE).add(PointOfInterestTypes.ARMORER, PointOfInterestTypes.BUTCHER, PointOfInterestTypes.CARTOGRAPHER, PointOfInterestTypes.CLERIC, PointOfInterestTypes.FARMER, PointOfInterestTypes.FISHERMAN, PointOfInterestTypes.FLETCHER, PointOfInterestTypes.LEATHERWORKER, PointOfInterestTypes.LIBRARIAN, PointOfInterestTypes.MASON, PointOfInterestTypes.SHEPHERD, PointOfInterestTypes.TOOLSMITH, PointOfInterestTypes.WEAPONSMITH);
        this.builder(PointOfInterestTypeTags.VILLAGE).addTag(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE).add(PointOfInterestTypes.HOME, PointOfInterestTypes.MEETING);
        this.builder(PointOfInterestTypeTags.BEE_HOME).add(PointOfInterestTypes.BEEHIVE, PointOfInterestTypes.BEE_NEST);
    }
}

