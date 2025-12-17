/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe.book;

public final class RecipeCategory
extends Enum<RecipeCategory> {
    public static final /* enum */ RecipeCategory BUILDING_BLOCKS = new RecipeCategory("building_blocks");
    public static final /* enum */ RecipeCategory DECORATIONS = new RecipeCategory("decorations");
    public static final /* enum */ RecipeCategory REDSTONE = new RecipeCategory("redstone");
    public static final /* enum */ RecipeCategory TRANSPORTATION = new RecipeCategory("transportation");
    public static final /* enum */ RecipeCategory TOOLS = new RecipeCategory("tools");
    public static final /* enum */ RecipeCategory COMBAT = new RecipeCategory("combat");
    public static final /* enum */ RecipeCategory FOOD = new RecipeCategory("food");
    public static final /* enum */ RecipeCategory BREWING = new RecipeCategory("brewing");
    public static final /* enum */ RecipeCategory MISC = new RecipeCategory("misc");
    private final String name;
    private static final /* synthetic */ RecipeCategory[] field_40644;

    public static RecipeCategory[] values() {
        return (RecipeCategory[])field_40644.clone();
    }

    public static RecipeCategory valueOf(String string) {
        return Enum.valueOf(RecipeCategory.class, string);
    }

    private RecipeCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    private static /* synthetic */ RecipeCategory[] method_46204() {
        return new RecipeCategory[]{BUILDING_BLOCKS, DECORATIONS, REDSTONE, TRANSPORTATION, TOOLS, COMBAT, FOOD, BREWING, MISC};
    }

    static {
        field_40644 = RecipeCategory.method_46204();
    }
}

