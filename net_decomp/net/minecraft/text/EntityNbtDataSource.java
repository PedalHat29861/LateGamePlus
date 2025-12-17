/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.NbtDataSource;
import org.jspecify.annotations.Nullable;

public record EntityNbtDataSource(String rawSelector, @Nullable EntitySelector selector) implements NbtDataSource
{
    public static final MapCodec<EntityNbtDataSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("entity").forGetter(EntityNbtDataSource::rawSelector)).apply((Applicative)instance, EntityNbtDataSource::new));

    public EntityNbtDataSource(String rawPath) {
        this(rawPath, EntityNbtDataSource.parseSelector(rawPath));
    }

    private static @Nullable EntitySelector parseSelector(String rawSelector) {
        try {
            EntitySelectorReader entitySelectorReader = new EntitySelectorReader(new StringReader(rawSelector), true);
            return entitySelectorReader.read();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public Stream<NbtCompound> get(ServerCommandSource source) throws CommandSyntaxException {
        if (this.selector != null) {
            List<? extends Entity> list = this.selector.getEntities(source);
            return list.stream().map(NbtPredicate::entityToNbt);
        }
        return Stream.empty();
    }

    public MapCodec<EntityNbtDataSource> getCodec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "entity=" + this.rawSelector;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityNbtDataSource)) return false;
        EntityNbtDataSource entityNbtDataSource = (EntityNbtDataSource)o;
        if (!this.rawSelector.equals(entityNbtDataSource.rawSelector)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.rawSelector.hashCode();
    }
}

