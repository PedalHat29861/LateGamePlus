/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.world.gen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.StringUtils;

public class GeneratorOptions {
    public static final MapCodec<GeneratorOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.LONG.fieldOf("seed").stable().forGetter(GeneratorOptions::getSeed), (App)Codec.BOOL.fieldOf("generate_features").orElse((Object)true).stable().forGetter(GeneratorOptions::shouldGenerateStructures), (App)Codec.BOOL.fieldOf("bonus_chest").orElse((Object)false).stable().forGetter(GeneratorOptions::hasBonusChest), (App)Codec.STRING.lenientOptionalFieldOf("legacy_custom_options").stable().forGetter(generatorOptions -> generatorOptions.legacyCustomOptions)).apply((Applicative)instance, instance.stable(GeneratorOptions::new)));
    public static final GeneratorOptions DEMO_OPTIONS = new GeneratorOptions("North Carolina".hashCode(), true, true);
    private final long seed;
    private final boolean generateStructures;
    private final boolean bonusChest;
    private final Optional<String> legacyCustomOptions;

    public GeneratorOptions(long seed, boolean generateStructures, boolean bonusChest) {
        this(seed, generateStructures, bonusChest, Optional.empty());
    }

    public static GeneratorOptions createRandom() {
        return new GeneratorOptions(GeneratorOptions.getRandomSeed(), true, false);
    }

    public static GeneratorOptions createTestWorld() {
        return new GeneratorOptions(GeneratorOptions.getRandomSeed(), false, false);
    }

    private GeneratorOptions(long seed, boolean generateStructures, boolean bonusChest, Optional<String> legacyCustomOptions) {
        this.seed = seed;
        this.generateStructures = generateStructures;
        this.bonusChest = bonusChest;
        this.legacyCustomOptions = legacyCustomOptions;
    }

    public long getSeed() {
        return this.seed;
    }

    public boolean shouldGenerateStructures() {
        return this.generateStructures;
    }

    public boolean hasBonusChest() {
        return this.bonusChest;
    }

    public boolean isLegacyCustomizedType() {
        return this.legacyCustomOptions.isPresent();
    }

    public GeneratorOptions withBonusChest(boolean bonusChest) {
        return new GeneratorOptions(this.seed, this.generateStructures, bonusChest, this.legacyCustomOptions);
    }

    public GeneratorOptions withStructures(boolean structures) {
        return new GeneratorOptions(this.seed, structures, this.bonusChest, this.legacyCustomOptions);
    }

    public GeneratorOptions withSeed(OptionalLong seed) {
        return new GeneratorOptions(seed.orElse(GeneratorOptions.getRandomSeed()), this.generateStructures, this.bonusChest, this.legacyCustomOptions);
    }

    public static OptionalLong parseSeed(String seed) {
        if (StringUtils.isEmpty((CharSequence)(seed = seed.trim()))) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(Long.parseLong(seed));
        }
        catch (NumberFormatException numberFormatException) {
            return OptionalLong.of(seed.hashCode());
        }
    }

    public static long getRandomSeed() {
        return Random.create().nextLong();
    }
}

