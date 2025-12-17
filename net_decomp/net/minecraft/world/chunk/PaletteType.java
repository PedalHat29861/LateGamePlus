/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import java.util.List;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteProvider;

public interface PaletteType {
    public boolean shouldRepack();

    public int bitsInMemory();

    public int bitsInStorage();

    public <T> Palette<T> createPalette(PaletteProvider<T> var1, List<T> var2);

    public record Dynamic(int bitsInMemory, int bitsInStorage) implements PaletteType
    {
        @Override
        public boolean shouldRepack() {
            return true;
        }

        @Override
        public <T> Palette<T> createPalette(PaletteProvider<T> provider, List<T> values) {
            return provider.getPalette();
        }
    }

    public record Static(Palette.Factory factory, int bits) implements PaletteType
    {
        @Override
        public boolean shouldRepack() {
            return false;
        }

        @Override
        public <T> Palette<T> createPalette(PaletteProvider<T> provider, List<T> values) {
            return this.factory.create(this.bits, values);
        }

        @Override
        public int bitsInMemory() {
            return this.bits;
        }

        @Override
        public int bitsInStorage() {
            return this.bits;
        }
    }
}

