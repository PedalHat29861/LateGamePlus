/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.PlayerInput;

public record InputPredicate(Optional<Boolean> forward, Optional<Boolean> backward, Optional<Boolean> left, Optional<Boolean> right, Optional<Boolean> jump, Optional<Boolean> sneak, Optional<Boolean> sprint) {
    public static final Codec<InputPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("forward").forGetter(InputPredicate::forward), (App)Codec.BOOL.optionalFieldOf("backward").forGetter(InputPredicate::backward), (App)Codec.BOOL.optionalFieldOf("left").forGetter(InputPredicate::left), (App)Codec.BOOL.optionalFieldOf("right").forGetter(InputPredicate::right), (App)Codec.BOOL.optionalFieldOf("jump").forGetter(InputPredicate::jump), (App)Codec.BOOL.optionalFieldOf("sneak").forGetter(InputPredicate::sneak), (App)Codec.BOOL.optionalFieldOf("sprint").forGetter(InputPredicate::sprint)).apply((Applicative)instance, InputPredicate::new));

    public boolean matches(PlayerInput playerInput) {
        return this.keyMatches(this.forward, playerInput.forward()) && this.keyMatches(this.backward, playerInput.backward()) && this.keyMatches(this.left, playerInput.left()) && this.keyMatches(this.right, playerInput.right()) && this.keyMatches(this.jump, playerInput.jump()) && this.keyMatches(this.sneak, playerInput.sneak()) && this.keyMatches(this.sprint, playerInput.sprint());
    }

    private boolean keyMatches(Optional<Boolean> keyPressed, boolean inputPressed) {
        return keyPressed.map(pressed -> pressed == inputPressed).orElse(true);
    }
}

