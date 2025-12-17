/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.family;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.util.StringHelper;
import org.jspecify.annotations.Nullable;

public class BlockFamily {
    private final Block baseBlock;
    final Map<Variant, Block> variants = Maps.newHashMap();
    boolean generateModels = true;
    boolean generateRecipes = true;
    @Nullable String group;
    @Nullable String unlockCriterionName;

    BlockFamily(Block baseBlock) {
        this.baseBlock = baseBlock;
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public Map<Variant, Block> getVariants() {
        return this.variants;
    }

    public Block getVariant(Variant variant) {
        return this.variants.get((Object)variant);
    }

    public boolean shouldGenerateModels() {
        return this.generateModels;
    }

    public boolean shouldGenerateRecipes() {
        return this.generateRecipes;
    }

    public Optional<String> getGroup() {
        if (StringHelper.isBlank(this.group)) {
            return Optional.empty();
        }
        return Optional.of(this.group);
    }

    public Optional<String> getUnlockCriterionName() {
        if (StringHelper.isBlank(this.unlockCriterionName)) {
            return Optional.empty();
        }
        return Optional.of(this.unlockCriterionName);
    }

    public static class Builder {
        private final BlockFamily family;

        public Builder(Block baseBlock) {
            this.family = new BlockFamily(baseBlock);
        }

        public BlockFamily build() {
            return this.family;
        }

        public Builder button(Block block) {
            this.family.variants.put(Variant.BUTTON, block);
            return this;
        }

        public Builder chiseled(Block block) {
            this.family.variants.put(Variant.CHISELED, block);
            return this;
        }

        public Builder mosaic(Block block) {
            this.family.variants.put(Variant.MOSAIC, block);
            return this;
        }

        public Builder cracked(Block block) {
            this.family.variants.put(Variant.CRACKED, block);
            return this;
        }

        public Builder cut(Block block) {
            this.family.variants.put(Variant.CUT, block);
            return this;
        }

        public Builder door(Block block) {
            this.family.variants.put(Variant.DOOR, block);
            return this;
        }

        public Builder customFence(Block block) {
            this.family.variants.put(Variant.CUSTOM_FENCE, block);
            return this;
        }

        public Builder fence(Block block) {
            this.family.variants.put(Variant.FENCE, block);
            return this;
        }

        public Builder customFenceGate(Block block) {
            this.family.variants.put(Variant.CUSTOM_FENCE_GATE, block);
            return this;
        }

        public Builder fenceGate(Block block) {
            this.family.variants.put(Variant.FENCE_GATE, block);
            return this;
        }

        public Builder sign(Block block, Block wallBlock) {
            this.family.variants.put(Variant.SIGN, block);
            this.family.variants.put(Variant.WALL_SIGN, wallBlock);
            return this;
        }

        public Builder slab(Block block) {
            this.family.variants.put(Variant.SLAB, block);
            return this;
        }

        public Builder stairs(Block block) {
            this.family.variants.put(Variant.STAIRS, block);
            return this;
        }

        public Builder pressurePlate(Block block) {
            this.family.variants.put(Variant.PRESSURE_PLATE, block);
            return this;
        }

        public Builder polished(Block block) {
            this.family.variants.put(Variant.POLISHED, block);
            return this;
        }

        public Builder trapdoor(Block block) {
            this.family.variants.put(Variant.TRAPDOOR, block);
            return this;
        }

        public Builder wall(Block block) {
            this.family.variants.put(Variant.WALL, block);
            return this;
        }

        public Builder noGenerateModels() {
            this.family.generateModels = false;
            return this;
        }

        public Builder noGenerateRecipes() {
            this.family.generateRecipes = false;
            return this;
        }

        public Builder group(String group) {
            this.family.group = group;
            return this;
        }

        public Builder unlockCriterionName(String unlockCriterionName) {
            this.family.unlockCriterionName = unlockCriterionName;
            return this;
        }
    }

    public static final class Variant
    extends Enum<Variant> {
        public static final /* enum */ Variant BUTTON = new Variant("button");
        public static final /* enum */ Variant CHISELED = new Variant("chiseled");
        public static final /* enum */ Variant CRACKED = new Variant("cracked");
        public static final /* enum */ Variant CUT = new Variant("cut");
        public static final /* enum */ Variant DOOR = new Variant("door");
        public static final /* enum */ Variant CUSTOM_FENCE = new Variant("fence");
        public static final /* enum */ Variant FENCE = new Variant("fence");
        public static final /* enum */ Variant CUSTOM_FENCE_GATE = new Variant("fence_gate");
        public static final /* enum */ Variant FENCE_GATE = new Variant("fence_gate");
        public static final /* enum */ Variant MOSAIC = new Variant("mosaic");
        public static final /* enum */ Variant SIGN = new Variant("sign");
        public static final /* enum */ Variant SLAB = new Variant("slab");
        public static final /* enum */ Variant STAIRS = new Variant("stairs");
        public static final /* enum */ Variant PRESSURE_PLATE = new Variant("pressure_plate");
        public static final /* enum */ Variant POLISHED = new Variant("polished");
        public static final /* enum */ Variant TRAPDOOR = new Variant("trapdoor");
        public static final /* enum */ Variant WALL = new Variant("wall");
        public static final /* enum */ Variant WALL_SIGN = new Variant("wall_sign");
        private final String name;
        private static final /* synthetic */ Variant[] field_28547;

        public static Variant[] values() {
            return (Variant[])field_28547.clone();
        }

        public static Variant valueOf(String string) {
            return Enum.valueOf(Variant.class, string);
        }

        private Variant(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        private static /* synthetic */ Variant[] method_36938() {
            return new Variant[]{BUTTON, CHISELED, CRACKED, CUT, DOOR, CUSTOM_FENCE, FENCE, CUSTOM_FENCE_GATE, FENCE_GATE, MOSAIC, SIGN, SLAB, STAIRS, PRESSURE_PLATE, POLISHED, TRAPDOOR, WALL, WALL_SIGN};
        }

        static {
            field_28547 = Variant.method_36938();
        }
    }
}

