package com.pedalhat.lategameplus.tag;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class LGPItemTags {
    public static final TagKey<Item> CROSSBOWS =
        TagKey.of(RegistryKeys.ITEM, Identifier.of(LateGamePlus.MOD_ID, "crossbows"));
    private LGPItemTags() {}
}
