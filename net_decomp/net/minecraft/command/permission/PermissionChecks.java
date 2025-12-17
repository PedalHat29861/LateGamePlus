/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.command.permission;

import com.mojang.serialization.MapCodec;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class PermissionChecks {
    public static MapCodec<? extends PermissionCheck> registerAndGetDefault(Registry<MapCodec<? extends PermissionCheck>> registry) {
        Registry.register(registry, Identifier.ofVanilla("always_pass"), PermissionCheck.AlwaysPass.CODEC);
        return Registry.register(registry, Identifier.ofVanilla("require"), PermissionCheck.Require.CODEC);
    }
}

