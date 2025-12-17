/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.context;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

public interface LootEntityValueSource<R> {
    public static final Codec<LootEntityValueSource<Object>> ENTITY_OR_BLOCK_ENTITY_CODEC = LootEntityValueSource.createCodec(builder -> builder.addEntityReferences(LootContext.EntityReference.values()).addEntityReferences(LootContext.BlockEntityReference.values()));

    public @Nullable R get(LootContext var1);

    public ContextParameter<?> contextParam();

    public static <U> LootEntityValueSource<U> cast(LootEntityValueSource<? extends U> source) {
        return source;
    }

    public static <R> Codec<LootEntityValueSource<R>> createCodec(UnaryOperator<Builder<R>> factory) {
        return ((Builder)factory.apply(new Builder())).getCodec();
    }

    public static final class Builder<R> {
        private final Codecs.IdMapper<String, LootEntityValueSource<R>> ID_MAPPER = new Codecs.IdMapper();

        Builder() {
        }

        public <T> Builder<R> addAll(T[] values, Function<T, String> idGetter, Function<T, ? extends LootEntityValueSource<R>> sourceGetter) {
            for (T object : values) {
                this.ID_MAPPER.put(idGetter.apply(object), sourceGetter.apply(object));
            }
            return this;
        }

        public <T extends StringIdentifiable> Builder<R> addEnum(T[] values, Function<T, ? extends LootEntityValueSource<R>> sourceGetter) {
            return this.addAll(values, StringIdentifiable::asString, sourceGetter);
        }

        public <T extends StringIdentifiable & LootEntityValueSource<? extends R>> Builder<R> addEntityReferences(T[] values) {
            return this.addEnum((StringIdentifiable[])values, value -> LootEntityValueSource.cast((LootEntityValueSource)value));
        }

        public Builder<R> forEntities(Function<? super ContextParameter<? extends Entity>, ? extends LootEntityValueSource<R>> sourceFactory) {
            return this.addEnum(LootContext.EntityReference.values(), reference -> (LootEntityValueSource)sourceFactory.apply(reference.contextParam()));
        }

        public Builder<R> forBlockEntities(Function<? super ContextParameter<? extends BlockEntity>, ? extends LootEntityValueSource<R>> sourceFactory) {
            return this.addEnum(LootContext.BlockEntityReference.values(), reference -> (LootEntityValueSource)sourceFactory.apply(reference.contextParam()));
        }

        public Builder<R> forItemStacks(Function<? super ContextParameter<? extends ItemStack>, ? extends LootEntityValueSource<R>> sourceFactory) {
            return this.addEnum(LootContext.ItemStackReference.values(), reference -> (LootEntityValueSource)sourceFactory.apply(reference.contextParam()));
        }

        Codec<LootEntityValueSource<R>> getCodec() {
            return this.ID_MAPPER.getCodec((Codec<String>)Codec.STRING);
        }
    }

    public static interface ContextBased<T>
    extends LootEntityValueSource<T> {
        @Override
        public ContextParameter<? extends T> contextParam();

        @Override
        default public @Nullable T get(LootContext context) {
            return context.get(this.contextParam());
        }
    }

    public static interface ContextComponentBased<T, R>
    extends LootEntityValueSource<R> {
        public @Nullable R get(T var1);

        @Override
        public ContextParameter<? extends T> contextParam();

        @Override
        default public @Nullable R get(LootContext context) {
            T object = context.get(this.contextParam());
            return object != null ? (R)this.get(object) : null;
        }
    }
}

