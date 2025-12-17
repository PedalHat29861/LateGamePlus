/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public class GenerationStep {

    public static final class Feature
    extends Enum<Feature>
    implements StringIdentifiable {
        public static final /* enum */ Feature RAW_GENERATION = new Feature("raw_generation");
        public static final /* enum */ Feature LAKES = new Feature("lakes");
        public static final /* enum */ Feature LOCAL_MODIFICATIONS = new Feature("local_modifications");
        public static final /* enum */ Feature UNDERGROUND_STRUCTURES = new Feature("underground_structures");
        public static final /* enum */ Feature SURFACE_STRUCTURES = new Feature("surface_structures");
        public static final /* enum */ Feature STRONGHOLDS = new Feature("strongholds");
        public static final /* enum */ Feature UNDERGROUND_ORES = new Feature("underground_ores");
        public static final /* enum */ Feature UNDERGROUND_DECORATION = new Feature("underground_decoration");
        public static final /* enum */ Feature FLUID_SPRINGS = new Feature("fluid_springs");
        public static final /* enum */ Feature VEGETAL_DECORATION = new Feature("vegetal_decoration");
        public static final /* enum */ Feature TOP_LAYER_MODIFICATION = new Feature("top_layer_modification");
        public static final Codec<Feature> CODEC;
        private final String name;
        private static final /* synthetic */ Feature[] field_13181;

        public static Feature[] values() {
            return (Feature[])field_13181.clone();
        }

        public static Feature valueOf(String string) {
            return Enum.valueOf(Feature.class, string);
        }

        private Feature(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Feature[] method_36751() {
            return new Feature[]{RAW_GENERATION, LAKES, LOCAL_MODIFICATIONS, UNDERGROUND_STRUCTURES, SURFACE_STRUCTURES, STRONGHOLDS, UNDERGROUND_ORES, UNDERGROUND_DECORATION, FLUID_SPRINGS, VEGETAL_DECORATION, TOP_LAYER_MODIFICATION};
        }

        static {
            field_13181 = Feature.method_36751();
            CODEC = StringIdentifiable.createCodec(Feature::values);
        }
    }
}

