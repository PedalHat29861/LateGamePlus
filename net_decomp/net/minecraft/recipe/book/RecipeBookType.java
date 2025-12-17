/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe.book;

public final class RecipeBookType
extends Enum<RecipeBookType> {
    public static final /* enum */ RecipeBookType CRAFTING = new RecipeBookType();
    public static final /* enum */ RecipeBookType FURNACE = new RecipeBookType();
    public static final /* enum */ RecipeBookType BLAST_FURNACE = new RecipeBookType();
    public static final /* enum */ RecipeBookType SMOKER = new RecipeBookType();
    private static final /* synthetic */ RecipeBookType[] field_25767;

    public static RecipeBookType[] values() {
        return (RecipeBookType[])field_25767.clone();
    }

    public static RecipeBookType valueOf(String string) {
        return Enum.valueOf(RecipeBookType.class, string);
    }

    private static /* synthetic */ RecipeBookType[] method_36674() {
        return new RecipeBookType[]{CRAFTING, FURNACE, BLAST_FURNACE, SMOKER};
    }

    static {
        field_25767 = RecipeBookType.method_36674();
    }
}

