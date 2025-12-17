/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.encryption;

import java.security.SecureRandom;

public record BearerToken(String secretKey) {
    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static boolean isValid(String token) {
        if (token.isEmpty()) {
            return false;
        }
        return token.matches("^[a-zA-Z0-9]{40}$");
    }

    public static String generate() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(40);
        for (int i = 0; i < 40; ++i) {
            stringBuilder.append(ALLOWED_CHARS.charAt(secureRandom.nextInt(ALLOWED_CHARS.length())));
        }
        return stringBuilder.toString();
    }
}

