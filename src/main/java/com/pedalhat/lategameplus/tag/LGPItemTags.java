package com.pedalhat.lategameplus.tag;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class LGPItemTags {
    public static final TagKey<Item> CROSSBOWS =
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "crossbows"));
    public static final TagKey<Item> NETHERITE_HARNESSES =
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "netherite_harnesses"));
    public static final TagKey<Item> REPAIRS_NETHERITE_WOLF_ARMOR =
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "repairs_netherite_wolf_armor"));
    private LGPItemTags() {}
}
