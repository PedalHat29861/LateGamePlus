/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.player;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public class PlayerAbilities {
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULT_FLYING = false;
    private static final boolean DEFAULT_ALLOW_FLYING = false;
    private static final boolean DEFAULT_CREATIVE_MODE = false;
    private static final boolean DEFAULT_ALLOW_MODIFY_WORLD = true;
    private static final float DEFAULT_FLY_SPEED = 0.05f;
    private static final float DEFAULT_WALK_SPEED = 0.1f;
    public boolean invulnerable;
    public boolean flying;
    public boolean allowFlying;
    public boolean creativeMode;
    public boolean allowModifyWorld = true;
    private float flySpeed = 0.05f;
    private float walkSpeed = 0.1f;

    public float getFlySpeed() {
        return this.flySpeed;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    public void setWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public Packed pack() {
        return new Packed(this.invulnerable, this.flying, this.allowFlying, this.creativeMode, this.allowModifyWorld, this.flySpeed, this.walkSpeed);
    }

    public void unpack(Packed packed) {
        this.invulnerable = packed.invulnerable;
        this.flying = packed.flying;
        this.allowFlying = packed.mayFly;
        this.creativeMode = packed.instabuild;
        this.allowModifyWorld = packed.mayBuild;
        this.flySpeed = packed.flyingSpeed;
        this.walkSpeed = packed.walkingSpeed;
    }

    public static final class Packed
    extends Record {
        final boolean invulnerable;
        final boolean flying;
        final boolean mayFly;
        final boolean instabuild;
        final boolean mayBuild;
        final float flyingSpeed;
        final float walkingSpeed;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.fieldOf("invulnerable").orElse((Object)false).forGetter(Packed::invulnerable), (App)Codec.BOOL.fieldOf("flying").orElse((Object)false).forGetter(Packed::flying), (App)Codec.BOOL.fieldOf("mayfly").orElse((Object)false).forGetter(Packed::mayFly), (App)Codec.BOOL.fieldOf("instabuild").orElse((Object)false).forGetter(Packed::instabuild), (App)Codec.BOOL.fieldOf("mayBuild").orElse((Object)true).forGetter(Packed::mayBuild), (App)Codec.FLOAT.fieldOf("flySpeed").orElse((Object)Float.valueOf(0.05f)).forGetter(Packed::flyingSpeed), (App)Codec.FLOAT.fieldOf("walkSpeed").orElse((Object)Float.valueOf(0.1f)).forGetter(Packed::walkingSpeed)).apply((Applicative)instance, Packed::new));

        public Packed(boolean invulnerable, boolean flying, boolean mayFly, boolean instabuild, boolean mayBuild, float flyingSpeed, float walkingSpeed) {
            this.invulnerable = invulnerable;
            this.flying = flying;
            this.mayFly = mayFly;
            this.instabuild = instabuild;
            this.mayBuild = mayBuild;
            this.flyingSpeed = flyingSpeed;
            this.walkingSpeed = walkingSpeed;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this, object);
        }

        public boolean invulnerable() {
            return this.invulnerable;
        }

        public boolean flying() {
            return this.flying;
        }

        public boolean mayFly() {
            return this.mayFly;
        }

        public boolean instabuild() {
            return this.instabuild;
        }

        public boolean mayBuild() {
            return this.mayBuild;
        }

        public float flyingSpeed() {
            return this.flyingSpeed;
        }

        public float walkingSpeed() {
            return this.walkingSpeed;
        }
    }
}

