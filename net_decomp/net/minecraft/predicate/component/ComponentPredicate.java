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
package net.minecraft.predicate.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.component.ComponentExistencePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

public interface ComponentPredicate {
    public static final Codec<Map<Type<?>, ComponentPredicate>> PREDICATES_MAP_CODEC = Codec.dispatchedMap(Type.CODEC, Type::getPredicateCodec);
    public static final PacketCodec<RegistryByteBuf, Typed<?>> SINGLE_PREDICATE_PACKET_CODEC = Type.PACKET_CODEC.dispatch(Typed::type, Type::getTypedPacketCodec);
    public static final PacketCodec<RegistryByteBuf, Map<Type<?>, ComponentPredicate>> PREDICATES_MAP_PACKET_CODEC = SINGLE_PREDICATE_PACKET_CODEC.collect(PacketCodecs.toList(64)).xmap(list -> list.stream().collect(Collectors.toMap(Typed::type, Typed::predicate)), map -> map.entrySet().stream().map(Typed::fromEntry).toList());

    public static MapCodec<Typed<?>> createCodec(String predicateFieldName) {
        return Type.CODEC.dispatchMap(predicateFieldName, Typed::type, Type::getTypedCodec);
    }

    public boolean test(ComponentsAccess var1);

    public static interface Type<T extends ComponentPredicate> {
        public static final Codec<Type<?>> CODEC = Codec.either(Registries.DATA_COMPONENT_PREDICATE_TYPE.getCodec(), Registries.DATA_COMPONENT_TYPE.getCodec()).xmap(Type::toType, Type::fromType);
        public static final PacketCodec<RegistryByteBuf, Type<?>> PACKET_CODEC = PacketCodecs.either(PacketCodecs.registryValue(RegistryKeys.DATA_COMPONENT_PREDICATE_TYPE), PacketCodecs.registryValue(RegistryKeys.DATA_COMPONENT_TYPE)).xmap(Type::toType, Type::fromType);

        private static <T extends Type<?>> Either<T, ComponentType<?>> fromType(T type) {
            Either either;
            if (type instanceof OfExistence) {
                OfExistence ofExistence = (OfExistence)type;
                either = Either.right(ofExistence.getComponentType());
            } else {
                either = Either.left(type);
            }
            return either;
        }

        private static Type<?> toType(Either<Type<?>, ComponentType<?>> either) {
            return (Type)either.map(type -> type, OfExistence::toPredicateType);
        }

        public Codec<T> getPredicateCodec();

        public MapCodec<Typed<T>> getTypedCodec();

        public PacketCodec<RegistryByteBuf, Typed<T>> getTypedPacketCodec();
    }

    public record Typed<T extends ComponentPredicate>(Type<T> type, T predicate) {
        static <T extends ComponentPredicate> MapCodec<Typed<T>> getCodec(Type<T> type, Codec<T> valueCodec) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)valueCodec.fieldOf("value").forGetter(Typed::predicate)).apply((Applicative)instance, predicate -> new Typed<ComponentPredicate>(type, (ComponentPredicate)predicate)));
        }

        private static <T extends ComponentPredicate> Typed<T> fromEntry(Map.Entry<Type<?>, T> entry) {
            return new Typed<ComponentPredicate>(entry.getKey(), (ComponentPredicate)entry.getValue());
        }
    }

    public static final class OfExistence
    extends TypeImpl<ComponentExistencePredicate> {
        private final ComponentExistencePredicate predicate;

        public OfExistence(ComponentExistencePredicate predicate) {
            super(MapCodec.unitCodec((Object)predicate));
            this.predicate = predicate;
        }

        public ComponentExistencePredicate getPredicate() {
            return this.predicate;
        }

        public ComponentType<?> getComponentType() {
            return this.predicate.type();
        }

        public static OfExistence toPredicateType(ComponentType<?> type) {
            return new OfExistence(new ComponentExistencePredicate(type));
        }
    }

    public static final class OfValue<T extends ComponentPredicate>
    extends TypeImpl<T> {
        public OfValue(Codec<T> codec) {
            super(codec);
        }
    }

    public static abstract class TypeImpl<T extends ComponentPredicate>
    implements Type<T> {
        private final Codec<T> predicateCodec;
        private final MapCodec<Typed<T>> typedCodec;
        private final PacketCodec<RegistryByteBuf, Typed<T>> packetCodec;

        public TypeImpl(Codec<T> predicateCodec) {
            this.predicateCodec = predicateCodec;
            this.typedCodec = Typed.getCodec(this, predicateCodec);
            this.packetCodec = PacketCodecs.registryCodec(predicateCodec).xmap(predicate -> new Typed<ComponentPredicate>(this, (ComponentPredicate)predicate), Typed::predicate);
        }

        @Override
        public Codec<T> getPredicateCodec() {
            return this.predicateCodec;
        }

        @Override
        public MapCodec<Typed<T>> getTypedCodec() {
            return this.typedCodec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, Typed<T>> getTypedPacketCodec() {
            return this.packetCodec;
        }
    }
}

