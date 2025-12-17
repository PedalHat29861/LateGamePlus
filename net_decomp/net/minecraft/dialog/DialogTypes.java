/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.dialog;

import com.mojang.serialization.MapCodec;
import net.minecraft.dialog.type.ConfirmationDialog;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.dialog.type.DialogListDialog;
import net.minecraft.dialog.type.MultiActionDialog;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.dialog.type.ServerLinksDialog;
import net.minecraft.registry.Registry;

public class DialogTypes {
    public static MapCodec<? extends Dialog> registerAndGetDefault(Registry<MapCodec<? extends Dialog>> registry) {
        Registry.register(registry, "notice", NoticeDialog.CODEC);
        Registry.register(registry, "server_links", ServerLinksDialog.CODEC);
        Registry.register(registry, "dialog_list", DialogListDialog.CODEC);
        Registry.register(registry, "multi_action", MultiActionDialog.CODEC);
        return Registry.register(registry, "confirmation", ConfirmationDialog.CODEC);
    }
}

