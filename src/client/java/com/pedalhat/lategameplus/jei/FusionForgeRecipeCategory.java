package com.pedalhat.lategameplus.jei;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.recipe.FusionForgeRecipe;
import com.pedalhat.lategameplus.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FusionForgeRecipeCategory implements IRecipeCategory<FusionForgeRecipe> {
    private static final Identifier TEXTURE = Identifier.of(LateGamePlus.MOD_ID, "textures/gui/container/fusion_forge.png");
    private static final Identifier BURN_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/burn_progress.png");
    private static final Identifier LIT_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/lit_progress.png");
    private static final int BACKGROUND_WIDTH = 176;
    private static final int BACKGROUND_HEIGHT = 79;
    private static final int INPUT_A_X = 49;
    private static final int INPUT_A_Y = 22;
    private static final int INPUT_B_X = 86;
    private static final int INPUT_B_Y = 22;
    private static final int FUEL_X = 68;
    private static final int FUEL_Y = 57;
    private static final int CATALYST_X = 19;
    private static final int CATALYST_Y = 50;
    private static final int OUTPUT_X = 142;
    private static final int OUTPUT_Y = 42;
    private static final int ARROW_X = 105;
    private static final int ARROW_Y = 42;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int FLAME_X = 68;
    private static final int FLAME_Y = 39;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable flame;

    public FusionForgeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FUSION_FORGE));
        this.arrow = guiHelper.createDrawable(BURN_PROGRESS_TEXTURE, 0, 0, ARROW_WIDTH, ARROW_HEIGHT);
        this.flame = guiHelper.createDrawable(LIT_PROGRESS_TEXTURE, 0, 0, FLAME_WIDTH, FLAME_HEIGHT);
    }

    @Override
    public IRecipeType<FusionForgeRecipe> getRecipeType() {
        return FusionForgeJeiPlugin.FUSION_FORGE_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("block.lategameplus.fusion_forge");
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FusionForgeRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(INPUT_A_X, INPUT_A_Y).add(recipe.getInputA());
        builder.addInputSlot(INPUT_B_X, INPUT_B_Y).add(recipe.getInputB());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, FUEL_X, FUEL_Y).add(new ItemStack(Items.COAL));
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CATALYST_X, CATALYST_Y)
            .add(new ItemStack(Items.NETHER_STAR));
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).add(recipe.getOutput());
    }

    @Override
    public void draw(FusionForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext context, double mouseX,
                     double mouseY) {
        background.draw(context);
        arrow.draw(context, ARROW_X, ARROW_Y);
        flame.draw(context, FLAME_X, FLAME_Y);
    }
}
