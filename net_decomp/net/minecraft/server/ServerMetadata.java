/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record ServerMetadata(Text description, Optional<Players> players, Optional<Version> version, Optional<Favicon> favicon, boolean secureChatEnforced) {
    public static final Codec<ServerMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TextCodecs.CODEC.lenientOptionalFieldOf("description", (Object)ScreenTexts.EMPTY).forGetter(ServerMetadata::description), (App)Players.CODEC.lenientOptionalFieldOf("players").forGetter(ServerMetadata::players), (App)Version.CODEC.lenientOptionalFieldOf("version").forGetter(ServerMetadata::version), (App)Favicon.CODEC.lenientOptionalFieldOf("favicon").forGetter(ServerMetadata::favicon), (App)Codec.BOOL.lenientOptionalFieldOf("enforcesSecureChat", (Object)false).forGetter(ServerMetadata::secureChatEnforced)).apply((Applicative)instance, ServerMetadata::new));

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ServerMetadata.class, "description;players;version;favicon;enforcesSecureChat", "description", "players", "version", "favicon", "secureChatEnforced"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ServerMetadata.class, "description;players;version;favicon;enforcesSecureChat", "description", "players", "version", "favicon", "secureChatEnforced"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ServerMetadata.class, "description;players;version;favicon;enforcesSecureChat", "description", "players", "version", "favicon", "secureChatEnforced"}, this, object);
    }

    public record Players(int max, int online, List<PlayerConfigEntry> sample) {
        public static final Codec<Players> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("max").forGetter(Players::max), (App)Codec.INT.fieldOf("online").forGetter(Players::online), (App)PlayerConfigEntry.CODEC.listOf().lenientOptionalFieldOf("sample", List.of()).forGetter(Players::sample)).apply((Applicative)instance, Players::new));
    }

    public record Version(String gameVersion, int protocolVersion) {
        public static final Codec<Version> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(Version::gameVersion), (App)Codec.INT.fieldOf("protocol").forGetter(Version::protocolVersion)).apply((Applicative)instance, Version::new));

        public static Version create() {
            GameVersion gameVersion = SharedConstants.getGameVersion();
            return new Version(gameVersion.name(), gameVersion.protocolVersion());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Version.class, "name;protocol", "gameVersion", "protocolVersion"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Version.class, "name;protocol", "gameVersion", "protocolVersion"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Version.class, "name;protocol", "gameVersion", "protocolVersion"}, this, object);
        }
    }

    public record Favicon(byte[] iconBytes) {
        private static final String DATA_URI_PREFIX = "data:image/png;base64,";
        public static final Codec<Favicon> CODEC = Codec.STRING.comapFlatMap(uri -> {
            if (!uri.startsWith(DATA_URI_PREFIX)) {
                return DataResult.error(() -> "Unknown format");
            }
            try {
                String string = uri.substring(DATA_URI_PREFIX.length()).replaceAll("\n", "");
                byte[] bs = Base64.getDecoder().decode(string.getBytes(StandardCharsets.UTF_8));
                return DataResult.success((Object)new Favicon(bs));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return DataResult.error(() -> "Malformed base64 server icon");
            }
        }, iconBytes -> DATA_URI_PREFIX + new String(Base64.getEncoder().encode(iconBytes.iconBytes), StandardCharsets.UTF_8));
    }
}

