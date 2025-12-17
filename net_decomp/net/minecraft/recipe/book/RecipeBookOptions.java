/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.recipe.book;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.UnaryOperator;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.book.RecipeBookType;

public final class RecipeBookOptions {
    public static final PacketCodec<PacketByteBuf, RecipeBookOptions> PACKET_CODEC = PacketCodec.tuple(CategoryOption.PACKET_CODEC, options -> options.crafting, CategoryOption.PACKET_CODEC, options -> options.furnace, CategoryOption.PACKET_CODEC, options -> options.blastFurnace, CategoryOption.PACKET_CODEC, options -> options.smoker, RecipeBookOptions::new);
    public static final MapCodec<RecipeBookOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CategoryOption.CRAFTING.forGetter(options -> options.crafting), (App)CategoryOption.FURNACE.forGetter(options -> options.furnace), (App)CategoryOption.BLAST_FURNACE.forGetter(options -> options.blastFurnace), (App)CategoryOption.SMOKER.forGetter(options -> options.smoker)).apply((Applicative)instance, RecipeBookOptions::new));
    private CategoryOption crafting;
    private CategoryOption furnace;
    private CategoryOption blastFurnace;
    private CategoryOption smoker;

    public RecipeBookOptions() {
        this(CategoryOption.DEFAULT, CategoryOption.DEFAULT, CategoryOption.DEFAULT, CategoryOption.DEFAULT);
    }

    private RecipeBookOptions(CategoryOption crafting, CategoryOption furnace, CategoryOption blastFurnace, CategoryOption smoker) {
        this.crafting = crafting;
        this.furnace = furnace;
        this.blastFurnace = blastFurnace;
        this.smoker = smoker;
    }

    @VisibleForTesting
    public CategoryOption getOption(RecipeBookType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case RecipeBookType.CRAFTING -> this.crafting;
            case RecipeBookType.FURNACE -> this.furnace;
            case RecipeBookType.BLAST_FURNACE -> this.blastFurnace;
            case RecipeBookType.SMOKER -> this.smoker;
        };
    }

    private void apply(RecipeBookType type, UnaryOperator<CategoryOption> modifier) {
        switch (type) {
            case CRAFTING: {
                this.crafting = (CategoryOption)modifier.apply(this.crafting);
                break;
            }
            case FURNACE: {
                this.furnace = (CategoryOption)modifier.apply(this.furnace);
                break;
            }
            case BLAST_FURNACE: {
                this.blastFurnace = (CategoryOption)modifier.apply(this.blastFurnace);
                break;
            }
            case SMOKER: {
                this.smoker = (CategoryOption)modifier.apply(this.smoker);
            }
        }
    }

    public boolean isGuiOpen(RecipeBookType category) {
        return this.getOption((RecipeBookType)category).guiOpen;
    }

    public void setGuiOpen(RecipeBookType category, boolean open) {
        this.apply(category, option -> option.withGuiOpen(open));
    }

    public boolean isFilteringCraftable(RecipeBookType category) {
        return this.getOption((RecipeBookType)category).filteringCraftable;
    }

    public void setFilteringCraftable(RecipeBookType category, boolean filtering) {
        this.apply(category, option -> option.withFilteringCraftable(filtering));
    }

    public RecipeBookOptions copy() {
        return new RecipeBookOptions(this.crafting, this.furnace, this.blastFurnace, this.smoker);
    }

    public void copyFrom(RecipeBookOptions other) {
        this.crafting = other.crafting;
        this.furnace = other.furnace;
        this.blastFurnace = other.blastFurnace;
        this.smoker = other.smoker;
    }

    public static final class CategoryOption
    extends Record {
        final boolean guiOpen;
        final boolean filteringCraftable;
        public static final CategoryOption DEFAULT = new CategoryOption(false, false);
        public static final MapCodec<CategoryOption> CRAFTING = CategoryOption.createCodec("isGuiOpen", "isFilteringCraftable");
        public static final MapCodec<CategoryOption> FURNACE = CategoryOption.createCodec("isFurnaceGuiOpen", "isFurnaceFilteringCraftable");
        public static final MapCodec<CategoryOption> BLAST_FURNACE = CategoryOption.createCodec("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable");
        public static final MapCodec<CategoryOption> SMOKER = CategoryOption.createCodec("isSmokerGuiOpen", "isSmokerFilteringCraftable");
        public static final PacketCodec<ByteBuf, CategoryOption> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, CategoryOption::guiOpen, PacketCodecs.BOOLEAN, CategoryOption::filteringCraftable, CategoryOption::new);

        public CategoryOption(boolean guiOpen, boolean filteringCraftable) {
            this.guiOpen = guiOpen;
            this.filteringCraftable = filteringCraftable;
        }

        @Override
        public String toString() {
            return "[open=" + this.guiOpen + ", filtering=" + this.filteringCraftable + "]";
        }

        public CategoryOption withGuiOpen(boolean guiOpen) {
            return new CategoryOption(guiOpen, this.filteringCraftable);
        }

        public CategoryOption withFilteringCraftable(boolean filteringCraftable) {
            return new CategoryOption(this.guiOpen, filteringCraftable);
        }

        private static MapCodec<CategoryOption> createCodec(String guiOpenField, String filteringCraftableField) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf(guiOpenField, (Object)false).forGetter(CategoryOption::guiOpen), (App)Codec.BOOL.optionalFieldOf(filteringCraftableField, (Object)false).forGetter(CategoryOption::filteringCraftable)).apply((Applicative)instance, CategoryOption::new));
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CategoryOption.class, "open;filtering", "guiOpen", "filteringCraftable"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CategoryOption.class, "open;filtering", "guiOpen", "filteringCraftable"}, this, o);
        }

        public boolean guiOpen() {
            return this.guiOpen;
        }

        public boolean filteringCraftable() {
            return this.filteringCraftable;
        }
    }
}

