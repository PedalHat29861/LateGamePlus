/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfileRepository
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.yggdrasil.ServicesKeySet
 *  com.mojang.authlib.yggdrasil.ServicesKeyType
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.server.GameProfileResolver;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.UserCache;
import org.jspecify.annotations.Nullable;

public record ApiServices(MinecraftSessionService sessionService, ServicesKeySet servicesKeySet, GameProfileRepository profileRepository, NameToIdCache nameToIdCache, GameProfileResolver profileResolver) {
    private static final String USER_CACHE_FILE_NAME = "usercache.json";

    public static ApiServices create(YggdrasilAuthenticationService authenticationService, File rootDirectory) {
        MinecraftSessionService minecraftSessionService = authenticationService.createMinecraftSessionService();
        GameProfileRepository gameProfileRepository = authenticationService.createProfileRepository();
        UserCache nameToIdCache = new UserCache(gameProfileRepository, new File(rootDirectory, USER_CACHE_FILE_NAME));
        GameProfileResolver.CachedSessionProfileResolver gameProfileResolver = new GameProfileResolver.CachedSessionProfileResolver(minecraftSessionService, nameToIdCache);
        return new ApiServices(minecraftSessionService, authenticationService.getServicesKeySet(), gameProfileRepository, nameToIdCache, gameProfileResolver);
    }

    public @Nullable SignatureVerifier serviceSignatureVerifier() {
        return SignatureVerifier.create(this.servicesKeySet, ServicesKeyType.PROFILE_KEY);
    }

    public boolean providesProfileKeys() {
        return !this.servicesKeySet.keys(ServicesKeyType.PROFILE_KEY).isEmpty();
    }
}

