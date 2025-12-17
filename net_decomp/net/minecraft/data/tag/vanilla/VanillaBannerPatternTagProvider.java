/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BannerPatternTags;

public class VanillaBannerPatternTagProvider
extends SimpleTagProvider<BannerPattern> {
    public VanillaBannerPatternTagProvider(DataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataGenerator, RegistryKeys.BANNER_PATTERN, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(BannerPatternTags.NO_ITEM_REQUIRED).add(BannerPatterns.SQUARE_BOTTOM_LEFT, BannerPatterns.SQUARE_BOTTOM_RIGHT, BannerPatterns.SQUARE_TOP_LEFT, BannerPatterns.SQUARE_TOP_RIGHT, BannerPatterns.STRIPE_BOTTOM, BannerPatterns.STRIPE_TOP, BannerPatterns.STRIPE_LEFT, BannerPatterns.STRIPE_RIGHT, BannerPatterns.STRIPE_CENTER, BannerPatterns.STRIPE_MIDDLE, BannerPatterns.STRIPE_DOWNRIGHT, BannerPatterns.STRIPE_DOWNLEFT, BannerPatterns.SMALL_STRIPES, BannerPatterns.CROSS, BannerPatterns.STRAIGHT_CROSS, BannerPatterns.TRIANGLE_BOTTOM, BannerPatterns.TRIANGLE_TOP, BannerPatterns.TRIANGLES_BOTTOM, BannerPatterns.TRIANGLES_TOP, BannerPatterns.DIAGONAL_LEFT, BannerPatterns.DIAGONAL_UP_RIGHT, BannerPatterns.DIAGONAL_UP_LEFT, BannerPatterns.DIAGONAL_RIGHT, BannerPatterns.CIRCLE, BannerPatterns.RHOMBUS, BannerPatterns.HALF_VERTICAL, BannerPatterns.HALF_HORIZONTAL, BannerPatterns.HALF_VERTICAL_RIGHT, BannerPatterns.HALF_HORIZONTAL_BOTTOM, BannerPatterns.BORDER, BannerPatterns.GRADIENT, BannerPatterns.GRADIENT_UP);
        this.builder(BannerPatternTags.FLOWER_PATTERN_ITEM).add(BannerPatterns.FLOWER);
        this.builder(BannerPatternTags.CREEPER_PATTERN_ITEM).add(BannerPatterns.CREEPER);
        this.builder(BannerPatternTags.SKULL_PATTERN_ITEM).add(BannerPatterns.SKULL);
        this.builder(BannerPatternTags.MOJANG_PATTERN_ITEM).add(BannerPatterns.MOJANG);
        this.builder(BannerPatternTags.GLOBE_PATTERN_ITEM).add(BannerPatterns.GLOBE);
        this.builder(BannerPatternTags.PIGLIN_PATTERN_ITEM).add(BannerPatterns.PIGLIN);
        this.builder(BannerPatternTags.FLOW_PATTERN_ITEM).add(BannerPatterns.FLOW);
        this.builder(BannerPatternTags.GUSTER_PATTERN_ITEM).add(BannerPatterns.GUSTER);
        this.builder(BannerPatternTags.FIELD_MASONED_PATTERN_ITEM).add(BannerPatterns.BRICKS);
        this.builder(BannerPatternTags.BORDURE_INDENTED_PATTERN_ITEM).add(BannerPatterns.CURLY_BORDER);
    }
}

