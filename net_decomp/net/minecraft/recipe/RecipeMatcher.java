/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.objects.ObjectIterable
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntMaps
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.recipe;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class RecipeMatcher<T> {
    public final Reference2IntOpenHashMap<T> available = new Reference2IntOpenHashMap();

    boolean hasAtLeast(T input, int minimum) {
        return this.available.getInt(input) >= minimum;
    }

    void consume(T input, int count) {
        int i = this.available.addTo(input, -count);
        if (i < count) {
            throw new IllegalStateException("Took " + count + " items, but only had " + i);
        }
    }

    void addInput(T input, int count) {
        this.available.addTo(input, count);
    }

    public boolean match(List<? extends RawIngredient<T>> ingredients, int quantity, @Nullable ItemCallback<T> itemCallback) {
        return new Matcher(ingredients).match(quantity, itemCallback);
    }

    public int countCrafts(List<? extends RawIngredient<T>> ingredients, int max, @Nullable ItemCallback<T> itemCallback) {
        return new Matcher(ingredients).countCrafts(max, itemCallback);
    }

    public void clear() {
        this.available.clear();
    }

    public void add(T input, int count) {
        this.addInput(input, count);
    }

    List<T> createItemRequirementList(Iterable<? extends RawIngredient<T>> ingredients) {
        ArrayList<Object> list = new ArrayList<Object>();
        for (Reference2IntMap.Entry entry : Reference2IntMaps.fastIterable(this.available)) {
            if (entry.getIntValue() <= 0 || !RecipeMatcher.anyAccept(ingredients, entry.getKey())) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    private static <T> boolean anyAccept(Iterable<? extends RawIngredient<T>> ingredients, T item) {
        for (RawIngredient<T> rawIngredient : ingredients) {
            if (!rawIngredient.acceptsItem(item)) continue;
            return true;
        }
        return false;
    }

    @VisibleForTesting
    public int getMaximumCrafts(List<? extends RawIngredient<T>> ingredients) {
        int i = Integer.MAX_VALUE;
        ObjectIterable objectIterable = Reference2IntMaps.fastIterable(this.available);
        block0: for (RawIngredient<Object> rawIngredient : ingredients) {
            int j = 0;
            for (Reference2IntMap.Entry entry : objectIterable) {
                int k = entry.getIntValue();
                if (k <= j) continue;
                if (rawIngredient.acceptsItem(entry.getKey())) {
                    j = k;
                }
                if (j < i) continue;
                continue block0;
            }
            i = j;
            if (i != 0) continue;
            break;
        }
        return i;
    }

    class Matcher {
        private final List<? extends RawIngredient<T>> ingredients;
        private final int totalIngredients;
        private final List<T> requiredItems;
        private final int totalRequiredItems;
        private final BitSet bits;
        private final IntList ingredientItemLookup = new IntArrayList();

        public Matcher(List<? extends RawIngredient<T>> ingredients) {
            this.ingredients = ingredients;
            this.totalIngredients = ingredients.size();
            this.requiredItems = RecipeMatcher.this.createItemRequirementList(ingredients);
            this.totalRequiredItems = this.requiredItems.size();
            this.bits = new BitSet(this.getVisitedIngredientIndexCount() + this.getVisitedItemIndexCount() + this.getRequirementIndexCount() + this.getItemMatchIndexCount() + this.getMissingIndexCount());
            this.initItemMatch();
        }

        private void initItemMatch() {
            for (int i = 0; i < this.totalIngredients; ++i) {
                RawIngredient rawIngredient = this.ingredients.get(i);
                for (int j = 0; j < this.totalRequiredItems; ++j) {
                    if (!rawIngredient.acceptsItem(this.requiredItems.get(j))) continue;
                    this.setMatch(j, i);
                }
            }
        }

        public boolean match(int quantity, @Nullable ItemCallback<T> itemCallback) {
            int l;
            int k;
            IntList intList;
            if (quantity <= 0) {
                return true;
            }
            int i = 0;
            while ((intList = this.tryFindIngredientItemLookup(quantity)) != null) {
                int j = intList.getInt(0);
                RecipeMatcher.this.consume(this.requiredItems.get(j), quantity);
                k = intList.size() - 1;
                this.unfulfillRequirement(intList.getInt(k));
                ++i;
                for (l = 0; l < intList.size() - 1; ++l) {
                    int n;
                    int m;
                    if (Matcher.isItem(l)) {
                        m = intList.getInt(l);
                        n = intList.getInt(l + 1);
                        this.markMissing(m, n);
                        continue;
                    }
                    m = intList.getInt(l + 1);
                    n = intList.getInt(l);
                    this.markNotMissing(m, n);
                }
            }
            boolean bl = i == this.totalIngredients;
            boolean bl2 = bl && itemCallback != null;
            this.clearVisited();
            this.clearRequirements();
            block2: for (k = 0; k < this.totalIngredients; ++k) {
                for (l = 0; l < this.totalRequiredItems; ++l) {
                    if (!this.isMissing(l, k)) continue;
                    this.markNotMissing(l, k);
                    RecipeMatcher.this.addInput(this.requiredItems.get(l), quantity);
                    if (!bl2) continue block2;
                    itemCallback.accept(this.requiredItems.get(l));
                    continue block2;
                }
            }
            assert (this.bits.get(this.getMissingIndexOffset(), this.getMissingIndexOffset() + this.getMissingIndexCount()).isEmpty());
            return bl;
        }

        private static boolean isItem(int index) {
            return (index & 1) == 0;
        }

        private @Nullable IntList tryFindIngredientItemLookup(int min) {
            this.clearVisited();
            for (int i = 0; i < this.totalRequiredItems; ++i) {
                IntList intList;
                if (!RecipeMatcher.this.hasAtLeast(this.requiredItems.get(i), min) || (intList = this.findIngredientItemLookup(i)) == null) continue;
                return intList;
            }
            return null;
        }

        private @Nullable IntList findIngredientItemLookup(int itemIndex) {
            this.ingredientItemLookup.clear();
            this.markItemVisited(itemIndex);
            this.ingredientItemLookup.add(itemIndex);
            while (!this.ingredientItemLookup.isEmpty()) {
                int j;
                int i = this.ingredientItemLookup.size();
                if (Matcher.isItem(i - 1)) {
                    j = this.ingredientItemLookup.getInt(i - 1);
                    for (k = 0; k < this.totalIngredients; ++k) {
                        if (this.hasVisitedIngredient(k) || !this.matches(j, k) || this.isMissing(j, k)) continue;
                        this.markIngredientVisited(k);
                        this.ingredientItemLookup.add(k);
                        break;
                    }
                } else {
                    j = this.ingredientItemLookup.getInt(i - 1);
                    if (!this.getRequirement(j)) {
                        return this.ingredientItemLookup;
                    }
                    for (k = 0; k < this.totalRequiredItems; ++k) {
                        if (this.isRequirementUnfulfilled(k) || !this.isMissing(k, j)) continue;
                        assert (this.matches(k, j));
                        this.markItemVisited(k);
                        this.ingredientItemLookup.add(k);
                        break;
                    }
                }
                if ((j = this.ingredientItemLookup.size()) != i) continue;
                this.ingredientItemLookup.removeInt(j - 1);
            }
            return null;
        }

        private int getVisitedIngredientIndexOffset() {
            return 0;
        }

        private int getVisitedIngredientIndexCount() {
            return this.totalIngredients;
        }

        private int getVisitedItemIndexOffset() {
            return this.getVisitedIngredientIndexOffset() + this.getVisitedIngredientIndexCount();
        }

        private int getVisitedItemIndexCount() {
            return this.totalRequiredItems;
        }

        private int getRequirementIndexOffset() {
            return this.getVisitedItemIndexOffset() + this.getVisitedItemIndexCount();
        }

        private int getRequirementIndexCount() {
            return this.totalIngredients;
        }

        private int getItemMatchIndexOffset() {
            return this.getRequirementIndexOffset() + this.getRequirementIndexCount();
        }

        private int getItemMatchIndexCount() {
            return this.totalIngredients * this.totalRequiredItems;
        }

        private int getMissingIndexOffset() {
            return this.getItemMatchIndexOffset() + this.getItemMatchIndexCount();
        }

        private int getMissingIndexCount() {
            return this.totalIngredients * this.totalRequiredItems;
        }

        private boolean getRequirement(int itemId) {
            return this.bits.get(this.getRequirementIndex(itemId));
        }

        private void unfulfillRequirement(int itemId) {
            this.bits.set(this.getRequirementIndex(itemId));
        }

        private int getRequirementIndex(int itemId) {
            assert (itemId >= 0 && itemId < this.totalIngredients);
            return this.getRequirementIndexOffset() + itemId;
        }

        private void clearRequirements() {
            this.clear(this.getRequirementIndexOffset(), this.getRequirementIndexCount());
        }

        private void setMatch(int itemIndex, int ingredientIndex) {
            this.bits.set(this.getMatchIndex(itemIndex, ingredientIndex));
        }

        private boolean matches(int itemIndex, int ingredientIndex) {
            return this.bits.get(this.getMatchIndex(itemIndex, ingredientIndex));
        }

        private int getMatchIndex(int itemIndex, int ingredientIndex) {
            assert (itemIndex >= 0 && itemIndex < this.totalRequiredItems);
            assert (ingredientIndex >= 0 && ingredientIndex < this.totalIngredients);
            return this.getItemMatchIndexOffset() + itemIndex * this.totalIngredients + ingredientIndex;
        }

        private boolean isMissing(int itemIndex, int ingredientIndex) {
            return this.bits.get(this.getMissingIndex(itemIndex, ingredientIndex));
        }

        private void markMissing(int itemIndex, int ingredientIndex) {
            int i = this.getMissingIndex(itemIndex, ingredientIndex);
            assert (!this.bits.get(i));
            this.bits.set(i);
        }

        private void markNotMissing(int itemIndex, int ingredientIndex) {
            int i = this.getMissingIndex(itemIndex, ingredientIndex);
            assert (this.bits.get(i));
            this.bits.clear(i);
        }

        private int getMissingIndex(int itemIndex, int ingredientIndex) {
            assert (itemIndex >= 0 && itemIndex < this.totalRequiredItems);
            assert (ingredientIndex >= 0 && ingredientIndex < this.totalIngredients);
            return this.getMissingIndexOffset() + itemIndex * this.totalIngredients + ingredientIndex;
        }

        private void markIngredientVisited(int index) {
            this.bits.set(this.getVisitedIngredientIndex(index));
        }

        private boolean hasVisitedIngredient(int index) {
            return this.bits.get(this.getVisitedIngredientIndex(index));
        }

        private int getVisitedIngredientIndex(int index) {
            assert (index >= 0 && index < this.totalIngredients);
            return this.getVisitedIngredientIndexOffset() + index;
        }

        private void markItemVisited(int index) {
            this.bits.set(this.getVisitedItemIndex(index));
        }

        private boolean isRequirementUnfulfilled(int index) {
            return this.bits.get(this.getVisitedItemIndex(index));
        }

        private int getVisitedItemIndex(int index) {
            assert (index >= 0 && index < this.totalRequiredItems);
            return this.getVisitedItemIndexOffset() + index;
        }

        private void clearVisited() {
            this.clear(this.getVisitedIngredientIndexOffset(), this.getVisitedIngredientIndexCount());
            this.clear(this.getVisitedItemIndexOffset(), this.getVisitedItemIndexCount());
        }

        private void clear(int start, int offset) {
            this.bits.clear(start, start + offset);
        }

        public int countCrafts(int max, @Nullable ItemCallback<T> itemCallback) {
            int k;
            int i = 0;
            int j = Math.min(max, RecipeMatcher.this.getMaximumCrafts(this.ingredients)) + 1;
            while (true) {
                if (this.match(k = (i + j) / 2, null)) {
                    if (j - i <= 1) break;
                    i = k;
                    continue;
                }
                j = k;
            }
            if (k > 0) {
                this.match(k, itemCallback);
            }
            return k;
        }
    }

    @FunctionalInterface
    public static interface ItemCallback<T> {
        public void accept(T var1);
    }

    @FunctionalInterface
    public static interface RawIngredient<T> {
        public boolean acceptsItem(T var1);
    }
}

