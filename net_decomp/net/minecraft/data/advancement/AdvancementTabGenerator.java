/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancement;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public interface AdvancementTabGenerator {
    public void accept(RegistryWrapper.WrapperLookup var1, Consumer<AdvancementEntry> var2);

    public static AdvancementEntry reference(String id) {
        return Advancement.Builder.create().build(Identifier.of(id));
    }
}

