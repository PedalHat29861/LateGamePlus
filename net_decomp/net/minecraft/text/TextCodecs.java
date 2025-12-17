/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapDecoder
 *  com.mojang.serialization.MapEncoder
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.text;

import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.ObjectTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;

public class TextCodecs {
    public static final Codec<Text> CODEC = Codec.recursive((String)"Component", TextCodecs::createCodec);
    public static final PacketCodec<RegistryByteBuf, Text> REGISTRY_PACKET_CODEC = PacketCodecs.registryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, Optional<Text>> OPTIONAL_PACKET_CODEC = REGISTRY_PACKET_CODEC.collect(PacketCodecs::optional);
    public static final PacketCodec<RegistryByteBuf, Text> UNLIMITED_REGISTRY_PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, Optional<Text>> OPTIONAL_UNLIMITED_REGISTRY_PACKET_CODEC = UNLIMITED_REGISTRY_PACKET_CODEC.collect(PacketCodecs::optional);
    public static final PacketCodec<ByteBuf, Text> PACKET_CODEC = PacketCodecs.unlimitedCodec(CODEC);

    public static Codec<Text> withJsonLengthLimit(final int maxLength) {
        return new Codec<Text>(){

            public <T> DataResult<Pair<Text, T>> decode(DynamicOps<T> ops, T value) {
                return CODEC.decode(ops, value).flatMap(pair -> {
                    if (this.isTooLarge(ops, (Text)pair.getFirst())) {
                        return DataResult.error(() -> "Component was too large: greater than max size " + maxLength);
                    }
                    return DataResult.success((Object)pair);
                });
            }

            public <T> DataResult<T> encode(Text text, DynamicOps<T> dynamicOps, T object) {
                return CODEC.encodeStart(dynamicOps, (Object)text);
            }

            private <T> boolean isTooLarge(DynamicOps<T> ops, Text text) {
                DataResult dataResult = CODEC.encodeStart(1.toJsonOps(ops), (Object)text);
                return dataResult.isSuccess() && JsonHelper.isTooLarge((JsonElement)dataResult.getOrThrow(), maxLength);
            }

            private static <T> DynamicOps<JsonElement> toJsonOps(DynamicOps<T> ops) {
                if (ops instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)ops;
                    return registryOps.withDelegate(JsonOps.INSTANCE);
                }
                return JsonOps.INSTANCE;
            }

            public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
                return this.encode((Text)input, ops, prefix);
            }
        };
    }

    private static MutableText combine(List<Text> texts) {
        MutableText mutableText = texts.get(0).copy();
        for (int i = 1; i < texts.size(); ++i) {
            mutableText.append(texts.get(i));
        }
        return mutableText;
    }

    public static <T> MapCodec<T> dispatchingCodec(Codecs.IdMapper<String, MapCodec<? extends T>> idMapper, Function<T, MapCodec<? extends T>> typeToCodec, String typeKey) {
        FuzzyCodec<? extends T> mapCodec = new FuzzyCodec<T>(idMapper.values(), typeToCodec);
        MapCodec mapCodec2 = idMapper.getCodec((Codec<String>)Codec.STRING).dispatchMap(typeKey, typeToCodec, codec -> codec);
        DispatchingCodec<? extends T> mapCodec3 = new DispatchingCodec<T>(typeKey, mapCodec2, mapCodec);
        return Codecs.orCompressed(mapCodec3, mapCodec2);
    }

    private static Codec<Text> createCodec(Codec<Text> selfCodec) {
        Codecs.IdMapper idMapper = new Codecs.IdMapper();
        TextCodecs.registerTypes(idMapper);
        MapCodec<TextContent> mapCodec = TextCodecs.dispatchingCodec(idMapper, TextContent::getCodec, "type");
        Codec codec = RecordCodecBuilder.create(instance -> instance.group((App)mapCodec.forGetter(Text::getContent), (App)Codecs.nonEmptyList(selfCodec.listOf()).optionalFieldOf("extra", List.of()).forGetter(Text::getSiblings), (App)Style.Codecs.MAP_CODEC.forGetter(Text::getStyle)).apply((Applicative)instance, MutableText::new));
        return Codec.either((Codec)Codec.either((Codec)Codec.STRING, Codecs.nonEmptyList(selfCodec.listOf())), (Codec)codec).xmap(either -> (Text)either.map(either2 -> (Text)either2.map(Text::literal, TextCodecs::combine), text -> text), text -> {
            String string = text.getLiteralString();
            return string != null ? Either.left((Object)Either.left((Object)string)) : Either.right((Object)text);
        });
    }

    private static void registerTypes(Codecs.IdMapper<String, MapCodec<? extends TextContent>> idMapper) {
        idMapper.put("text", PlainTextContent.CODEC);
        idMapper.put("translatable", TranslatableTextContent.CODEC);
        idMapper.put("keybind", KeybindTextContent.CODEC);
        idMapper.put("score", ScoreTextContent.CODEC);
        idMapper.put("selector", SelectorTextContent.CODEC);
        idMapper.put("nbt", NbtTextContent.CODEC);
        idMapper.put("object", ObjectTextContent.CODEC);
    }

    static class FuzzyCodec<T>
    extends MapCodec<T> {
        private final Collection<MapCodec<? extends T>> codecs;
        private final Function<T, ? extends MapEncoder<? extends T>> codecGetter;

        public FuzzyCodec(Collection<MapCodec<? extends T>> codecs, Function<T, ? extends MapEncoder<? extends T>> codecGetter) {
            this.codecs = codecs;
            this.codecGetter = codecGetter;
        }

        public <S> DataResult<T> decode(DynamicOps<S> ops, MapLike<S> input) {
            for (MapDecoder mapDecoder : this.codecs) {
                DataResult dataResult = mapDecoder.decode(ops, input);
                if (!dataResult.result().isPresent()) continue;
                return dataResult;
            }
            return DataResult.error(() -> "No matching codec found");
        }

        public <S> RecordBuilder<S> encode(T input, DynamicOps<S> ops, RecordBuilder<S> prefix) {
            MapEncoder<? extends T> mapEncoder = this.codecGetter.apply(input);
            return mapEncoder.encode(input, ops, prefix);
        }

        public <S> Stream<S> keys(DynamicOps<S> ops) {
            return this.codecs.stream().flatMap(codec -> codec.keys(ops)).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }

    static class DispatchingCodec<T>
    extends MapCodec<T> {
        private final String dispatchingKey;
        private final MapCodec<T> withKeyCodec;
        private final MapCodec<T> withoutKeyCodec;

        public DispatchingCodec(String dispatchingKey, MapCodec<T> withKeyCodec, MapCodec<T> withoutKeyCodec) {
            this.dispatchingKey = dispatchingKey;
            this.withKeyCodec = withKeyCodec;
            this.withoutKeyCodec = withoutKeyCodec;
        }

        public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
            if (input.get(this.dispatchingKey) != null) {
                return this.withKeyCodec.decode(ops, input);
            }
            return this.withoutKeyCodec.decode(ops, input);
        }

        public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
            return this.withoutKeyCodec.encode(input, ops, prefix);
        }

        public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
            return Stream.concat(this.withKeyCodec.keys(ops), this.withoutKeyCodec.keys(ops)).distinct();
        }
    }
}

