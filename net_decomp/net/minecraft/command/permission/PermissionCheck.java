/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.command.permission;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.registry.Registries;

public interface PermissionCheck {
    public static final Codec<PermissionCheck> CODEC = Registries.PERMISSION_CHECK_TYPE.getCodec().dispatch(PermissionCheck::getCodec, codec -> codec);

    public boolean allows(PermissionPredicate var1);

    public MapCodec<? extends PermissionCheck> getCodec();

    public record Require(Permission permission) implements PermissionCheck
    {
        public static final MapCodec<Require> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Permission.CODEC.fieldOf("permission").forGetter(Require::permission)).apply((Applicative)instance, Require::new));

        public MapCodec<Require> getCodec() {
            return CODEC;
        }

        @Override
        public boolean allows(PermissionPredicate permissions) {
            return permissions.hasPermission(this.permission);
        }
    }

    public static class AlwaysPass
    implements PermissionCheck {
        public static final AlwaysPass INSTANCE = new AlwaysPass();
        public static final MapCodec<AlwaysPass> CODEC = MapCodec.unit((Object)INSTANCE);

        private AlwaysPass() {
        }

        @Override
        public boolean allows(PermissionPredicate permissions) {
            return true;
        }

        public MapCodec<AlwaysPass> getCodec() {
            return CODEC;
        }
    }
}

