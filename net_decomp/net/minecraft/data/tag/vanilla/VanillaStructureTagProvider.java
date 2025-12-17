/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

public class VanillaStructureTagProvider
extends SimpleTagProvider<Structure> {
    public VanillaStructureTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.STRUCTURE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(StructureTags.VILLAGE).add(StructureKeys.VILLAGE_PLAINS).add(StructureKeys.VILLAGE_DESERT).add(StructureKeys.VILLAGE_SAVANNA).add(StructureKeys.VILLAGE_SNOWY).add(StructureKeys.VILLAGE_TAIGA);
        this.builder(StructureTags.MINESHAFT).add(StructureKeys.MINESHAFT).add(StructureKeys.MINESHAFT_MESA);
        this.builder(StructureTags.OCEAN_RUIN).add(StructureKeys.OCEAN_RUIN_COLD).add(StructureKeys.OCEAN_RUIN_WARM);
        this.builder(StructureTags.SHIPWRECK).add(StructureKeys.SHIPWRECK).add(StructureKeys.SHIPWRECK_BEACHED);
        this.builder(StructureTags.RUINED_PORTAL).add(StructureKeys.RUINED_PORTAL_DESERT).add(StructureKeys.RUINED_PORTAL_JUNGLE).add(StructureKeys.RUINED_PORTAL_MOUNTAIN).add(StructureKeys.RUINED_PORTAL_NETHER).add(StructureKeys.RUINED_PORTAL_OCEAN).add(StructureKeys.RUINED_PORTAL).add(StructureKeys.RUINED_PORTAL_SWAMP);
        this.builder(StructureTags.CATS_SPAWN_IN).add(StructureKeys.SWAMP_HUT);
        this.builder(StructureTags.CATS_SPAWN_AS_BLACK).add(StructureKeys.SWAMP_HUT);
        this.builder(StructureTags.EYE_OF_ENDER_LOCATED).add(StructureKeys.STRONGHOLD);
        this.builder(StructureTags.DOLPHIN_LOCATED).addTag(StructureTags.OCEAN_RUIN).addTag(StructureTags.SHIPWRECK);
        this.builder(StructureTags.ON_WOODLAND_EXPLORER_MAPS).add(StructureKeys.MANSION);
        this.builder(StructureTags.ON_OCEAN_EXPLORER_MAPS).add(StructureKeys.MONUMENT);
        this.builder(StructureTags.ON_TREASURE_MAPS).add(StructureKeys.BURIED_TREASURE);
        this.builder(StructureTags.ON_TRIAL_CHAMBERS_MAPS).add(StructureKeys.TRIAL_CHAMBERS);
        this.builder(StructureTags.ON_SAVANNA_VILLAGE_MAPS).add(StructureKeys.VILLAGE_SAVANNA);
        this.builder(StructureTags.ON_DESERT_VILLAGE_MAPS).add(StructureKeys.VILLAGE_DESERT);
        this.builder(StructureTags.ON_PLAINS_VILLAGE_MAPS).add(StructureKeys.VILLAGE_PLAINS);
        this.builder(StructureTags.ON_TAIGA_VILLAGE_MAPS).add(StructureKeys.VILLAGE_TAIGA);
        this.builder(StructureTags.ON_SNOWY_VILLAGE_MAPS).add(StructureKeys.VILLAGE_SNOWY);
        this.builder(StructureTags.ON_SWAMP_EXPLORER_MAPS).add(StructureKeys.SWAMP_HUT);
        this.builder(StructureTags.ON_JUNGLE_EXPLORER_MAPS).add(StructureKeys.JUNGLE_PYRAMID);
    }
}

