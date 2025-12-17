/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ObjectUtils
 */
package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModStatus(Confidence confidence, String description) {
    public static ModStatus check(String vanillaBrand, Supplier<String> brandSupplier, String environment, Class<?> clazz) {
        String string = brandSupplier.get();
        if (!vanillaBrand.equals(string)) {
            return new ModStatus(Confidence.DEFINITELY, environment + " brand changed to '" + string + "'");
        }
        if (clazz.getSigners() == null) {
            return new ModStatus(Confidence.VERY_LIKELY, environment + " jar signature invalidated");
        }
        return new ModStatus(Confidence.PROBABLY_NOT, environment + " jar signature and brand is untouched");
    }

    public boolean isModded() {
        return this.confidence.modded;
    }

    public ModStatus combine(ModStatus brand) {
        return new ModStatus((Confidence)((Object)ObjectUtils.max((Comparable[])new Confidence[]{this.confidence, brand.confidence})), this.description + "; " + brand.description);
    }

    public String getMessage() {
        return this.confidence.description + " " + this.description;
    }

    public static final class Confidence
    extends Enum<Confidence> {
        public static final /* enum */ Confidence PROBABLY_NOT = new Confidence("Probably not.", false);
        public static final /* enum */ Confidence VERY_LIKELY = new Confidence("Very likely;", true);
        public static final /* enum */ Confidence DEFINITELY = new Confidence("Definitely;", true);
        final String description;
        final boolean modded;
        private static final /* synthetic */ Confidence[] field_35179;

        public static Confidence[] values() {
            return (Confidence[])field_35179.clone();
        }

        public static Confidence valueOf(String string) {
            return Enum.valueOf(Confidence.class, string);
        }

        private Confidence(String description, boolean modded) {
            this.description = description;
            this.modded = modded;
        }

        private static /* synthetic */ Confidence[] method_39033() {
            return new Confidence[]{PROBABLY_NOT, VERY_LIKELY, DEFINITELY};
        }

        static {
            field_35179 = Confidence.method_39033();
        }
    }
}

