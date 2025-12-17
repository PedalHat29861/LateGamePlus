/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.component;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.predicate.component.ComponentPredicate;

public record ComponentsPredicate(ComponentMapPredicate exact, Map<ComponentPredicate.Type<?>, ComponentPredicate> partial) implements Predicate<ComponentsAccess>
{
    public static final ComponentsPredicate EMPTY = new ComponentsPredicate(ComponentMapPredicate.EMPTY, Map.of());
    public static final MapCodec<ComponentsPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ComponentMapPredicate.CODEC.optionalFieldOf("components", (Object)ComponentMapPredicate.EMPTY).forGetter(ComponentsPredicate::exact), (App)ComponentPredicate.PREDICATES_MAP_CODEC.optionalFieldOf("predicates", Map.of()).forGetter(ComponentsPredicate::partial)).apply((Applicative)instance, ComponentsPredicate::new));
    public static final PacketCodec<RegistryByteBuf, ComponentsPredicate> PACKET_CODEC = PacketCodec.tuple(ComponentMapPredicate.PACKET_CODEC, ComponentsPredicate::exact, ComponentPredicate.PREDICATES_MAP_PACKET_CODEC, ComponentsPredicate::partial, ComponentsPredicate::new);

    @Override
    public boolean test(ComponentsAccess componentsAccess) {
        if (!this.exact.test(componentsAccess)) {
            return false;
        }
        for (ComponentPredicate componentPredicate : this.partial.values()) {
            if (componentPredicate.test(componentsAccess)) continue;
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return this.exact.isEmpty() && this.partial.isEmpty();
    }

    @Override
    public /* synthetic */ boolean test(Object components) {
        return this.test((ComponentsAccess)components);
    }

    public static class Builder {
        private ComponentMapPredicate exact = ComponentMapPredicate.EMPTY;
        private final ImmutableMap.Builder<ComponentPredicate.Type<?>, ComponentPredicate> partial = ImmutableMap.builder();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public <T extends ComponentType<?>> Builder has(ComponentType<?> type) {
            ComponentPredicate.OfExistence ofExistence = ComponentPredicate.OfExistence.toPredicateType(type);
            this.partial.put((Object)ofExistence, (Object)ofExistence.getPredicate());
            return this;
        }

        public <T extends ComponentPredicate> Builder partial(ComponentPredicate.Type<T> type, T predicate) {
            this.partial.put(type, predicate);
            return this;
        }

        public Builder exact(ComponentMapPredicate exact) {
            this.exact = exact;
            return this;
        }

        public ComponentsPredicate build() {
            return new ComponentsPredicate(this.exact, (Map<ComponentPredicate.Type<?>, ComponentPredicate>)this.partial.buildOrThrow());
        }
    }
}

