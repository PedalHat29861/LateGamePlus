/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.datafixers.util.Either
 */
package net.minecraft.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.StringHelper;

public interface GameProfileResolver {
    public Optional<GameProfile> getProfileByName(String var1);

    public Optional<GameProfile> getProfileById(UUID var1);

    default public Optional<GameProfile> getProfile(Either<String, UUID> either) {
        return (Optional)either.map(this::getProfileByName, this::getProfileById);
    }

    public static class CachedSessionProfileResolver
    implements GameProfileResolver {
        private final LoadingCache<String, Optional<GameProfile>> nameCache;
        final LoadingCache<UUID, Optional<GameProfile>> idCache;

        public CachedSessionProfileResolver(final MinecraftSessionService sessionService, final NameToIdCache cache) {
            this.idCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<UUID, Optional<GameProfile>>(this){

                public Optional<GameProfile> load(UUID uUID) {
                    ProfileResult profileResult = sessionService.fetchProfile(uUID, true);
                    return Optional.ofNullable(profileResult).map(ProfileResult::profile);
                }

                public /* synthetic */ Object load(Object id) throws Exception {
                    return this.load((UUID)id);
                }
            });
            this.nameCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build((CacheLoader)new CacheLoader<String, Optional<GameProfile>>(){

                public Optional<GameProfile> load(String string) {
                    return cache.findByName(string).flatMap(entry -> (Optional)idCache.getUnchecked((Object)entry.id()));
                }

                public /* synthetic */ Object load(Object name) throws Exception {
                    return this.load((String)name);
                }
            });
        }

        @Override
        public Optional<GameProfile> getProfileByName(String name) {
            if (StringHelper.isValidPlayerName(name)) {
                return (Optional)this.nameCache.getUnchecked((Object)name);
            }
            return Optional.empty();
        }

        @Override
        public Optional<GameProfile> getProfileById(UUID id) {
            return (Optional)this.idCache.getUnchecked((Object)id);
        }
    }
}

