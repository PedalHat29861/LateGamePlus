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
package net.minecraft.text.object;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.object.TextObjectContents;

public record PlayerTextObjectContents(ProfileComponent player, boolean hat) implements TextObjectContents
{
    public static final MapCodec<PlayerTextObjectContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ProfileComponent.CODEC.fieldOf("player").forGetter(PlayerTextObjectContents::player), (App)Codec.BOOL.optionalFieldOf("hat", (Object)true).forGetter(PlayerTextObjectContents::hat)).apply((Applicative)instance, PlayerTextObjectContents::new));

    @Override
    public StyleSpriteSource spriteSource() {
        return new StyleSpriteSource.Player(this.player, this.hat);
    }

    @Override
    public String asText() {
        return this.player.getName().map(name -> "[" + name + " head]").orElse("[unknown player head]");
    }

    public MapCodec<PlayerTextObjectContents> getCodec() {
        return CODEC;
    }
}

