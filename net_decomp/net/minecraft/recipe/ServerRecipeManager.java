/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.recipe;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SingleStackRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerRecipeManager
extends SinglePreparationResourceReloader<PreparedRecipes>
implements RecipeManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<RegistryKey<RecipePropertySet>, SoleIngredientGetter> SOLE_INGREDIENT_GETTERS = Map.of(RecipePropertySet.SMITHING_ADDITION, recipe -> {
        Optional<Object> optional;
        if (recipe instanceof SmithingRecipe) {
            SmithingRecipe smithingRecipe = (SmithingRecipe)recipe;
            optional = smithingRecipe.addition();
        } else {
            optional = Optional.empty();
        }
        return optional;
    }, RecipePropertySet.SMITHING_BASE, recipe -> {
        Optional<Object> optional;
        if (recipe instanceof SmithingRecipe) {
            SmithingRecipe smithingRecipe = (SmithingRecipe)recipe;
            optional = Optional.of(smithingRecipe.base());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }, RecipePropertySet.SMITHING_TEMPLATE, recipe -> {
        Optional<Object> optional;
        if (recipe instanceof SmithingRecipe) {
            SmithingRecipe smithingRecipe = (SmithingRecipe)recipe;
            optional = smithingRecipe.template();
        } else {
            optional = Optional.empty();
        }
        return optional;
    }, RecipePropertySet.FURNACE_INPUT, ServerRecipeManager.cookingIngredientGetter(RecipeType.SMELTING), RecipePropertySet.BLAST_FURNACE_INPUT, ServerRecipeManager.cookingIngredientGetter(RecipeType.BLASTING), RecipePropertySet.SMOKER_INPUT, ServerRecipeManager.cookingIngredientGetter(RecipeType.SMOKING), RecipePropertySet.CAMPFIRE_INPUT, ServerRecipeManager.cookingIngredientGetter(RecipeType.CAMPFIRE_COOKING));
    private static final ResourceFinder FINDER = ResourceFinder.json(RegistryKeys.RECIPE);
    private final RegistryWrapper.WrapperLookup registries;
    private PreparedRecipes preparedRecipes = PreparedRecipes.EMPTY;
    private Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySets = Map.of();
    private CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecutterRecipes = CuttingRecipeDisplay.Grouping.empty();
    private List<ServerRecipe> recipes = List.of();
    private Map<RegistryKey<Recipe<?>>, List<ServerRecipe>> recipesByKey = Map.of();

    public ServerRecipeManager(RegistryWrapper.WrapperLookup registries) {
        this.registries = registries;
    }

    @Override
    protected PreparedRecipes prepare(ResourceManager resourceManager, Profiler profiler) {
        TreeMap<Identifier, Recipe> sortedMap = new TreeMap<Identifier, Recipe>();
        JsonDataLoader.load(resourceManager, FINDER, this.registries.getOps(JsonOps.INSTANCE), Recipe.CODEC, sortedMap);
        ArrayList list = new ArrayList(sortedMap.size());
        sortedMap.forEach((id, recipe) -> {
            RegistryKey<Recipe<?>> registryKey = RegistryKey.of(RegistryKeys.RECIPE, id);
            RecipeEntry<Recipe> recipeEntry = new RecipeEntry<Recipe>(registryKey, (Recipe)recipe);
            list.add(recipeEntry);
        });
        return PreparedRecipes.of(list);
    }

    @Override
    protected void apply(PreparedRecipes preparedRecipes, ResourceManager resourceManager, Profiler profiler) {
        this.preparedRecipes = preparedRecipes;
        LOGGER.info("Loaded {} recipes", (Object)preparedRecipes.recipes().size());
    }

    public void initialize(FeatureSet features) {
        ArrayList list = new ArrayList();
        List<PropertySetBuilder> list2 = SOLE_INGREDIENT_GETTERS.entrySet().stream().map(entry -> new PropertySetBuilder((RegistryKey)entry.getKey(), (SoleIngredientGetter)entry.getValue())).toList();
        this.preparedRecipes.recipes().forEach(recipe -> {
            Object recipe2 = recipe.value();
            if (!recipe2.isIgnoredInRecipeBook() && recipe2.getIngredientPlacement().hasNoPlacement()) {
                LOGGER.warn("Recipe {} can't be placed due to empty ingredients and will be ignored", (Object)recipe.id().getValue());
                return;
            }
            list2.forEach(builder -> builder.accept((Recipe<?>)recipe2));
            if (recipe2 instanceof StonecuttingRecipe) {
                StonecuttingRecipe stonecuttingRecipe = (StonecuttingRecipe)recipe2;
                RecipeEntry recipeEntry = recipe;
                if (ServerRecipeManager.isEnabled(features, stonecuttingRecipe.ingredient()) && stonecuttingRecipe.createResultDisplay().isEnabled(features)) {
                    list.add(new CuttingRecipeDisplay.GroupEntry(stonecuttingRecipe.ingredient(), new CuttingRecipeDisplay(stonecuttingRecipe.createResultDisplay(), Optional.of(recipeEntry))));
                }
            }
        });
        this.propertySets = list2.stream().collect(Collectors.toUnmodifiableMap(builder -> builder.propertySetKey, builder -> builder.build(features)));
        this.stonecutterRecipes = new CuttingRecipeDisplay.Grouping(list);
        this.recipes = ServerRecipeManager.collectServerRecipes(this.preparedRecipes.recipes(), features);
        this.recipesByKey = this.recipes.stream().collect(Collectors.groupingBy(recipe -> recipe.parent.id(), IdentityHashMap::new, Collectors.toList()));
    }

    static List<Ingredient> filterIngredients(FeatureSet features, List<Ingredient> ingredients) {
        ingredients.removeIf(ingredient -> !ServerRecipeManager.isEnabled(features, ingredient));
        return ingredients;
    }

    private static boolean isEnabled(FeatureSet features, Ingredient ingredient) {
        return ingredient.getMatchingItems().allMatch(entry -> ((Item)entry.value()).isEnabled(features));
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable RegistryKey<Recipe<?>> recipe) {
        RecipeEntry<T> recipeEntry = recipe != null ? this.get(type, recipe) : null;
        return this.getFirstMatch(type, input, world, recipeEntry);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable RecipeEntry<T> recipe) {
        if (recipe != null && recipe.value().matches(input, world)) {
            return Optional.of(recipe);
        }
        return this.getFirstMatch(type, input, world);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world) {
        return this.preparedRecipes.find(type, input, world).findFirst();
    }

    public Optional<RecipeEntry<?>> get(RegistryKey<Recipe<?>> key) {
        return Optional.ofNullable(this.preparedRecipes.get(key));
    }

    private <T extends Recipe<?>> @Nullable RecipeEntry<T> get(RecipeType<T> type, RegistryKey<Recipe<?>> key) {
        RecipeEntry<?> recipeEntry = this.preparedRecipes.get(key);
        if (recipeEntry != null && recipeEntry.value().getType().equals(type)) {
            return recipeEntry;
        }
        return null;
    }

    public Map<RegistryKey<RecipePropertySet>, RecipePropertySet> getPropertySets() {
        return this.propertySets;
    }

    public CuttingRecipeDisplay.Grouping<StonecuttingRecipe> getStonecutterRecipeForSync() {
        return this.stonecutterRecipes;
    }

    @Override
    public RecipePropertySet getPropertySet(RegistryKey<RecipePropertySet> key) {
        return this.propertySets.getOrDefault(key, RecipePropertySet.EMPTY);
    }

    @Override
    public CuttingRecipeDisplay.Grouping<StonecuttingRecipe> getStonecutterRecipes() {
        return this.stonecutterRecipes;
    }

    public Collection<RecipeEntry<?>> values() {
        return this.preparedRecipes.recipes();
    }

    public @Nullable ServerRecipe get(NetworkRecipeId id) {
        int i = id.index();
        return i >= 0 && i < this.recipes.size() ? this.recipes.get(i) : null;
    }

    public void forEachRecipeDisplay(RegistryKey<Recipe<?>> key, Consumer<RecipeDisplayEntry> action) {
        List<ServerRecipe> list = this.recipesByKey.get(key);
        if (list != null) {
            list.forEach(recipe -> action.accept(recipe.display));
        }
    }

    @VisibleForTesting
    protected static RecipeEntry<?> deserialize(RegistryKey<Recipe<?>> key, JsonObject json, RegistryWrapper.WrapperLookup registries) {
        Recipe recipe = (Recipe)Recipe.CODEC.parse(registries.getOps(JsonOps.INSTANCE), (Object)json).getOrThrow(JsonParseException::new);
        return new RecipeEntry<Recipe>(key, recipe);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> MatchGetter<I, T> createCachedMatchGetter(final RecipeType<T> type) {
        return new MatchGetter<I, T>(){
            private @Nullable RegistryKey<Recipe<?>> id;

            @Override
            public Optional<RecipeEntry<T>> getFirstMatch(I input, ServerWorld world) {
                ServerRecipeManager serverRecipeManager = world.getRecipeManager();
                Optional optional = serverRecipeManager.getFirstMatch(type, input, (World)world, this.id);
                if (optional.isPresent()) {
                    RecipeEntry recipeEntry = optional.get();
                    this.id = recipeEntry.id();
                    return Optional.of(recipeEntry);
                }
                return Optional.empty();
            }
        };
    }

    private static List<ServerRecipe> collectServerRecipes(Iterable<RecipeEntry<?>> recipes, FeatureSet enabledFeatures) {
        ArrayList<ServerRecipe> list = new ArrayList<ServerRecipe>();
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        for (RecipeEntry<?> recipeEntry : recipes) {
            Object recipe = recipeEntry.value();
            OptionalInt optionalInt = recipe.getGroup().isEmpty() ? OptionalInt.empty() : OptionalInt.of(object2IntMap.computeIfAbsent((Object)recipe.getGroup(), arg_0 -> ServerRecipeManager.method_64687((Object2IntMap)object2IntMap, arg_0)));
            Optional<Object> optional = recipe.isIgnoredInRecipeBook() ? Optional.empty() : Optional.of(recipe.getIngredientPlacement().getIngredients());
            for (RecipeDisplay recipeDisplay : recipe.getDisplays()) {
                if (!recipeDisplay.isEnabled(enabledFeatures)) continue;
                int i = list.size();
                NetworkRecipeId networkRecipeId = new NetworkRecipeId(i);
                RecipeDisplayEntry recipeDisplayEntry = new RecipeDisplayEntry(networkRecipeId, recipeDisplay, optionalInt, recipe.getRecipeBookCategory(), optional);
                list.add(new ServerRecipe(recipeDisplayEntry, recipeEntry));
            }
        }
        return list;
    }

    private static SoleIngredientGetter cookingIngredientGetter(RecipeType<? extends SingleStackRecipe> expectedType) {
        return recipe -> {
            Optional<Object> optional;
            if (recipe.getType() == expectedType && recipe instanceof SingleStackRecipe) {
                SingleStackRecipe singleStackRecipe = (SingleStackRecipe)recipe;
                optional = Optional.of(singleStackRecipe.ingredient());
            } else {
                optional = Optional.empty();
            }
            return optional;
        };
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }

    private static /* synthetic */ int method_64687(Object2IntMap object2IntMap, Object group) {
        return object2IntMap.size();
    }

    public static final class ServerRecipe
    extends Record {
        final RecipeDisplayEntry display;
        final RecipeEntry<?> parent;

        public ServerRecipe(RecipeDisplayEntry display, RecipeEntry<?> parent) {
            this.display = display;
            this.parent = parent;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ServerRecipe.class, "display;parent", "display", "parent"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ServerRecipe.class, "display;parent", "display", "parent"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ServerRecipe.class, "display;parent", "display", "parent"}, this, object);
        }

        public RecipeDisplayEntry display() {
            return this.display;
        }

        public RecipeEntry<?> parent() {
            return this.parent;
        }
    }

    @FunctionalInterface
    public static interface SoleIngredientGetter {
        public Optional<Ingredient> apply(Recipe<?> var1);
    }

    public static class PropertySetBuilder
    implements Consumer<Recipe<?>> {
        final RegistryKey<RecipePropertySet> propertySetKey;
        private final SoleIngredientGetter ingredientGetter;
        private final List<Ingredient> ingredients = new ArrayList<Ingredient>();

        protected PropertySetBuilder(RegistryKey<RecipePropertySet> propertySetKey, SoleIngredientGetter ingredientGetter) {
            this.propertySetKey = propertySetKey;
            this.ingredientGetter = ingredientGetter;
        }

        @Override
        public void accept(Recipe<?> recipe) {
            this.ingredientGetter.apply(recipe).ifPresent(this.ingredients::add);
        }

        public RecipePropertySet build(FeatureSet enabledFeatures) {
            return RecipePropertySet.of(ServerRecipeManager.filterIngredients(enabledFeatures, this.ingredients));
        }

        @Override
        public /* synthetic */ void accept(Object recipe) {
            this.accept((Recipe)recipe);
        }
    }

    public static interface MatchGetter<I extends RecipeInput, T extends Recipe<I>> {
        public Optional<RecipeEntry<T>> getFirstMatch(I var1, ServerWorld var2);
    }
}

