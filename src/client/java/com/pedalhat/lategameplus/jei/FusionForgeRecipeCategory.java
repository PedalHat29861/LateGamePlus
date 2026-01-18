package com.pedalhat.lategameplus.jei;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.recipe.FusionForgeRecipe;
import com.pedalhat.lategameplus.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import java.util.Locale;

public class FusionForgeRecipeCategory implements IRecipeCategory<RecipeEntry<FusionForgeRecipe>> {
    private static final Identifier TEXTURE = Identifier.of(LateGamePlus.MOD_ID, "textures/gui/container/fusion_forge_jei.png");
    private static final Identifier BURN_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/burn_progress.png");
    private static final Identifier LIT_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/lit_progress.png");
    private static final Identifier X2_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/fusion_forge_x2.png");
    private static final int BACKGROUND_WIDTH = 127;
    private static final int BACKGROUND_HEIGHT = 61;
    private static final int CATEGORY_WIDTH = 127;
    private static final int CATEGORY_HEIGHT = BACKGROUND_HEIGHT;
    private static final int INPUT_A_X = 22;
    private static final int INPUT_A_Y = 5;
    private static final int INPUT_B_X = 59;
    private static final int INPUT_B_Y = 5;
    private static final int FUEL_X = 41;
    private static final int FUEL_Y = 40;
    private static final int CATALYST_X = 6;
    private static final int CATALYST_Y = 36;
    private static final int OUTPUT_X = 101;
    private static final int OUTPUT_Y = 23;
    private static final int ARROW_X = 70;
    private static final int ARROW_Y = 25;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int FLAME_X = 41;
    private static final int FLAME_Y = 23;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;
    private static final int X2_WIDTH = 11;
    private static final int X2_HEIGHT = 7;
    private static final int X2_X = CATALYST_X + 2;
    private static final int X2_Y = CATALYST_Y - 10;
    private static final String X2_TOOLTIP_KEY = "tooltip.lategameplus.fusion_forge.catalyst_x2";
    private static final int XP_TEXT_X_OFFSET = -6;
    private static final int XP_TEXT_Y_OFFSET = -16;
    private static final int TXT_COLOR = 0xFF7e7e7e;
    private static final int TIME_TEXT_X_OFFSET =  0;
    private static final int TIME_TEXT_Y_OFFSET = 25;
    private static final int COAL_BURN_TICKS = 1600;

    private final IGuiHelper guiHelper;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic arrowBase;
    private final IDrawableStatic flameBase;
    private final IDrawable x2Icon;

    public FusionForgeRecipeCategory(IGuiHelper guiHelper) {
        this.guiHelper = guiHelper;
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT)
            .setTextureSize(BACKGROUND_WIDTH, BACKGROUND_HEIGHT)
            .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.FUSION_FORGE));
        this.arrowBase = guiHelper.drawableBuilder(BURN_PROGRESS_TEXTURE, 0, 0, ARROW_WIDTH, ARROW_HEIGHT)
            .setTextureSize(ARROW_WIDTH, ARROW_HEIGHT)
            .build();
        this.flameBase = guiHelper.drawableBuilder(LIT_PROGRESS_TEXTURE, 0, 0, FLAME_WIDTH, FLAME_HEIGHT)
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
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeEntry<FusionForgeRecipe> recipe,
                                   IFocusGroup focuses) {
        FusionForgeRecipe value = recipe.value();
        int cookTime = Math.max(1, value.getCookTime());
        int fuelCost = Math.max(1, value.getFuelCost());
        int fuelPerTick = Math.max(1, fuelCost / cookTime);
        int flameTicks = Math.max(1, COAL_BURN_TICKS / fuelPerTick);

        IDrawableAnimated arrowAnimated = guiHelper.createAnimatedDrawable(
            arrowBase, cookTime, IDrawableAnimated.StartDirection.LEFT, false);
        IDrawableAnimated flameAnimated = guiHelper.createAnimatedDrawable(
            flameBase, flameTicks, IDrawableAnimated.StartDirection.TOP, true);

        builder.addDrawable(arrowAnimated, ARROW_X, ARROW_Y);
        builder.addDrawable(flameAnimated, FLAME_X, FLAME_Y);
    }

    @Override
    public void draw(RecipeEntry<FusionForgeRecipe> recipe, IRecipeSlotsView recipeSlotsView, DrawContext context,
                     double mouseX, double mouseY) {
        background.draw(context);
        x2Icon.draw(context, X2_X, X2_Y);
        drawTimeText(recipe, context);
        drawExperienceText(recipe, context);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeEntry<FusionForgeRecipe> recipe,
                           IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= X2_X && mouseX < X2_X + X2_WIDTH && mouseY >= X2_Y && mouseY < X2_Y + X2_HEIGHT) {
            tooltip.add(Text.translatable(X2_TOOLTIP_KEY));
        }
    }

    private static void drawExperienceText(RecipeEntry<FusionForgeRecipe> recipe, DrawContext context) {
        float exp = recipe.value().getExperience();
        if (exp <= 0.0f) {
            return;
        }
        String text = formatExperience(exp) + " XP";
        context.drawText(MinecraftClient.getInstance().textRenderer, text,
            OUTPUT_X + XP_TEXT_X_OFFSET, OUTPUT_Y + XP_TEXT_Y_OFFSET, TXT_COLOR, false);
    }

    private static void drawTimeText(RecipeEntry<FusionForgeRecipe> recipe, DrawContext context) {
        int cookTime = recipe.value().getCookTime();
        if (cookTime <= 0) {
            return;
        }
        String text = formatSeconds(cookTime) + "s";
        context.drawText(MinecraftClient.getInstance().textRenderer, text,
            OUTPUT_X + TIME_TEXT_X_OFFSET, OUTPUT_Y + TIME_TEXT_Y_OFFSET, TXT_COLOR, false);
    }

    private static String formatExperience(float exp) {
        if (Math.abs(exp - Math.round(exp)) < 0.001f) {
            return Integer.toString(Math.round(exp));
        }
        return String.format(Locale.US, "%.1f", exp);
    }

    private static String formatSeconds(int cookTimeTicks) {
        float seconds = cookTimeTicks / 20.0f;
        if (Math.abs(seconds - Math.round(seconds)) < 0.01f) {
            return Integer.toString(Math.round(seconds));
        }
        return String.format(Locale.US, "%.1f", seconds);
    }
}
