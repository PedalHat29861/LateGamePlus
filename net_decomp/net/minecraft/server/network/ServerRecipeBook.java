/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.RecipeBookSettingsS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

public class ServerRecipeBook
extends RecipeBook {
    public static final String RECIPE_BOOK_KEY = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DisplayCollector collector;
    @VisibleForTesting
    protected final Set<RegistryKey<Recipe<?>>> unlocked = Sets.newIdentityHashSet();
    @VisibleForTesting
    protected final Set<RegistryKey<Recipe<?>>> highlighted = Sets.newIdentityHashSet();

    public ServerRecipeBook(DisplayCollector collector) {
        this.collector = collector;
    }

    public void unlock(RegistryKey<Recipe<?>> recipeKey) {
        this.unlocked.add(recipeKey);
    }

    public boolean isUnlocked(RegistryKey<Recipe<?>> recipeKey) {
        return this.unlocked.contains(recipeKey);
    }

    public void lock(RegistryKey<Recipe<?>> recipeKey) {
        this.unlocked.remove(recipeKey);
        this.highlighted.remove(recipeKey);
    }

    public void unmarkHighlighted(RegistryKey<Recipe<?>> recipeKey) {
        this.highlighted.remove(recipeKey);
    }

    private void markHighlighted(RegistryKey<Recipe<?>> recipeKey) {
        this.highlighted.add(recipeKey);
    }

    public int unlockRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        ArrayList<RecipeBookAddS2CPacket.Entry> list = new ArrayList<RecipeBookAddS2CPacket.Entry>();
        for (RecipeEntry<?> recipeEntry : recipes) {
            RegistryKey<Recipe<?>> registryKey = recipeEntry.id();
            if (this.unlocked.contains(registryKey) || recipeEntry.value().isIgnoredInRecipeBook()) continue;
            this.unlock(registryKey);
            this.markHighlighted(registryKey);
            this.collector.displaysForRecipe(registryKey, display -> list.add(new RecipeBookAddS2CPacket.Entry((RecipeDisplayEntry)display, recipeEntry.value().showNotification(), true)));
            Criteria.RECIPE_UNLOCKED.trigger(player, recipeEntry);
        }
        if (!list.isEmpty()) {
            player.networkHandler.sendPacket(new RecipeBookAddS2CPacket(list, false));
        }
        return list.size();
    }

    public int lockRecipes(Collection<RecipeEntry<?>> recipes, ServerPlayerEntity player) {
        ArrayList list = Lists.newArrayList();
        for (RecipeEntry<?> recipeEntry : recipes) {
            RegistryKey<Recipe<?>> registryKey = recipeEntry.id();
            if (!this.unlocked.contains(registryKey)) continue;
            this.lock(registryKey);
            this.collector.displaysForRecipe(registryKey, display -> list.add(display.id()));
        }
        if (!list.isEmpty()) {
            player.networkHandler.sendPacket(new RecipeBookRemoveS2CPacket(list));
        }
        return list.size();
    }

    private void handleList(List<RegistryKey<Recipe<?>>> recipes, Consumer<RegistryKey<Recipe<?>>> handler, Predicate<RegistryKey<Recipe<?>>> validPredicate) {
        for (RegistryKey<Recipe<?>> registryKey : recipes) {
            if (!validPredicate.test(registryKey)) {
                LOGGER.error("Tried to load unrecognized recipe: {} removed now.", registryKey);
                continue;
            }
            handler.accept(registryKey);
        }
    }

    public void sendInitRecipesPacket(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new RecipeBookSettingsS2CPacket(this.getOptions().copy()));
        ArrayList<RecipeBookAddS2CPacket.Entry> list = new ArrayList<RecipeBookAddS2CPacket.Entry>(this.unlocked.size());
        for (RegistryKey<Recipe<?>> registryKey : this.unlocked) {
            this.collector.displaysForRecipe(registryKey, display -> list.add(new RecipeBookAddS2CPacket.Entry((RecipeDisplayEntry)display, false, this.highlighted.contains(registryKey))));
        }
        player.networkHandler.sendPacket(new RecipeBookAddS2CPacket(list, true));
    }

    public void copyFrom(ServerRecipeBook recipeBook) {
        this.unpack(recipeBook.pack());
    }

    public Packed pack() {
        return new Packed(this.options.copy(), List.copyOf(this.unlocked), List.copyOf(this.highlighted));
    }

    private void unpack(Packed packed) {
        this.unlocked.clear();
        this.highlighted.clear();
        this.options.copyFrom(packed.settings);
        this.unlocked.addAll(packed.known);
        this.highlighted.addAll(packed.highlight);
    }

    public void unpack(Packed packed, Predicate<RegistryKey<Recipe<?>>> validPredicate) {
        this.options.copyFrom(packed.settings);
        this.handleList(packed.known, this.unlocked::add, validPredicate);
        this.handleList(packed.highlight, this.highlighted::add, validPredicate);
    }

    @FunctionalInterface
    public static interface DisplayCollector {
        public void displaysForRecipe(RegistryKey<Recipe<?>> var1, Consumer<RecipeDisplayEntry> var2);
    }

    public static final class Packed
    extends Record {
        final RecipeBookOptions settings;
        final List<RegistryKey<Recipe<?>>> known;
        final List<RegistryKey<Recipe<?>>> highlight;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RecipeBookOptions.CODEC.forGetter(Packed::settings), (App)Recipe.KEY_CODEC.listOf().fieldOf("recipes").forGetter(Packed::known), (App)Recipe.KEY_CODEC.listOf().fieldOf("toBeDisplayed").forGetter(Packed::highlight)).apply((Applicative)instance, Packed::new));

        public Packed(RecipeBookOptions settings, List<RegistryKey<Recipe<?>>> known, List<RegistryKey<Recipe<?>>> highlight) {
            this.settings = settings;
            this.known = known;
            this.highlight = highlight;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this, object);
        }

        public RecipeBookOptions settings() {
            return this.settings;
        }

        public List<RegistryKey<Recipe<?>>> known() {
            return this.known;
        }

        public List<RegistryKey<Recipe<?>>> highlight() {
            return this.highlight;
        }
    }
}

