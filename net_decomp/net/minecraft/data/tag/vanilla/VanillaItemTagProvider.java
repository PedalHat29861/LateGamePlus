/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.data.tag.ValueLookupTagProvider;
import net.minecraft.data.tag.vanilla.VanillaBlockItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

public class VanillaItemTagProvider
extends ValueLookupTagProvider<Item> {
    public VanillaItemTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ITEM, registriesFuture, (T value) -> value.getRegistryEntry().registryKey());
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        new VanillaBlockItemTags(){

            @Override
            protected ProvidedTagBuilder<Block, Block> builder(TagKey<Block> blockTag, TagKey<Item> itemTag) {
                return new ItemTagBuilder(VanillaItemTagProvider.this.builder(itemTag));
            }
        }.configure();
        this.builder(ItemTags.BANNERS).add((Item[])new Item[]{Items.WHITE_BANNER, Items.ORANGE_BANNER, Items.MAGENTA_BANNER, Items.LIGHT_BLUE_BANNER, Items.YELLOW_BANNER, Items.LIME_BANNER, Items.PINK_BANNER, Items.GRAY_BANNER, Items.LIGHT_GRAY_BANNER, Items.CYAN_BANNER, Items.PURPLE_BANNER, Items.BLUE_BANNER, Items.BROWN_BANNER, Items.GREEN_BANNER, Items.RED_BANNER, Items.BLACK_BANNER});
        this.builder(ItemTags.BOATS).add((Item[])new Item[]{Items.OAK_BOAT, Items.SPRUCE_BOAT, Items.BIRCH_BOAT, Items.JUNGLE_BOAT, Items.ACACIA_BOAT, Items.DARK_OAK_BOAT, Items.PALE_OAK_BOAT, Items.MANGROVE_BOAT, Items.BAMBOO_RAFT, Items.CHERRY_BOAT}).addTag(ItemTags.CHEST_BOATS);
        this.builder(ItemTags.BUNDLES).add((Item[])new Item[]{Items.BUNDLE, Items.BLACK_BUNDLE, Items.BLUE_BUNDLE, Items.BROWN_BUNDLE, Items.CYAN_BUNDLE, Items.GRAY_BUNDLE, Items.GREEN_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.LIME_BUNDLE, Items.MAGENTA_BUNDLE, Items.ORANGE_BUNDLE, Items.PINK_BUNDLE, Items.PURPLE_BUNDLE, Items.RED_BUNDLE, Items.YELLOW_BUNDLE, Items.WHITE_BUNDLE});
        this.builder(ItemTags.CHEST_BOATS).add((Item[])new Item[]{Items.OAK_CHEST_BOAT, Items.SPRUCE_CHEST_BOAT, Items.BIRCH_CHEST_BOAT, Items.JUNGLE_CHEST_BOAT, Items.ACACIA_CHEST_BOAT, Items.DARK_OAK_CHEST_BOAT, Items.PALE_OAK_CHEST_BOAT, Items.MANGROVE_CHEST_BOAT, Items.BAMBOO_CHEST_RAFT, Items.CHERRY_CHEST_BOAT});
        this.builder(ItemTags.EGGS).add((Item[])new Item[]{Items.EGG, Items.BLUE_EGG, Items.BROWN_EGG});
        this.builder(ItemTags.FISHES).add((Item[])new Item[]{Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.PUFFERFISH, Items.TROPICAL_FISH});
        this.builder(ItemTags.CREEPER_DROP_MUSIC_DISCS).add((Item[])new Item[]{Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT});
        this.builder(ItemTags.COALS).add((Item[])new Item[]{Items.COAL, Items.CHARCOAL});
        this.builder(ItemTags.ARROWS).add((Item[])new Item[]{Items.ARROW, Items.TIPPED_ARROW, Items.SPECTRAL_ARROW});
        this.builder(ItemTags.LECTERN_BOOKS).add((Item[])new Item[]{Items.WRITTEN_BOOK, Items.WRITABLE_BOOK});
        this.builder(ItemTags.BEACON_PAYMENT_ITEMS).add((Item[])new Item[]{Items.NETHERITE_INGOT, Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT});
        this.builder(ItemTags.PIGLIN_REPELLENTS).add(Items.SOUL_TORCH).add(Items.SOUL_LANTERN).add(Items.SOUL_CAMPFIRE);
        this.builder(ItemTags.PIGLIN_LOVED).addTag(ItemTags.GOLD_ORES).add((Item[])new Item[]{Items.GOLD_BLOCK, Items.GILDED_BLACKSTONE, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.GOLD_INGOT, Items.BELL, Items.CLOCK, Items.GOLDEN_CARROT, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GOLDEN_HORSE_ARMOR, Items.GOLDEN_NAUTILUS_ARMOR, Items.GOLDEN_SWORD, Items.GOLDEN_SPEAR, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.RAW_GOLD, Items.RAW_GOLD_BLOCK});
        this.builder(ItemTags.IGNORED_BY_PIGLIN_BABIES).add(Items.LEATHER);
        this.builder(ItemTags.PIGLIN_FOOD).add((Item[])new Item[]{Items.PORKCHOP, Items.COOKED_PORKCHOP});
        this.builder(ItemTags.PIGLIN_SAFE_ARMOR).add((Item[])new Item[]{Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS});
        this.builder(ItemTags.FOX_FOOD).add((Item[])new Item[]{Items.SWEET_BERRIES, Items.GLOW_BERRIES});
        this.builder(ItemTags.DUPLICATES_ALLAYS).add(Items.AMETHYST_SHARD);
        this.builder(ItemTags.BREWING_FUEL).add(Items.BLAZE_POWDER);
        this.builder(ItemTags.NON_FLAMMABLE_WOOD).add((Item[])new Item[]{Items.WARPED_STEM, Items.STRIPPED_WARPED_STEM, Items.WARPED_HYPHAE, Items.STRIPPED_WARPED_HYPHAE, Items.CRIMSON_STEM, Items.STRIPPED_CRIMSON_STEM, Items.CRIMSON_HYPHAE, Items.STRIPPED_CRIMSON_HYPHAE, Items.CRIMSON_PLANKS, Items.WARPED_PLANKS, Items.CRIMSON_SLAB, Items.WARPED_SLAB, Items.CRIMSON_PRESSURE_PLATE, Items.WARPED_PRESSURE_PLATE, Items.CRIMSON_FENCE, Items.WARPED_FENCE, Items.CRIMSON_TRAPDOOR, Items.WARPED_TRAPDOOR, Items.CRIMSON_FENCE_GATE, Items.WARPED_FENCE_GATE, Items.CRIMSON_STAIRS, Items.WARPED_STAIRS, Items.CRIMSON_BUTTON, Items.WARPED_BUTTON, Items.CRIMSON_DOOR, Items.WARPED_DOOR, Items.CRIMSON_SIGN, Items.WARPED_SIGN, Items.WARPED_HANGING_SIGN, Items.CRIMSON_HANGING_SIGN, Items.WARPED_SHELF, Items.CRIMSON_SHELF});
        this.builder(ItemTags.WOODEN_TOOL_MATERIALS).addTag(ItemTags.PLANKS);
        this.builder(ItemTags.STONE_TOOL_MATERIALS).add((Item[])new Item[]{Items.COBBLESTONE, Items.BLACKSTONE, Items.COBBLED_DEEPSLATE});
        this.builder(ItemTags.COPPER_TOOL_MATERIALS).add(Items.COPPER_INGOT);
        this.builder(ItemTags.IRON_TOOL_MATERIALS).add(Items.IRON_INGOT);
        this.builder(ItemTags.GOLD_TOOL_MATERIALS).add(Items.GOLD_INGOT);
        this.builder(ItemTags.DIAMOND_TOOL_MATERIALS).add(Items.DIAMOND);
        this.builder(ItemTags.NETHERITE_TOOL_MATERIALS).add(Items.NETHERITE_INGOT);
        this.builder(ItemTags.REPAIRS_LEATHER_ARMOR).add(Items.LEATHER);
        this.builder(ItemTags.REPAIRS_COPPER_ARMOR).add(Items.COPPER_INGOT);
        this.builder(ItemTags.REPAIRS_CHAIN_ARMOR).add(Items.IRON_INGOT);
        this.builder(ItemTags.REPAIRS_IRON_ARMOR).add(Items.IRON_INGOT);
        this.builder(ItemTags.REPAIRS_GOLD_ARMOR).add(Items.GOLD_INGOT);
        this.builder(ItemTags.REPAIRS_DIAMOND_ARMOR).add(Items.DIAMOND);
        this.builder(ItemTags.REPAIRS_NETHERITE_ARMOR).add(Items.NETHERITE_INGOT);
        this.builder(ItemTags.REPAIRS_TURTLE_HELMET).add(Items.TURTLE_SCUTE);
        this.builder(ItemTags.REPAIRS_WOLF_ARMOR).add(Items.ARMADILLO_SCUTE);
        this.builder(ItemTags.STONE_CRAFTING_MATERIALS).add((Item[])new Item[]{Items.COBBLESTONE, Items.BLACKSTONE, Items.COBBLED_DEEPSLATE});
        this.builder(ItemTags.FREEZE_IMMUNE_WEARABLES).add((Item[])new Item[]{Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET, Items.LEATHER_HORSE_ARMOR});
        this.builder(ItemTags.AXOLOTL_FOOD).add(Items.TROPICAL_FISH_BUCKET);
        this.builder(ItemTags.CLUSTER_MAX_HARVESTABLES).add((Item[])new Item[]{Items.DIAMOND_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.NETHERITE_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.COPPER_PICKAXE});
        this.builder(ItemTags.COMPASSES).add(Items.COMPASS).add(Items.RECOVERY_COMPASS);
        this.builder(ItemTags.CREEPER_IGNITERS).add(Items.FLINT_AND_STEEL).add(Items.FIRE_CHARGE);
        this.builder(ItemTags.SWORDS).add(Items.DIAMOND_SWORD).add(Items.STONE_SWORD).add(Items.GOLDEN_SWORD).add(Items.NETHERITE_SWORD).add(Items.WOODEN_SWORD).add(Items.IRON_SWORD).add(Items.COPPER_SWORD);
        this.builder(ItemTags.AXES).add(Items.DIAMOND_AXE).add(Items.STONE_AXE).add(Items.GOLDEN_AXE).add(Items.NETHERITE_AXE).add(Items.WOODEN_AXE).add(Items.IRON_AXE).add(Items.COPPER_AXE);
        this.builder(ItemTags.PICKAXES).add(Items.DIAMOND_PICKAXE).add(Items.STONE_PICKAXE).add(Items.GOLDEN_PICKAXE).add(Items.NETHERITE_PICKAXE).add(Items.WOODEN_PICKAXE).add(Items.IRON_PICKAXE).add(Items.COPPER_PICKAXE);
        this.builder(ItemTags.SHOVELS).add(Items.DIAMOND_SHOVEL).add(Items.STONE_SHOVEL).add(Items.GOLDEN_SHOVEL).add(Items.NETHERITE_SHOVEL).add(Items.WOODEN_SHOVEL).add(Items.IRON_SHOVEL).add(Items.COPPER_SHOVEL);
        this.builder(ItemTags.HOES).add(Items.DIAMOND_HOE).add(Items.STONE_HOE).add(Items.GOLDEN_HOE).add(Items.NETHERITE_HOE).add(Items.WOODEN_HOE).add(Items.IRON_HOE).add(Items.COPPER_HOE);
        this.builder(ItemTags.SPEARS).add((Item[])new Item[]{Items.DIAMOND_SPEAR, Items.STONE_SPEAR, Items.GOLDEN_SPEAR, Items.NETHERITE_SPEAR, Items.WOODEN_SPEAR, Items.IRON_SPEAR, Items.COPPER_SPEAR});
        this.builder(ItemTags.BREAKS_DECORATED_POTS).addTag(ItemTags.SWORDS).addTag(ItemTags.AXES).addTag(ItemTags.PICKAXES).addTag(ItemTags.SHOVELS).addTag(ItemTags.HOES).add(Items.TRIDENT).add(Items.MACE);
        this.builder(ItemTags.SKELETON_PREFERRED_WEAPONS).add(Items.BOW);
        this.builder(ItemTags.DROWNED_PREFERRED_WEAPONS).add(Items.TRIDENT);
        this.builder(ItemTags.PIGLIN_PREFERRED_WEAPONS).add((Item[])new Item[]{Items.CROSSBOW, Items.GOLDEN_SPEAR});
        this.builder(ItemTags.PILLAGER_PREFERRED_WEAPONS).add(Items.CROSSBOW);
        this.builder(ItemTags.WITHER_SKELETON_DISLIKED_WEAPONS).add(Items.BOW).add(Items.CROSSBOW);
        this.builder(ItemTags.DECORATED_POT_SHERDS).add((Item[])new Item[]{Items.ANGLER_POTTERY_SHERD, Items.ARCHER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD, Items.BURN_POTTERY_SHERD, Items.DANGER_POTTERY_SHERD, Items.EXPLORER_POTTERY_SHERD, Items.FRIEND_POTTERY_SHERD, Items.HEART_POTTERY_SHERD, Items.HEARTBREAK_POTTERY_SHERD, Items.HOWL_POTTERY_SHERD, Items.MINER_POTTERY_SHERD, Items.MOURNER_POTTERY_SHERD, Items.PLENTY_POTTERY_SHERD, Items.PRIZE_POTTERY_SHERD, Items.SHEAF_POTTERY_SHERD, Items.SHELTER_POTTERY_SHERD, Items.SKULL_POTTERY_SHERD, Items.SNORT_POTTERY_SHERD, Items.FLOW_POTTERY_SHERD, Items.GUSTER_POTTERY_SHERD, Items.SCRAPE_POTTERY_SHERD});
        this.builder(ItemTags.DECORATED_POT_INGREDIENTS).add(Items.BRICK).addTag(ItemTags.DECORATED_POT_SHERDS);
        this.builder(ItemTags.FOOT_ARMOR).add((Item[])new Item[]{Items.LEATHER_BOOTS, Items.COPPER_BOOTS, Items.CHAINMAIL_BOOTS, Items.GOLDEN_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS});
        this.builder(ItemTags.LEG_ARMOR).add((Item[])new Item[]{Items.LEATHER_LEGGINGS, Items.COPPER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS});
        this.builder(ItemTags.CHEST_ARMOR).add((Item[])new Item[]{Items.LEATHER_CHESTPLATE, Items.COPPER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE});
        this.builder(ItemTags.HEAD_ARMOR).add((Item[])new Item[]{Items.LEATHER_HELMET, Items.COPPER_HELMET, Items.CHAINMAIL_HELMET, Items.GOLDEN_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET});
        this.builder(ItemTags.SKULLS).add((Item[])new Item[]{Items.PLAYER_HEAD, Items.CREEPER_HEAD, Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.DRAGON_HEAD, Items.PIGLIN_HEAD});
        this.builder(ItemTags.TRIMMABLE_ARMOR).addTag(ItemTags.FOOT_ARMOR).addTag(ItemTags.LEG_ARMOR).addTag(ItemTags.CHEST_ARMOR).addTag(ItemTags.HEAD_ARMOR);
        this.builder(ItemTags.TRIM_MATERIALS).add(registries.getOrThrow(RegistryKeys.ITEM).streamEntries().filter(entry -> ((Item)entry.value()).getComponents().contains(DataComponentTypes.PROVIDES_TRIM_MATERIAL)).sorted(Comparator.comparing(entry -> entry.registryKey().getValue())).map(RegistryEntry.Reference::value));
        this.builder(ItemTags.BOOKSHELF_BOOKS).add((Item[])new Item[]{Items.BOOK, Items.WRITTEN_BOOK, Items.ENCHANTED_BOOK, Items.WRITABLE_BOOK, Items.KNOWLEDGE_BOOK});
        this.builder(ItemTags.NOTEBLOCK_TOP_INSTRUMENTS).add((Item[])new Item[]{Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.WITHER_SKELETON_SKULL, Items.PIGLIN_HEAD, Items.PLAYER_HEAD});
        this.builder(ItemTags.SNIFFER_FOOD).add(Items.TORCHFLOWER_SEEDS);
        this.builder(ItemTags.VILLAGER_PLANTABLE_SEEDS).add((Item[])new Item[]{Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD});
        this.builder(ItemTags.VILLAGER_PICKS_UP).addTag(ItemTags.VILLAGER_PLANTABLE_SEEDS).add((Item[])new Item[]{Items.BREAD, Items.WHEAT, Items.BEETROOT});
        this.builder(ItemTags.BOOK_CLONING_TARGET).add(Items.WRITABLE_BOOK);
        this.builder(ItemTags.FOOT_ARMOR_ENCHANTABLE).addTag(ItemTags.FOOT_ARMOR);
        this.builder(ItemTags.LEG_ARMOR_ENCHANTABLE).addTag(ItemTags.LEG_ARMOR);
        this.builder(ItemTags.CHEST_ARMOR_ENCHANTABLE).addTag(ItemTags.CHEST_ARMOR);
        this.builder(ItemTags.HEAD_ARMOR_ENCHANTABLE).addTag(ItemTags.HEAD_ARMOR);
        this.builder(ItemTags.ARMOR_ENCHANTABLE).addTag(ItemTags.FOOT_ARMOR_ENCHANTABLE).addTag(ItemTags.LEG_ARMOR_ENCHANTABLE).addTag(ItemTags.CHEST_ARMOR_ENCHANTABLE).addTag(ItemTags.HEAD_ARMOR_ENCHANTABLE);
        this.builder(ItemTags.SWEEPING_ENCHANTABLE).addTag(ItemTags.SWORDS);
        this.builder(ItemTags.MELEE_WEAPON_ENCHANTABLE).addTag(ItemTags.SWORDS).addTag(ItemTags.SPEARS);
        this.builder(ItemTags.FIRE_ASPECT_ENCHANTABLE).addTag(ItemTags.MELEE_WEAPON_ENCHANTABLE).add(Items.MACE);
        this.builder(ItemTags.SHARP_WEAPON_ENCHANTABLE).addTag(ItemTags.MELEE_WEAPON_ENCHANTABLE).addTag(ItemTags.AXES);
        this.builder(ItemTags.WEAPON_ENCHANTABLE).addTag(ItemTags.SHARP_WEAPON_ENCHANTABLE).add(Items.MACE);
        this.builder(ItemTags.MACE_ENCHANTABLE).add(Items.MACE);
        this.builder(ItemTags.MINING_ENCHANTABLE).addTag(ItemTags.AXES).addTag(ItemTags.PICKAXES).addTag(ItemTags.SHOVELS).addTag(ItemTags.HOES).add(Items.SHEARS);
        this.builder(ItemTags.MINING_LOOT_ENCHANTABLE).addTag(ItemTags.AXES).addTag(ItemTags.PICKAXES).addTag(ItemTags.SHOVELS).addTag(ItemTags.HOES);
        this.builder(ItemTags.FISHING_ENCHANTABLE).add(Items.FISHING_ROD);
        this.builder(ItemTags.TRIDENT_ENCHANTABLE).add(Items.TRIDENT);
        this.builder(ItemTags.LUNGE_ENCHANTABLE).addTag(ItemTags.SPEARS);
        this.builder(ItemTags.DURABILITY_ENCHANTABLE).addTag(ItemTags.FOOT_ARMOR).addTag(ItemTags.LEG_ARMOR).addTag(ItemTags.CHEST_ARMOR).addTag(ItemTags.HEAD_ARMOR).add(Items.ELYTRA).add(Items.SHIELD).addTag(ItemTags.SWORDS).addTag(ItemTags.AXES).addTag(ItemTags.PICKAXES).addTag(ItemTags.SHOVELS).addTag(ItemTags.HOES).add(Items.BOW).add(Items.CROSSBOW).add(Items.TRIDENT).add(Items.FLINT_AND_STEEL).add(Items.SHEARS).add(Items.BRUSH).add(Items.FISHING_ROD).add((Item[])new Item[]{Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK}).add(Items.MACE).addTag(ItemTags.SPEARS);
        this.builder(ItemTags.BOW_ENCHANTABLE).add(Items.BOW);
        this.builder(ItemTags.EQUIPPABLE_ENCHANTABLE).addTag(ItemTags.FOOT_ARMOR).addTag(ItemTags.LEG_ARMOR).addTag(ItemTags.CHEST_ARMOR).addTag(ItemTags.HEAD_ARMOR).add(Items.ELYTRA).addTag(ItemTags.SKULLS).add(Items.CARVED_PUMPKIN);
        this.builder(ItemTags.CROSSBOW_ENCHANTABLE).add(Items.CROSSBOW);
        this.builder(ItemTags.VANISHING_ENCHANTABLE).addTag(ItemTags.DURABILITY_ENCHANTABLE).add(Items.COMPASS).add(Items.CARVED_PUMPKIN).addTag(ItemTags.SKULLS);
        this.builder(ItemTags.DYEABLE).add((Item[])new Item[]{Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR, Items.WOLF_ARMOR});
        this.builder(ItemTags.FURNACE_MINECART_FUEL).add((Item[])new Item[]{Items.COAL, Items.CHARCOAL});
        this.builder(ItemTags.MEAT).add((Item[])new Item[]{Items.BEEF, Items.CHICKEN, Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_PORKCHOP, Items.COOKED_RABBIT, Items.MUTTON, Items.PORKCHOP, Items.RABBIT, Items.ROTTEN_FLESH});
        this.builder(ItemTags.WOLF_FOOD).addTag(ItemTags.MEAT).add((Item[])new Item[]{Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.RABBIT_STEW});
        this.builder(ItemTags.OCELOT_FOOD).add((Item[])new Item[]{Items.COD, Items.SALMON});
        this.builder(ItemTags.CAT_FOOD).add((Item[])new Item[]{Items.COD, Items.SALMON});
        this.builder(ItemTags.HORSE_FOOD).add((Item[])new Item[]{Items.WHEAT, Items.SUGAR, Items.HAY_BLOCK, Items.APPLE, Items.CARROT, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE});
        this.builder(ItemTags.ZOMBIE_HORSE_FOOD).add(Items.RED_MUSHROOM);
        this.builder(ItemTags.HORSE_TEMPT_ITEMS).add((Item[])new Item[]{Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE});
        this.builder(ItemTags.HARNESSES).add((Item[])new Item[]{Items.WHITE_HARNESS, Items.ORANGE_HARNESS, Items.MAGENTA_HARNESS, Items.LIGHT_BLUE_HARNESS, Items.YELLOW_HARNESS, Items.LIME_HARNESS, Items.PINK_HARNESS, Items.GRAY_HARNESS, Items.LIGHT_GRAY_HARNESS, Items.CYAN_HARNESS, Items.PURPLE_HARNESS, Items.BLUE_HARNESS, Items.BROWN_HARNESS, Items.GREEN_HARNESS, Items.RED_HARNESS, Items.BLACK_HARNESS});
        this.builder(ItemTags.HAPPY_GHAST_FOOD).add(Items.SNOWBALL);
        this.builder(ItemTags.HAPPY_GHAST_TEMPT_ITEMS).addTag(ItemTags.HAPPY_GHAST_FOOD).addTag(ItemTags.HARNESSES);
        this.builder(ItemTags.CAMEL_FOOD).add(Items.CACTUS);
        this.builder(ItemTags.CAMEL_HUSK_FOOD).add(Items.RABBIT_FOOT);
        this.builder(ItemTags.ARMADILLO_FOOD).add(Items.SPIDER_EYE);
        this.builder(ItemTags.CHICKEN_FOOD).add((Item[])new Item[]{Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD});
        this.builder(ItemTags.FROG_FOOD).add(Items.SLIME_BALL);
        this.builder(ItemTags.HOGLIN_FOOD).add(Items.CRIMSON_FUNGUS);
        this.builder(ItemTags.LLAMA_FOOD).add((Item[])new Item[]{Items.WHEAT, Items.HAY_BLOCK});
        this.builder(ItemTags.LLAMA_TEMPT_ITEMS).add(Items.HAY_BLOCK);
        this.builder(ItemTags.NAUTILUS_TAMING_ITEMS).add((Item[])new Item[]{Items.PUFFERFISH_BUCKET, Items.PUFFERFISH});
        this.builder(ItemTags.NAUTILUS_BUCKET_FOOD).add((Item[])new Item[]{Items.PUFFERFISH_BUCKET, Items.COD_BUCKET, Items.SALMON_BUCKET, Items.TROPICAL_FISH_BUCKET});
        this.builder(ItemTags.NAUTILUS_FOOD).addTag(ItemTags.FISHES).addTag(ItemTags.NAUTILUS_BUCKET_FOOD);
        this.builder(ItemTags.PANDA_FOOD).add(Items.BAMBOO);
        this.builder(ItemTags.PANDA_EATS_FROM_GROUND).addTag(ItemTags.PANDA_FOOD).add(Items.CAKE);
        this.builder(ItemTags.PIG_FOOD).add((Item[])new Item[]{Items.CARROT, Items.POTATO, Items.BEETROOT});
        this.builder(ItemTags.RABBIT_FOOD).add((Item[])new Item[]{Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION});
        this.builder(ItemTags.STRIDER_FOOD).add(Items.WARPED_FUNGUS);
        this.builder(ItemTags.STRIDER_TEMPT_ITEMS).addTag(ItemTags.STRIDER_FOOD).add(Items.WARPED_FUNGUS_ON_A_STICK);
        this.builder(ItemTags.TURTLE_FOOD).add(Items.SEAGRASS);
        this.builder(ItemTags.PARROT_FOOD).add((Item[])new Item[]{Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD});
        this.builder(ItemTags.PARROT_POISONOUS_FOOD).add(Items.COOKIE);
        this.builder(ItemTags.COW_FOOD).add(Items.WHEAT);
        this.builder(ItemTags.SHEEP_FOOD).add(Items.WHEAT);
        this.builder(ItemTags.GOAT_FOOD).add(Items.WHEAT);
        this.builder(ItemTags.MAP_INVISIBILITY_EQUIPMENT).add(Items.CARVED_PUMPKIN);
        this.builder(ItemTags.GAZE_DISGUISE_EQUIPMENT).add(Items.CARVED_PUMPKIN);
        this.builder(ItemTags.SHEARABLE_FROM_COPPER_GOLEM).add(Items.POPPY);
    }

    static class ItemTagBuilder
    implements ProvidedTagBuilder<Block, Block> {
        private final ProvidedTagBuilder<Item, Item> parent;

        public ItemTagBuilder(ProvidedTagBuilder<Item, Item> parent) {
            this.parent = parent;
        }

        @Override
        public ProvidedTagBuilder<Block, Block> add(Block block) {
            this.parent.add(Objects.requireNonNull(block.asItem()));
            return this;
        }

        @Override
        public ProvidedTagBuilder<Block, Block> addOptional(Block block) {
            this.parent.addOptional(Objects.requireNonNull(block.asItem()));
            return this;
        }

        private static TagKey<Item> getItemTag(TagKey<Block> tag) {
            return TagKey.of(RegistryKeys.ITEM, tag.id());
        }

        @Override
        public ProvidedTagBuilder<Block, Block> addTag(TagKey<Block> tag) {
            this.parent.addTag(ItemTagBuilder.getItemTag(tag));
            return this;
        }

        @Override
        public ProvidedTagBuilder<Block, Block> addOptionalTag(TagKey<Block> tag) {
            this.parent.addOptionalTag(ItemTagBuilder.getItemTag(tag));
            return this;
        }
    }
}

