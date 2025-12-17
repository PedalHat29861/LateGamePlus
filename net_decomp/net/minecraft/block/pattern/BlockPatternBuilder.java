/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.chars.CharOpenHashSet
 *  it.unimi.dsi.fastutil.chars.CharSet
 *  org.apache.commons.lang3.ArrayUtils
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.CachedBlockPosition;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class BlockPatternBuilder {
    private final List<String[]> aisles = Lists.newArrayList();
    private final Map<Character, Predicate<@Nullable CachedBlockPosition>> charMap = Maps.newHashMap();
    private int height;
    private int width;
    private final CharSet keysMissingPredicates = new CharOpenHashSet();

    private BlockPatternBuilder() {
        this.charMap.put(Character.valueOf(' '), pos -> true);
    }

    public BlockPatternBuilder aisle(String ... pattern) {
        if (ArrayUtils.isEmpty((Object[])pattern) || StringUtils.isEmpty((CharSequence)pattern[0])) {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
        if (this.aisles.isEmpty()) {
            this.height = pattern.length;
            this.width = pattern[0].length();
        }
        if (pattern.length != this.height) {
            throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + pattern.length + ")");
        }
        for (String string : pattern) {
            if (string.length() != this.width) {
                throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + string.length() + ")");
            }
            for (char c : string.toCharArray()) {
                if (this.charMap.containsKey(Character.valueOf(c))) continue;
                this.keysMissingPredicates.add(c);
            }
        }
        this.aisles.add(pattern);
        return this;
    }

    public static BlockPatternBuilder start() {
        return new BlockPatternBuilder();
    }

    public BlockPatternBuilder where(char key, Predicate<@Nullable CachedBlockPosition> predicate) {
        this.charMap.put(Character.valueOf(key), predicate);
        this.keysMissingPredicates.remove(key);
        return this;
    }

    public BlockPattern build() {
        return new BlockPattern(this.bakePredicates());
    }

    private Predicate<CachedBlockPosition>[][][] bakePredicates() {
        if (!this.keysMissingPredicates.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + String.valueOf(this.keysMissingPredicates) + " are missing");
        }
        Predicate[][][] predicates = (Predicate[][][])Array.newInstance(Predicate.class, this.aisles.size(), this.height, this.width);
        for (int i = 0; i < this.aisles.size(); ++i) {
            for (int j = 0; j < this.height; ++j) {
                for (int k = 0; k < this.width; ++k) {
                    predicates[i][j][k] = this.charMap.get(Character.valueOf(this.aisles.get(i)[j].charAt(k)));
                }
            }
        }
        return predicates;
    }
}

