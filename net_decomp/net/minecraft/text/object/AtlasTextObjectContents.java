/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.text.object;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.object.TextObjectContents;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;

public record AtlasTextObjectContents(Identifier atlas, Identifier sprite) implements TextObjectContents
{
    public static final Identifier DEFAULT_ATLAS = Atlases.BLOCKS;
    public static final MapCodec<AtlasTextObjectContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.optionalFieldOf("atlas", (Object)DEFAULT_ATLAS).forGetter(AtlasTextObjectContents::atlas), (App)Identifier.CODEC.fieldOf("sprite").forGetter(AtlasTextObjectContents::sprite)).apply((Applicative)instance, AtlasTextObjectContents::new));

    public MapCodec<AtlasTextObjectContents> getCodec() {
        return CODEC;
    }

    @Override
    public StyleSpriteSource spriteSource() {
        return new StyleSpriteSource.Sprite(this.atlas, this.sprite);
    }

    private static String getShortIdString(Identifier id) {
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
    }

    @Override
    public String asText() {
        String string = AtlasTextObjectContents.getShortIdString(this.sprite);
        if (this.atlas.equals(DEFAULT_ATLAS)) {
            return "[" + string + "]";
        }
        return "[" + string + "@" + AtlasTextObjectContents.getShortIdString(this.atlas) + "]";
    }
}

