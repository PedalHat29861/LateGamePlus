/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.biome;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.biome.Biome;

public record BiomeEffects(int waterColor, Optional<Integer> foliageColor, Optional<Integer> dryFoliageColor, Optional<Integer> grassColor, GrassColorModifier grassColorModifier) {
    public static final Codec<BiomeEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.HEX_RGB.fieldOf("water_color").forGetter(BiomeEffects::waterColor), (App)Codecs.HEX_RGB.optionalFieldOf("foliage_color").forGetter(BiomeEffects::foliageColor), (App)Codecs.HEX_RGB.optionalFieldOf("dry_foliage_color").forGetter(BiomeEffects::dryFoliageColor), (App)Codecs.HEX_RGB.optionalFieldOf("grass_color").forGetter(BiomeEffects::grassColor), (App)GrassColorModifier.CODEC.optionalFieldOf("grass_color_modifier", (Object)GrassColorModifier.NONE).forGetter(BiomeEffects::grassColorModifier)).apply((Applicative)instance, BiomeEffects::new));

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeEffects.class, "waterColor;foliageColorOverride;dryFoliageColorOverride;grassColorOverride;grassColorModifier", "waterColor", "foliageColor", "dryFoliageColor", "grassColor", "grassColorModifier"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeEffects.class, "waterColor;foliageColorOverride;dryFoliageColorOverride;grassColorOverride;grassColorModifier", "waterColor", "foliageColor", "dryFoliageColor", "grassColor", "grassColorModifier"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeEffects.class, "waterColor;foliageColorOverride;dryFoliageColorOverride;grassColorOverride;grassColorModifier", "waterColor", "foliageColor", "dryFoliageColor", "grassColor", "grassColorModifier"}, this, object);
    }

    public static abstract sealed class GrassColorModifier
    extends Enum<GrassColorModifier>
    implements StringIdentifiable {
        public static final /* enum */ GrassColorModifier NONE = new GrassColorModifier("none"){

            @Override
            public int getModifiedGrassColor(double x, double z, int color) {
                return color;
            }
        };
        public static final /* enum */ GrassColorModifier DARK_FOREST = new GrassColorModifier("dark_forest"){

            @Override
            public int getModifiedGrassColor(double x, double z, int color) {
                return (color & 0xFEFEFE) + 2634762 >> 1;
            }
        };
        public static final /* enum */ GrassColorModifier SWAMP = new GrassColorModifier("swamp"){

            @Override
            public int getModifiedGrassColor(double x, double z, int color) {
                double d = Biome.FOLIAGE_NOISE.sample(x * 0.0225, z * 0.0225, false);
                if (d < -0.1) {
                    return 5011004;
                }
                return 6975545;
            }
        };
        private final String name;
        public static final Codec<GrassColorModifier> CODEC;
        private static final /* synthetic */ GrassColorModifier[] field_26432;

        public static GrassColorModifier[] values() {
            return (GrassColorModifier[])field_26432.clone();
        }

        public static GrassColorModifier valueOf(String string) {
            return Enum.valueOf(GrassColorModifier.class, string);
        }

        public abstract int getModifiedGrassColor(double var1, double var3, int var5);

        GrassColorModifier(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ GrassColorModifier[] method_36701() {
            return new GrassColorModifier[]{NONE, DARK_FOREST, SWAMP};
        }

        static {
            field_26432 = GrassColorModifier.method_36701();
            CODEC = StringIdentifiable.createCodec(GrassColorModifier::values);
        }
    }

    public static class Builder {
        private OptionalInt waterColor = OptionalInt.empty();
        private Optional<Integer> foliageColor = Optional.empty();
        private Optional<Integer> dryFoliageColor = Optional.empty();
        private Optional<Integer> grassColor = Optional.empty();
        private GrassColorModifier grassColorModifier = GrassColorModifier.NONE;

        public Builder waterColor(int waterColor) {
            this.waterColor = OptionalInt.of(waterColor);
            return this;
        }

        public Builder foliageColor(int foliageColor) {
            this.foliageColor = Optional.of(foliageColor);
            return this;
        }

        public Builder dryFoliageColor(int dryFoliageColor) {
            this.dryFoliageColor = Optional.of(dryFoliageColor);
            return this;
        }

        public Builder grassColor(int grassColor) {
            this.grassColor = Optional.of(grassColor);
            return this;
        }

        public Builder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public BiomeEffects build() {
            return new BiomeEffects(this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")), this.foliageColor, this.dryFoliageColor, this.grassColor, this.grassColorModifier);
        }
    }
}

