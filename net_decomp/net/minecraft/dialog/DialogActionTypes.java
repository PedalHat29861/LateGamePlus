/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.dialog;

import com.mojang.serialization.MapCodec;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.action.DynamicCustomDialogAction;
import net.minecraft.dialog.action.DynamicRunCommandDialogAction;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DialogActionTypes {
    public static MapCodec<? extends DialogAction> registerAndGetDefault(Registry<MapCodec<? extends DialogAction>> registry) {
        SimpleDialogAction.CODECS.forEach((clickEventAction, codec) -> Registry.register(registry, Identifier.ofVanilla(clickEventAction.asString()), codec));
        Registry.register(registry, Identifier.ofVanilla("dynamic/run_command"), DynamicRunCommandDialogAction.CODEC);
        return Registry.register(registry, Identifier.ofVanilla("dynamic/custom"), DynamicCustomDialogAction.CODEC);
    }
}

