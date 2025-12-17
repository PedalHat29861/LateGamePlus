/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.command.permission;

import com.mojang.serialization.MapCodec;
import net.minecraft.command.permission.Permission;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Permissions {
    public static MapCodec<? extends Permission> registerAndGetDefault(Registry<MapCodec<? extends Permission>> registry) {
        Registry.register(registry, Identifier.ofVanilla("atom"), Permission.Atom.CODEC);
        return Registry.register(registry, Identifier.ofVanilla("command_level"), Permission.Level.CODEC);
    }
}

