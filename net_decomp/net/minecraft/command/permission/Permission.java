/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.command.permission;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public interface Permission {
    public static final Codec<Permission> UNABBREVIATED_CODEC = Registries.PERMISSION_TYPE.getCodec().dispatch(Permission::getCodec, codec -> codec);
    public static final Codec<Permission> CODEC = Codec.either(UNABBREVIATED_CODEC, Identifier.CODEC).xmap(either -> (Permission)either.map(perm -> perm, Atom::of), perm -> {
        Either either;
        if (perm instanceof Atom) {
            Atom atom = (Atom)perm;
            either = Either.right((Object)atom.id());
        } else {
            either = Either.left((Object)perm);
        }
        return either;
    });

    public MapCodec<? extends Permission> getCodec();

    public record Atom(Identifier id) implements Permission
    {
        public static final MapCodec<Atom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("id").forGetter(Atom::id)).apply((Applicative)instance, Atom::new));

        public MapCodec<Atom> getCodec() {
            return CODEC;
        }

        public static Atom ofVanilla(String path) {
            return Atom.of(Identifier.ofVanilla(path));
        }

        public static Atom of(Identifier id) {
            return new Atom(id);
        }
    }

    public record Level(PermissionLevel level) implements Permission
    {
        public static final MapCodec<Level> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)PermissionLevel.CODEC.fieldOf("level").forGetter(Level::level)).apply((Applicative)instance, Level::new));

        public MapCodec<Level> getCodec() {
            return CODEC;
        }
    }
}

