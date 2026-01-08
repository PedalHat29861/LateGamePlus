package com.pedalhat.lategameplus.jei;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.recipe.FusionForgeRecipe;
import com.pedalhat.lategameplus.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class FusionForgeRecipeCategory implements IRecipeCategory<RecipeEntry<FusionForgeRecipe>> {
    private static final Identifier TEXTURE = Identifier.of(LateGamePlus.MOD_ID, "textures/gui/container/fusion_forge_jei.png");
    private static final Identifier BURN_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/burn_progress.png");
    private static final Identifier LIT_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/lit_progress.png");
    private static final Identifier X2_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/fusion_forge_x2.png");
    private static final int BACKGROUND_WIDTH = 176;
    private static final int BACKGROUND_HEIGHT = 68;
    private static final int CATEGORY_WIDTH = 176;
    private static final int CATEGORY_HEIGHT = BACKGROUND_HEIGHT;
    private static final int INPUT_A_X = 49;
    private static final int INPUT_A_Y = 10;
    private static final int INPUT_B_X = 86;
    private static final int INPUT_B_Y = 10;
    private static final int FUEL_X = 68;
    private static final int FUEL_Y = 45;
    private static final int CATALYST_X = 19;
    private static final int CATALYST_Y = 36;
    private static final int OUTPUT_X = 142;
    private static final int OUTPUT_Y = 23;
    private static final int ARROW_X = 106;
    private static final int ARROW_Y = 23;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int FLAME_X = 68;
    private static final int FLAME_Y = 28;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;
    private static final int X2_WIDTH = 18;
    private static final int X2_HEIGHT = 11;
    private static final int X2_X = CATALYST_X;
    private static final int X2_Y = CATALYST_Y - 15;
    private static final String X2_TOOLTIP_KEY = "tooltip.lategameplus.fusion_forge.catalyst_x2";

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable flame;
    private final IDrawable x2Icon;

    public FusionForgeRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT)
            .setTextureSize(BACKGROUND_WIDTH, BACKGROUND_HEIGHT)
            .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FUSION_FORGE));
        this.arrow = guiHelper.drawableBuilder(BURN_PROGRESS_TEXTURE, 0, 0, ARROW_WIDTH, ARROW_HEIGHT)
            .setTextureSize(ARROW_WIDTH, ARROW_HEIGHT)
            .build();
        this.flame = guiHelper.drawableBuilder(LIT_PROGRESS_TEXTURE, 0, 0, FLAME_WIDTH, FLAME_HEIGHT)
            .setTextureSize(FLAME_WIDTH, FLAME_HEIGHT)
            .build();
        this.x2Icon = guiHelper.drawableBuilder(X2_TEXTURE, 0, 0, X2_WIDTH, X2_HEIGHT)
            .setTextureSize(X2_WIDTH, X2_HEIGHT)
            .build();
    }

    @SuppressWarnings("null")
    @Override
    public @NotNull IRecipeType<RecipeEntry<FusionForgeRecipe>> getRecipeType() {
        return FusionForgeJeiPlugin.FUSION_FORGE_TYPE;
    }

    @SuppressWarnings("null")
    @Override
    public Text getTitle() {
        return Text.translatable("block.lategameplus.fusion_forge");
    }

    @Override
    public int getWidth() {
        return CATEGORY_WIDTH;
    }

    @Override
    public int getHeight() {
        return CATEGORY_HEIGHT;
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeEntry<FusionForgeRecipe> recipe, IFocusGroup focuses) {
        FusionForgeRecipe value = recipe.value();
        builder.addInputSlot(INPUT_A_X, INPUT_A_Y).add(value.getInputA());
        builder.addInputSlot(INPUT_B_X, INPUT_B_Y).add(value.getInputB());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, FUEL_X, FUEL_Y).add(new ItemStack(Items.COAL));
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, CATALYST_X, CATALYST_Y)
            .add(new ItemStack(Items.NETHER_STAR));
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).add(value.getOutput());
    }

    @Override
    public void draw(RecipeEntry<FusionForgeRecipe> recipe, IRecipeSlotsView recipeSlotsView, DrawContext context,
                     double mouseX, double mouseY) {
        background.draw(context);
        arrow.draw(context, ARROW_X, ARROW_Y);
        flame.draw(context, FLAME_X, FLAME_Y);
        x2Icon.draw(context, X2_X, X2_Y);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeEntry<FusionForgeRecipe> recipe,
                           IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= X2_X && mouseX < X2_X + X2_WIDTH && mouseY >= X2_Y && mouseY < X2_Y + X2_HEIGHT) {
            tooltip.add(Text.translatable(X2_TOOLTIP_KEY));
        }
    }
}
