package com.pedalhat.lategameplus.jei;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.recipe.FusionForgeRecipe;
import com.pedalhat.lategameplus.recipe.ModRecipes;
import com.pedalhat.lategameplus.registry.ModBlocks;
import com.pedalhat.lategameplus.registry.ModScreenHandlers;
import com.pedalhat.lategameplus.screen.FusionForgeScreen;
import com.pedalhat.lategameplus.screen.FusionForgeScreenHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;

import java.util.List;

@JeiPlugin
public class FusionForgeJeiPlugin implements IModPlugin {
    public static final Identifier PLUGIN_ID = Identifier.of(LateGamePlus.MOD_ID, "jei_plugin");
    @SuppressWarnings("unchecked")
    public static final IRecipeType<RecipeEntry<FusionForgeRecipe>> FUSION_FORGE_TYPE =
        (IRecipeType<RecipeEntry<FusionForgeRecipe>>) (IRecipeType<?>)
            IRecipeType.create(Identifier.of(LateGamePlus.MOD_ID, "fusion_forge"), RecipeEntry.class);
    private static IJeiRuntime runtime;
    private static List<RecipeEntry<FusionForgeRecipe>> cachedRecipes = List.of();

    @SuppressWarnings("null")
    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        LateGamePlus.LOGGER.info("[JEI] Registering Fusion Forge category");
        registration.addRecipeCategories(new FusionForgeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeEntry<FusionForgeRecipe>> recipes = getFusionForgeRecipesFromClient();
        LateGamePlus.LOGGER.info("[JEI] Initial Fusion Forge recipes: {}", recipes.size());
        if (!recipes.isEmpty()) {
            registration.addRecipes(FUSION_FORGE_TYPE, recipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(FUSION_FORGE_TYPE, new ItemStack(ModBlocks.FUSION_FORGE));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(FusionForgeScreen.class, 79, 34, 18, 16, FUSION_FORGE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            FusionForgeScreenHandler.class,
            ModScreenHandlers.FUSION_FORGE,
            FUSION_FORGE_TYPE,
            FusionForgeScreenHandler.INPUT_A_SLOT,
            2,
            FusionForgeScreenHandler.SLOT_COUNT,
            36
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        FusionForgeJeiPlugin.runtime = runtime;
        LateGamePlus.LOGGER.info("[JEI] Runtime available for Fusion Forge");
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> updateJeiRecipes(recipes));
        SynchronizedRecipes recipes = getSynchronizedRecipes();
        updateJeiRecipes(recipes);
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        cachedRecipes = List.of();
        LateGamePlus.LOGGER.info("[JEI] Runtime unavailable for Fusion Forge");
    }

    private static void updateJeiRecipes(SynchronizedRecipes recipes) {
        if (runtime == null || recipes == null) {
            if (runtime != null) {
                LateGamePlus.LOGGER.info("[JEI] Fusion Forge recipes skipped (no synchronized recipes yet)");
            }
            return;
        }
        List<RecipeEntry<FusionForgeRecipe>> newRecipes = extractRecipes(recipes);
        LateGamePlus.LOGGER.info("[JEI] Fusion Forge recipes synchronized: {}", newRecipes.size());
        if (newRecipes.equals(cachedRecipes)) {
            return;
        }
        if (!cachedRecipes.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(FUSION_FORGE_TYPE, cachedRecipes);
        }
        if (!newRecipes.isEmpty()) {
            runtime.getRecipeManager().addRecipes(FUSION_FORGE_TYPE, newRecipes);
        }
        cachedRecipes = newRecipes;
    }

    private static List<RecipeEntry<FusionForgeRecipe>> getFusionForgeRecipesFromClient() {
        SynchronizedRecipes recipes = getSynchronizedRecipes();
        if (recipes == null) {
            return List.of();
        }
        return extractRecipes(recipes);
    }

    @SuppressWarnings("null")
    private static SynchronizedRecipes getSynchronizedRecipes() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }
        RecipeManager recipeManager = null;
        if (client.getNetworkHandler() != null) {
            recipeManager = client.getNetworkHandler().getRecipeManager();
        } else if (client.getServer() != null) {
            recipeManager = client.getServer().getRecipeManager();
        }
        if (recipeManager instanceof FabricRecipeManager fabricRecipeManager) {
            return fabricRecipeManager.getSynchronizedRecipes();
        }
        return null;
    }

    private static List<RecipeEntry<FusionForgeRecipe>> extractRecipes(SynchronizedRecipes recipes) {
        return recipes.getAllOfType(ModRecipes.FUSION_FORGE).stream().toList();
    }
}
