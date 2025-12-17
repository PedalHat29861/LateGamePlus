/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntSortedMap
 */
package net.minecraft.item;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import java.util.Collections;
import java.util.SequencedSet;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;

public class FuelRegistry {
    private final Object2IntSortedMap<Item> fuelValues;

    FuelRegistry(Object2IntSortedMap<Item> fuelValues) {
        this.fuelValues = fuelValues;
    }

    public boolean isFuel(ItemStack item) {
        return this.fuelValues.containsKey((Object)item.getItem());
    }

    public SequencedSet<Item> getFuelItems() {
        return Collections.unmodifiableSequencedSet(this.fuelValues.keySet());
    }

    public int getFuelTicks(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }
        return this.fuelValues.getInt((Object)item.getItem());
    }

    public static FuelRegistry createDefault(RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures) {
        return FuelRegistry.createDefault(registries, enabledFeatures, 200);
    }

    public static FuelRegistry createDefault(RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures, int itemSmeltTime) {
        return new Builder(registries, enabledFeatures).add(Items.LAVA_BUCKET, itemSmeltTime * 100).add(Blocks.COAL_BLOCK, itemSmeltTime * 8 * 10).add(Items.BLAZE_ROD, itemSmeltTime * 12).add(Items.COAL, itemSmeltTime * 8).add(Items.CHARCOAL, itemSmeltTime * 8).add(ItemTags.LOGS, itemSmeltTime * 3 / 2).add(ItemTags.BAMBOO_BLOCKS, itemSmeltTime * 3 / 2).add(ItemTags.PLANKS, itemSmeltTime * 3 / 2).add(Blocks.BAMBOO_MOSAIC, itemSmeltTime * 3 / 2).add(ItemTags.WOODEN_STAIRS, itemSmeltTime * 3 / 2).add(Blocks.BAMBOO_MOSAIC_STAIRS, itemSmeltTime * 3 / 2).add(ItemTags.WOODEN_SLABS, itemSmeltTime * 3 / 4).add(Blocks.BAMBOO_MOSAIC_SLAB, itemSmeltTime * 3 / 4).add(ItemTags.WOODEN_TRAPDOORS, itemSmeltTime * 3 / 2).add(ItemTags.WOODEN_PRESSURE_PLATES, itemSmeltTime * 3 / 2).add(ItemTags.WOODEN_SHELVES, itemSmeltTime * 3 / 2).add(ItemTags.WOODEN_FENCES, itemSmeltTime * 3 / 2).add(ItemTags.FENCE_GATES, itemSmeltTime * 3 / 2).add(Blocks.NOTE_BLOCK, itemSmeltTime * 3 / 2).add(Blocks.BOOKSHELF, itemSmeltTime * 3 / 2).add(Blocks.CHISELED_BOOKSHELF, itemSmeltTime * 3 / 2).add(Blocks.LECTERN, itemSmeltTime * 3 / 2).add(Blocks.JUKEBOX, itemSmeltTime * 3 / 2).add(Blocks.CHEST, itemSmeltTime * 3 / 2).add(Blocks.TRAPPED_CHEST, itemSmeltTime * 3 / 2).add(Blocks.CRAFTING_TABLE, itemSmeltTime * 3 / 2).add(Blocks.DAYLIGHT_DETECTOR, itemSmeltTime * 3 / 2).add(ItemTags.BANNERS, itemSmeltTime * 3 / 2).add(Items.BOW, itemSmeltTime * 3 / 2).add(Items.FISHING_ROD, itemSmeltTime * 3 / 2).add(Blocks.LADDER, itemSmeltTime * 3 / 2).add(ItemTags.SIGNS, itemSmeltTime).add(ItemTags.HANGING_SIGNS, itemSmeltTime * 4).add(Items.WOODEN_SHOVEL, itemSmeltTime).add(Items.WOODEN_SWORD, itemSmeltTime).add(Items.WOODEN_SPEAR, itemSmeltTime).add(Items.WOODEN_HOE, itemSmeltTime).add(Items.WOODEN_AXE, itemSmeltTime).add(Items.WOODEN_PICKAXE, itemSmeltTime).add(ItemTags.WOODEN_DOORS, itemSmeltTime).add(ItemTags.BOATS, itemSmeltTime * 6).add(ItemTags.WOOL, itemSmeltTime / 2).add(ItemTags.WOODEN_BUTTONS, itemSmeltTime / 2).add(Items.STICK, itemSmeltTime / 2).add(ItemTags.SAPLINGS, itemSmeltTime / 2).add(Items.BOWL, itemSmeltTime / 2).add(ItemTags.WOOL_CARPETS, 1 + itemSmeltTime / 3).add(Blocks.DRIED_KELP_BLOCK, 1 + itemSmeltTime * 20).add(Items.CROSSBOW, itemSmeltTime * 3 / 2).add(Blocks.BAMBOO, itemSmeltTime / 4).add(Blocks.DEAD_BUSH, itemSmeltTime / 2).add(Blocks.SHORT_DRY_GRASS, itemSmeltTime / 2).add(Blocks.TALL_DRY_GRASS, itemSmeltTime / 2).add(Blocks.SCAFFOLDING, itemSmeltTime / 4).add(Blocks.LOOM, itemSmeltTime * 3 / 2).add(Blocks.BARREL, itemSmeltTime * 3 / 2).add(Blocks.CARTOGRAPHY_TABLE, itemSmeltTime * 3 / 2).add(Blocks.FLETCHING_TABLE, itemSmeltTime * 3 / 2).add(Blocks.SMITHING_TABLE, itemSmeltTime * 3 / 2).add(Blocks.COMPOSTER, itemSmeltTime * 3 / 2).add(Blocks.AZALEA, itemSmeltTime / 2).add(Blocks.FLOWERING_AZALEA, itemSmeltTime / 2).add(Blocks.MANGROVE_ROOTS, itemSmeltTime * 3 / 2).add(Blocks.LEAF_LITTER, itemSmeltTime / 2).remove(ItemTags.NON_FLAMMABLE_WOOD).build();
    }

    public static class Builder {
        private final RegistryWrapper<Item> itemLookup;
        private final FeatureSet enabledFeatures;
        private final Object2IntSortedMap<Item> fuelValues = new Object2IntLinkedOpenHashMap();

        public Builder(RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures) {
            this.itemLookup = registries.getOrThrow(RegistryKeys.ITEM);
            this.enabledFeatures = enabledFeatures;
        }

        public FuelRegistry build() {
            return new FuelRegistry(this.fuelValues);
        }

        public Builder remove(TagKey<Item> tag) {
            this.fuelValues.keySet().removeIf(item -> item.getRegistryEntry().isIn(tag));
            return this;
        }

        public Builder add(TagKey<Item> tag2, int value) {
            this.itemLookup.getOptional(tag2).ifPresent(tag -> {
                for (RegistryEntry registryEntry : tag) {
                    this.add(value, (Item)registryEntry.value());
                }
            });
            return this;
        }

        public Builder add(ItemConvertible item, int value) {
            Item item2 = item.asItem();
            this.add(value, item2);
            return this;
        }

        private void add(int value, Item item) {
            if (item.isEnabled(this.enabledFeatures)) {
                this.fuelValues.put((Object)item, value);
            }
        }
    }
}

