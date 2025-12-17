/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry.tag;

import net.minecraft.dialog.type.Dialog;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DialogTags {
    public static final TagKey<Dialog> PAUSE_SCREEN_ADDITIONS = DialogTags.of("pause_screen_additions");
    public static final TagKey<Dialog> QUICK_ACTIONS = DialogTags.of("quick_actions");

    private DialogTags() {
    }

    private static TagKey<Dialog> of(String id) {
        return TagKey.of(RegistryKeys.DIALOG, Identifier.ofVanilla(id));
    }
}

