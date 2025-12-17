/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest;

import net.minecraft.SharedConstants;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.test.TestBootstrap;

public class Main {
    @DontObfuscate
    public static void main(String[] args) throws Exception {
        SharedConstants.createGameVersion();
        TestBootstrap.run(args, string -> {});
    }
}

