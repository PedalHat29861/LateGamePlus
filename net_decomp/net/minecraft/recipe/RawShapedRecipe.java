/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.chars.CharArraySet
 *  it.unimi.dsi.fastutil.chars.CharSet
 */
package net.minecraft.recipe;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

public final class RawShapedRecipe {
    private static final int MAX_WIDTH_AND_HEIGHT = 3;
    public static final char SPACE = ' ';
    public static final MapCodec<RawShapedRecipe> CODEC = Data.CODEC.flatXmap(RawShapedRecipe::fromData, recipe -> recipe.data.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe")));
    public static final PacketCodec<RegistryByteBuf, RawShapedRecipe> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, recipe -> recipe.width, PacketCodecs.VAR_INT, recipe -> recipe.height, Ingredient.OPTIONAL_PACKET_CODEC.collect(PacketCodecs.toList()), recipe -> recipe.ingredients, RawShapedRecipe::create);
    private final int width;
    private final int height;
    private final List<Optional<Ingredient>> ingredients;
    private final Optional<Data> data;
    private final int ingredientCount;
    private final boolean symmetrical;

    public RawShapedRecipe(int width, int height, List<Optional<Ingredient>> ingredients, Optional<Data> data) {
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.data = data;
        this.ingredientCount = (int)ingredients.stream().flatMap(Optional::stream).count();
        this.symmetrical = Util.isSymmetrical(width, height, ingredients);
    }

    private static RawShapedRecipe create(Integer width, Integer height, List<Optional<Ingredient>> ingredients) {
        return new RawShapedRecipe(width, height, ingredients, Optional.empty());
    }

    public static RawShapedRecipe create(Map<Character, Ingredient> key, String ... pattern) {
        return RawShapedRecipe.create(key, List.of(pattern));
    }

    public static RawShapedRecipe create(Map<Character, Ingredient> key, List<String> pattern) {
        Data data = new Data(key, pattern);
        return (RawShapedRecipe)RawShapedRecipe.fromData(data).getOrThrow();
    }

    private static DataResult<RawShapedRecipe> fromData(Data data) {
        String[] strings = RawShapedRecipe.removePadding(data.pattern);
        int i = strings[0].length();
        int j = strings.length;
        ArrayList<Optional<Ingredient>> list = new ArrayList<Optional<Ingredient>>(i * j);
        CharArraySet charSet = new CharArraySet(data.key.keySet());
        for (String string : strings) {
            for (int k = 0; k < string.length(); ++k) {
                Optional<Object> optional;
                char c = string.charAt(k);
                if (c == ' ') {
                    optional = Optional.empty();
                } else {
                    Ingredient ingredient = data.key.get(Character.valueOf(c));
                    if (ingredient == null) {
                        return DataResult.error(() -> "Pattern references symbol '" + c + "' but it's not defined in the key");
                    }
                    optional = Optional.of(ingredient);
                }
                charSet.remove(c);
                list.add(optional);
            }
        }
        if (!charSet.isEmpty()) {
            return DataResult.error(() -> RawShapedRecipe.method_55082((CharSet)charSet));
        }
        return DataResult.success((Object)new RawShapedRecipe(i, j, list, Optional.of(data)));
    }

    @VisibleForTesting
    static String[] removePadding(List<String> pattern) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;
        for (int m = 0; m < pattern.size(); ++m) {
            String string = pattern.get(m);
            i = Math.min(i, RawShapedRecipe.findFirstSymbol(string));
            int n = RawShapedRecipe.findLastSymbol(string);
            j = Math.max(j, n);
            if (n < 0) {
                if (k == m) {
                    ++k;
                }
                ++l;
                continue;
            }
            l = 0;
        }
        if (pattern.size() == l) {
            return new String[0];
        }
        String[] strings = new String[pattern.size() - l - k];
        for (int o = 0; o < strings.length; ++o) {
            strings[o] = pattern.get(o + k).substring(i, j + 1);
        }
        return strings;
    }

    private static int findFirstSymbol(String line) {
        int i;
        for (i = 0; i < line.length() && line.charAt(i) == ' '; ++i) {
        }
        return i;
    }

    private static int findLastSymbol(String line) {
        int i;
        for (i = line.length() - 1; i >= 0 && line.charAt(i) == ' '; --i) {
        }
        return i;
    }

    public boolean matches(CraftingRecipeInput input) {
        if (input.getStackCount() != this.ingredientCount) {
            return false;
        }
        if (input.getWidth() == this.width && input.getHeight() == this.height) {
            if (!this.symmetrical && this.matches(input, true)) {
                return true;
            }
            if (this.matches(input, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(CraftingRecipeInput input, boolean mirrored) {
        for (int i = 0; i < this.height; ++i) {
            for (int j = 0; j < this.width; ++j) {
                ItemStack itemStack;
                Optional<Ingredient> optional = mirrored ? this.ingredients.get(this.width - j - 1 + i * this.width) : this.ingredients.get(j + i * this.width);
                if (Ingredient.matches(optional, itemStack = input.getStackInSlot(j, i))) continue;
                return false;
            }
        }
        return true;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public List<Optional<Ingredient>> getIngredients() {
        return this.ingredients;
    }

    private static /* synthetic */ String method_55082(CharSet charSet) {
        return "Key defines symbols that aren't used in pattern: " + String.valueOf(charSet);
    }

    public static final class Data
    extends Record {
        final Map<Character, Ingredient> key;
        final List<String> pattern;
        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap(pattern -> {
            if (pattern.size() > 3) {
                return DataResult.error(() -> "Invalid pattern: too many rows, 3 is maximum");
            }
            if (pattern.isEmpty()) {
                return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            }
            int i = ((String)pattern.getFirst()).length();
            for (String string : pattern) {
                if (string.length() > 3) {
                    return DataResult.error(() -> "Invalid pattern: too many columns, 3 is maximum");
                }
                if (i == string.length()) continue;
                return DataResult.error(() -> "Invalid pattern: each row must be the same width");
            }
            return DataResult.success((Object)pattern);
        }, Function.identity());
        private static final Codec<Character> KEY_ENTRY_CODEC = Codec.STRING.comapFlatMap(keyEntry -> {
            if (keyEntry.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + keyEntry + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(keyEntry)) {
                return DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.");
            }
            return DataResult.success((Object)Character.valueOf(keyEntry.charAt(0)));
        }, String::valueOf);
        public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.strictUnboundedMap(KEY_ENTRY_CODEC, Ingredient.CODEC).fieldOf("key").forGetter(data -> data.key), (App)PATTERN_CODEC.fieldOf("pattern").forGetter(data -> data.pattern)).apply((Applicative)instance, Data::new));

        public Data(Map<Character, Ingredient> key, List<String> pattern) {
            this.key = key;
            this.pattern = pattern;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this, object);
        }

        public Map<Character, Ingredient> key() {
            return this.key;
        }

        public List<String> pattern() {
            return this.pattern;
        }
    }
}

