/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.google.common.primitives.UnsignedBytes
 *  com.google.gson.JsonElement
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Codec$ResultFunction
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.codecs.BaseMapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.apache.commons.lang3.StringEscapeUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.joml.AxisAngle4f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector2f
 *  org.joml.Vector2fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector3i
 *  org.joml.Vector3ic
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.dynamic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.ColorHelper;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class Codecs {
    public static final Codec<JsonElement> JSON_ELEMENT = Codecs.fromOps(JsonOps.INSTANCE);
    public static final Codec<Object> BASIC_OBJECT = Codecs.fromOps(JavaOps.INSTANCE);
    public static final Codec<NbtElement> NBT_ELEMENT = Codecs.fromOps(NbtOps.INSTANCE);
    public static final Codec<Vector2fc> VECTOR_2F = Codec.FLOAT.listOf().comapFlatMap(list -> Util.decodeFixedLengthList(list, 2).map(listx -> new Vector2f(((Float)listx.get(0)).floatValue(), ((Float)listx.get(1)).floatValue())), vec -> List.of(Float.valueOf(vec.x()), Float.valueOf(vec.y())));
    public static final Codec<Vector3fc> VECTOR_3F = Codec.FLOAT.listOf().comapFlatMap(list -> Util.decodeFixedLengthList(list, 3).map(listx -> new Vector3f(((Float)listx.get(0)).floatValue(), ((Float)listx.get(1)).floatValue(), ((Float)listx.get(2)).floatValue())), vec -> List.of(Float.valueOf(vec.x()), Float.valueOf(vec.y()), Float.valueOf(vec.z())));
    public static final Codec<Vector3ic> VECTOR_3I = Codec.INT.listOf().comapFlatMap(list -> Util.decodeFixedLengthList(list, 3).map(listx -> new Vector3i(((Integer)listx.get(0)).intValue(), ((Integer)listx.get(1)).intValue(), ((Integer)listx.get(2)).intValue())), vec -> List.of(Integer.valueOf(vec.x()), Integer.valueOf(vec.y()), Integer.valueOf(vec.z())));
    public static final Codec<Vector4fc> VECTOR_4F = Codec.FLOAT.listOf().comapFlatMap(list -> Util.decodeFixedLengthList(list, 4).map(listx -> new Vector4f(((Float)listx.get(0)).floatValue(), ((Float)listx.get(1)).floatValue(), ((Float)listx.get(2)).floatValue(), ((Float)listx.get(3)).floatValue())), vec -> List.of(Float.valueOf(vec.x()), Float.valueOf(vec.y()), Float.valueOf(vec.z()), Float.valueOf(vec.w())));
    public static final Codec<Quaternionfc> QUATERNION_F = Codec.FLOAT.listOf().comapFlatMap(list -> Util.decodeFixedLengthList(list, 4).map(listx -> new Quaternionf(((Float)listx.get(0)).floatValue(), ((Float)listx.get(1)).floatValue(), ((Float)listx.get(2)).floatValue(), ((Float)listx.get(3)).floatValue()).normalize()), quaternion -> List.of(Float.valueOf(quaternion.x()), Float.valueOf(quaternion.y()), Float.valueOf(quaternion.z()), Float.valueOf(quaternion.w())));
    public static final Codec<AxisAngle4f> AXIS_ANGLE_4F = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("angle").forGetter(axisAngle -> Float.valueOf(axisAngle.angle)), (App)VECTOR_3F.fieldOf("axis").forGetter(axisAngle -> new Vector3f(axisAngle.x, axisAngle.y, axisAngle.z))).apply((Applicative)instance, AxisAngle4f::new));
    public static final Codec<Quaternionfc> ROTATION = Codec.withAlternative(QUATERNION_F, (Codec)AXIS_ANGLE_4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static final Codec<Matrix4fc> MATRIX_4F = Codec.FLOAT.listOf().comapFlatMap(list -> Util.decodeFixedLengthList(list, 16).map(listx -> {
        Matrix4f matrix4f = new Matrix4f();
        for (int i = 0; i < listx.size(); ++i) {
            matrix4f.setRowColumn(i >> 2, i & 3, ((Float)listx.get(i)).floatValue());
        }
        return matrix4f.determineProperties();
    }), matrix -> {
        FloatArrayList floatList = new FloatArrayList(16);
        for (int i = 0; i < 16; ++i) {
            floatList.add(matrix.getRowColumn(i >> 2, i & 3));
        }
        return floatList;
    });
    private static final String HEX_PREFIX = "#";
    public static final Codec<Integer> RGB = Codec.withAlternative((Codec)Codec.INT, VECTOR_3F, vec -> ColorHelper.fromFloats(1.0f, vec.x(), vec.y(), vec.z()));
    public static final Codec<Integer> ARGB = Codec.withAlternative((Codec)Codec.INT, VECTOR_4F, vec -> ColorHelper.fromFloats(vec.w(), vec.x(), vec.y(), vec.z()));
    public static final Codec<Integer> HEX_RGB = Codec.withAlternative((Codec)Codecs.hexColor(6).xmap(ColorHelper::fullAlpha, ColorHelper::zeroAlpha), RGB);
    public static final Codec<Integer> HEX_ARGB = Codec.withAlternative(Codecs.hexColor(8), ARGB);
    public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE.flatComapMap(UnsignedBytes::toInt, value -> {
        if (value > 255) {
            return DataResult.error(() -> "Unsigned byte was too large: " + value + " > 255");
        }
        return DataResult.success((Object)value.byteValue());
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = Codecs.rangedInt(0, Integer.MAX_VALUE, v -> "Value must be non-negative: " + v);
    public static final Codec<Integer> POSITIVE_INT = Codecs.rangedInt(1, Integer.MAX_VALUE, v -> "Value must be positive: " + v);
    public static final Codec<Long> NON_NEGATIVE_LONG = Codecs.rangedLong(0L, Long.MAX_VALUE, v -> "Value must be non-negative: " + v);
    public static final Codec<Long> POSITIVE_LONG = Codecs.rangedLong(1L, Long.MAX_VALUE, v -> "Value must be positive: " + v);
    public static final Codec<Float> NON_NEGATIVE_FLOAT = Codecs.rangedInclusiveFloat(0.0f, Float.MAX_VALUE, v -> "Value must be non-negative: " + v);
    public static final Codec<Float> POSITIVE_FLOAT = Codecs.rangedFloat(0.0f, Float.MAX_VALUE, v -> "Value must be positive: " + v);
    public static final Codec<Pattern> REGULAR_EXPRESSION = Codec.STRING.comapFlatMap(pattern -> {
        try {
            return DataResult.success((Object)Pattern.compile(pattern));
        }
        catch (PatternSyntaxException patternSyntaxException) {
            return DataResult.error(() -> "Invalid regex pattern '" + pattern + "': " + patternSyntaxException.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT = Codecs.formattedTime(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE_64 = Codec.STRING.comapFlatMap(encoded -> {
        try {
            return DataResult.success((Object)Base64.getDecoder().decode((String)encoded));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return DataResult.error(() -> "Malformed base64 string");
        }
    }, data -> Base64.getEncoder().encodeToString((byte[])data));
    public static final Codec<String> ESCAPED_STRING = Codec.STRING.comapFlatMap(string -> DataResult.success((Object)StringEscapeUtils.unescapeJava((String)string)), StringEscapeUtils::escapeJava);
    public static final Codec<TagEntryId> TAG_ENTRY_ID = Codec.STRING.comapFlatMap(tagEntry -> tagEntry.startsWith(HEX_PREFIX) ? Identifier.validate(tagEntry.substring(1)).map(id -> new TagEntryId((Identifier)id, true)) : Identifier.validate(tagEntry).map(id -> new TagEntryId((Identifier)id, false)), TagEntryId::asString);
    public static final Function<Optional<Long>, OptionalLong> OPTIONAL_OF_LONG_TO_OPTIONAL_LONG = optional -> optional.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    public static final Function<OptionalLong, Optional<Long>> OPTIONAL_LONG_TO_OPTIONAL_OF_LONG = optionalLong -> optionalLong.isPresent() ? Optional.of(optionalLong.getAsLong()) : Optional.empty();
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap(stream -> BitSet.valueOf(stream.toArray()), set -> Arrays.stream(set.toLongArray()));
    public static final int MAX_PROFILE_PROPERTY_NAME_LENGTH = 64;
    public static final int MAX_PROFILE_PROPERTY_VALUE_LENGTH = Short.MAX_VALUE;
    public static final int MAX_PROFILE_PROPERTY_SIGNATURE_LENGTH = 1024;
    public static final int MAX_PROFILE_PROPERTIES_LENGTH = 16;
    private static final Codec<Property> GAME_PROFILE_PROPERTY = RecordCodecBuilder.create(instance -> instance.group((App)Codec.sizeLimitedString((int)64).fieldOf("name").forGetter(Property::name), (App)Codec.sizeLimitedString((int)Short.MAX_VALUE).fieldOf("value").forGetter(Property::value), (App)Codec.sizeLimitedString((int)1024).optionalFieldOf("signature").forGetter(property -> Optional.ofNullable(property.signature()))).apply((Applicative)instance, (key, value, signature) -> new Property(key, value, (String)signature.orElse(null))));
    public static final Codec<PropertyMap> GAME_PROFILE_PROPERTY_MAP = Codec.either((Codec)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING.listOf()).validate(map -> map.size() > 16 ? DataResult.error(() -> "Cannot have more than 16 properties, but was " + map.size()) : DataResult.success((Object)map)), (Codec)GAME_PROFILE_PROPERTY.sizeLimitedListOf(16)).xmap(either -> {
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        either.ifLeft(map -> map.forEach((key, values) -> {
            for (String string : values) {
                builder.put(key, (Object)new Property(key, string));
            }
        })).ifRight(properties -> {
            for (Property property : properties) {
                builder.put((Object)property.name(), (Object)property);
            }
        });
        return new PropertyMap((Multimap)builder.build());
    }, properties -> Either.right(properties.values().stream().toList()));
    public static final Codec<String> PLAYER_NAME = Codec.string((int)0, (int)16).validate(name -> {
        if (StringHelper.isValidPlayerName(name)) {
            return DataResult.success((Object)name);
        }
        return DataResult.error(() -> "Player name contained disallowed characters: '" + name + "'");
    });
    public static final Codec<GameProfile> GAME_PROFILE_CODEC = Codecs.createGameProfileCodec(Uuids.CODEC).codec();
    public static final MapCodec<GameProfile> INT_STREAM_UUID_GAME_PROFILE_CODEC = Codecs.createGameProfileCodec(Uuids.INT_STREAM_CODEC);
    public static final Codec<String> NON_EMPTY_STRING = Codec.STRING.validate(string -> string.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success((Object)string));
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap(string -> {
        int[] is = string.codePoints().toArray();
        if (is.length != 1) {
            return DataResult.error(() -> "Expected one codepoint, got: " + string);
        }
        return DataResult.success((Object)is[0]);
    }, Character::toString);
    public static final Codec<String> IDENTIFIER_PATH = Codec.STRING.validate(path -> {
        if (!Identifier.isPathValid(path)) {
            return DataResult.error(() -> "Invalid string to use as a resource path element: " + path);
        }
        return DataResult.success((Object)path);
    });
    public static final Codec<URI> URI = Codec.STRING.comapFlatMap(value -> {
        try {
            return DataResult.success((Object)Util.validateUri(value));
        }
        catch (URISyntaxException uRISyntaxException) {
            return DataResult.error(uRISyntaxException::getMessage);
        }
    }, URI::toString);
    public static final Codec<String> CHAT_TEXT = Codec.STRING.validate(s -> {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (StringHelper.isValidChar(c)) continue;
            return DataResult.error(() -> "Disallowed chat character: '" + c + "'");
        }
        return DataResult.success((Object)s);
    });

    public static <T> Codec<T> fromOps(DynamicOps<T> ops) {
        return Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(ops).getValue(), object -> new Dynamic(ops, object));
    }

    private static Codec<Integer> hexColor(int hexDigits) {
        long l = (1L << hexDigits * 4) - 1L;
        return Codec.STRING.comapFlatMap(value -> {
            if (!value.startsWith(HEX_PREFIX)) {
                return DataResult.error(() -> "Hex color must begin with #");
            }
            int j = value.length() - HEX_PREFIX.length();
            if (j != hexDigits) {
                return DataResult.error(() -> "Hex color is wrong size, expected " + hexDigits + " digits but got " + j);
            }
            try {
                long m = HexFormat.fromHexDigitsToLong(value, HEX_PREFIX.length(), value.length());
                if (m < 0L || m > l) {
                    return DataResult.error(() -> "Color value out of range: " + value);
                }
                return DataResult.success((Object)((int)m));
            }
            catch (NumberFormatException numberFormatException) {
                return DataResult.error(() -> "Invalid color value: " + value);
            }
        }, value -> HEX_PREFIX + HexFormat.of().toHexDigits(value.intValue(), hexDigits));
    }

    public static <P, I> Codec<I> createCodecForPairObject(Codec<P> codec, String leftFieldName, String rightFieldName, BiFunction<P, P, DataResult<I>> combineFunction, Function<I, P> leftFunction, Function<I, P> rightFunction) {
        Codec codec2 = Codec.list(codec).comapFlatMap(list -> Util.decodeFixedLengthList(list, 2).flatMap(listx -> {
            Object object = listx.get(0);
            Object object2 = listx.get(1);
            return (DataResult)combineFunction.apply(object, object2);
        }), pair -> ImmutableList.of(leftFunction.apply(pair), rightFunction.apply(pair)));
        Codec codec3 = RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf(leftFieldName).forGetter(Pair::getFirst), (App)codec.fieldOf(rightFieldName).forGetter(Pair::getSecond)).apply((Applicative)instance, Pair::of)).comapFlatMap(pair -> (DataResult)combineFunction.apply(pair.getFirst(), pair.getSecond()), pair -> Pair.of(leftFunction.apply(pair), rightFunction.apply(pair)));
        Codec codec4 = Codec.withAlternative((Codec)codec2, (Codec)codec3);
        return Codec.either(codec, (Codec)codec4).comapFlatMap(either -> (DataResult)either.map((T object) -> (DataResult)combineFunction.apply(object, object), DataResult::success), pair -> {
            Object object2;
            Object object = leftFunction.apply(pair);
            if (Objects.equals(object, object2 = rightFunction.apply(pair))) {
                return Either.left(object);
            }
            return Either.right((Object)pair);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A object) {
        return new Codec.ResultFunction<A>(){

            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<A, T>> result) {
                MutableObject mutableObject = new MutableObject();
                Optional optional = result.resultOrPartial(arg_0 -> ((MutableObject)mutableObject).setValue(arg_0));
                if (optional.isPresent()) {
                    return result;
                }
                return DataResult.error(() -> "(" + (String)mutableObject.get() + " -> using default)", (Object)Pair.of((Object)object, input));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> ops, A input, DataResult<T> result) {
                return result;
            }

            public String toString() {
                return "OrElsePartial[" + String.valueOf(object) + "]";
            }
        };
    }

    public static <E> Codec<E> rawIdChecked(ToIntFunction<E> elementToRawId, IntFunction<@Nullable E> rawIdToElement, int errorRawId) {
        return Codec.INT.flatXmap(rawId -> Optional.ofNullable(rawIdToElement.apply((int)rawId)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element id: " + rawId)), element -> {
            int j = elementToRawId.applyAsInt(element);
            return j == errorRawId ? DataResult.error(() -> "Element with unknown id: " + String.valueOf(element)) : DataResult.success((Object)j);
        });
    }

    public static <I, E> Codec<E> idChecked(Codec<I> idCodec, Function<I, @Nullable E> idToElement, Function<E, @Nullable I> elementToId) {
        return idCodec.flatXmap(id -> {
            Object object = idToElement.apply(id);
            return object == null ? DataResult.error(() -> "Unknown element id: " + String.valueOf(id)) : DataResult.success(object);
        }, element -> {
            Object object = elementToId.apply(element);
            if (object == null) {
                return DataResult.error(() -> "Element with unknown id: " + String.valueOf(element));
            }
            return DataResult.success(object);
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> uncompressedCodec, final Codec<E> compressedCodec) {
        return new Codec<E>(){

            public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
                if (ops.compressMaps()) {
                    return compressedCodec.encode(input, ops, prefix);
                }
                return uncompressedCodec.encode(input, ops, prefix);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
                if (ops.compressMaps()) {
                    return compressedCodec.decode(ops, input);
                }
                return uncompressedCodec.decode(ops, input);
            }

            public String toString() {
                return String.valueOf(uncompressedCodec) + " orCompressed " + String.valueOf(compressedCodec);
            }
        };
    }

    public static <E> MapCodec<E> orCompressed(final MapCodec<E> uncompressedCodec, final MapCodec<E> compressedCodec) {
        return new MapCodec<E>(){

            public <T> RecordBuilder<T> encode(E input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                if (ops.compressMaps()) {
                    return compressedCodec.encode(input, ops, prefix);
                }
                return uncompressedCodec.encode(input, ops, prefix);
            }

            public <T> DataResult<E> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (ops.compressMaps()) {
                    return compressedCodec.decode(ops, input);
                }
                return uncompressedCodec.decode(ops, input);
            }

            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return compressedCodec.keys(ops);
            }

            public String toString() {
                return String.valueOf(uncompressedCodec) + " orCompressed " + String.valueOf(compressedCodec);
            }
        };
    }

    public static <E> Codec<E> withLifecycle(Codec<E> originalCodec, final Function<E, Lifecycle> entryLifecycleGetter, final Function<E, Lifecycle> lifecycleGetter) {
        return originalCodec.mapResult(new Codec.ResultFunction<E>(){

            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<E, T>> result) {
                return result.result().map(pair -> result.setLifecycle((Lifecycle)entryLifecycleGetter.apply(pair.getFirst()))).orElse(result);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> ops, E input, DataResult<T> result) {
                return result.setLifecycle((Lifecycle)lifecycleGetter.apply(input));
            }

            public String toString() {
                return "WithLifecycle[" + String.valueOf(entryLifecycleGetter) + " " + String.valueOf(lifecycleGetter) + "]";
            }
        });
    }

    public static <E> Codec<E> withLifecycle(Codec<E> originalCodec, Function<E, Lifecycle> lifecycleGetter) {
        return Codecs.withLifecycle(originalCodec, lifecycleGetter, lifecycleGetter);
    }

    public static <K, V> StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new StrictUnboundedMapCodec<K, V>(keyCodec, elementCodec);
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec) {
        return Codecs.listOrSingle(entryCodec, entryCodec.listOf());
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, entryCodec).xmap(either -> (List)either.map((T list) -> list, List::of), list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left((Object)list));
    }

    private static Codec<Integer> rangedInt(int min, int max, Function<Integer, String> messageFactory) {
        return Codec.INT.validate(value -> {
            if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
                return DataResult.success((Object)value);
            }
            return DataResult.error(() -> (String)messageFactory.apply((Integer)value));
        });
    }

    public static Codec<Integer> rangedInt(int min, int max) {
        return Codecs.rangedInt(min, max, value -> "Value must be within range [" + min + ";" + max + "]: " + value);
    }

    private static Codec<Long> rangedLong(long min, long max, Function<Long, String> messageFactory) {
        return Codec.LONG.validate(value -> {
            if ((long)value.compareTo(min) >= 0L && (long)value.compareTo(max) <= 0L) {
                return DataResult.success((Object)value);
            }
            return DataResult.error(() -> (String)messageFactory.apply((Long)value));
        });
    }

    public static Codec<Long> rangedLong(int min, int max) {
        return Codecs.rangedLong(min, max, value -> "Value must be within range [" + min + ";" + max + "]: " + value);
    }

    private static Codec<Float> rangedInclusiveFloat(float minInclusive, float maxInclusive, Function<Float, String> messageFactory) {
        return Codec.FLOAT.validate(value -> {
            if (value.compareTo(Float.valueOf(minInclusive)) >= 0 && value.compareTo(Float.valueOf(maxInclusive)) <= 0) {
                return DataResult.success((Object)value);
            }
            return DataResult.error(() -> (String)messageFactory.apply((Float)value));
        });
    }

    private static Codec<Float> rangedFloat(float minExclusive, float maxInclusive, Function<Float, String> messageFactory) {
        return Codec.FLOAT.validate(value -> {
            if (value.compareTo(Float.valueOf(minExclusive)) > 0 && value.compareTo(Float.valueOf(maxInclusive)) <= 0) {
                return DataResult.success((Object)value);
            }
            return DataResult.error(() -> (String)messageFactory.apply((Float)value));
        });
    }

    public static Codec<Float> rangedInclusiveFloat(float minInclusive, float maxInclusive) {
        return Codecs.rangedInclusiveFloat(minInclusive, maxInclusive, value -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + value);
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> originalCodec) {
        return originalCodec.validate(list -> list.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success((Object)list));
    }

    public static <T> Codec<RegistryEntryList<T>> nonEmptyEntryList(Codec<RegistryEntryList<T>> originalCodec) {
        return originalCodec.validate(entryList -> {
            if (entryList.getStorage().right().filter(List::isEmpty).isPresent()) {
                return DataResult.error(() -> "List must have contents");
            }
            return DataResult.success((Object)entryList);
        });
    }

    public static <M extends Map<?, ?>> Codec<M> nonEmptyMap(Codec<M> originalCodec) {
        return originalCodec.validate(map -> map.isEmpty() ? DataResult.error(() -> "Map must have contents") : DataResult.success((Object)map));
    }

    public static <E> MapCodec<E> createContextRetrievalCodec(Function<DynamicOps<?>, DataResult<E>> retriever) {
        class ContextRetrievalCodec
        extends MapCodec<E> {
            final /* synthetic */ Function retriever;

            ContextRetrievalCodec(Function retriever) {
                this.retriever = retriever;
            }

            public <T> RecordBuilder<T> encode(E input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix;
            }

            public <T> DataResult<E> decode(DynamicOps<T> ops, MapLike<T> input) {
                return (DataResult)this.retriever.apply(ops);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + String.valueOf(this.retriever) + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }
        }
        return new ContextRetrievalCodec(retriever);
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> createEqualTypeChecker(Function<E, T> typeGetter) {
        return collection -> {
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                Object object = typeGetter.apply(iterator.next());
                while (iterator.hasNext()) {
                    Object object2 = iterator.next();
                    Object object3 = typeGetter.apply(object2);
                    if (object3 == object) continue;
                    return DataResult.error(() -> "Mixed type list: element " + String.valueOf(object2) + " had type " + String.valueOf(object3) + ", but list is of type " + String.valueOf(object));
                }
            }
            return DataResult.success((Object)collection, (Lifecycle)Lifecycle.stable());
        };
    }

    public static <A> Codec<A> exceptionCatching(final Codec<A> codec) {
        return Codec.of(codec, (Decoder)new Decoder<A>(){

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                try {
                    return codec.decode(ops, input);
                }
                catch (Exception exception) {
                    return DataResult.error(() -> "Caught exception decoding " + String.valueOf(input) + ": " + exception.getMessage());
                }
            }
        });
    }

    public static Codec<TemporalAccessor> formattedTime(DateTimeFormatter formatter) {
        return Codec.STRING.comapFlatMap(string -> {
            try {
                return DataResult.success((Object)formatter.parse((CharSequence)string));
            }
            catch (Exception exception) {
                return DataResult.error(exception::getMessage);
            }
        }, formatter::format);
    }

    public static MapCodec<OptionalLong> optionalLong(MapCodec<Optional<Long>> codec) {
        return codec.xmap(OPTIONAL_OF_LONG_TO_OPTIONAL_LONG, OPTIONAL_LONG_TO_OPTIONAL_OF_LONG);
    }

    private static MapCodec<GameProfile> createGameProfileCodec(Codec<UUID> uuidCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)uuidCodec.fieldOf("id").forGetter(GameProfile::id), (App)PLAYER_NAME.fieldOf("name").forGetter(GameProfile::name), (App)GAME_PROFILE_PROPERTY_MAP.optionalFieldOf("properties", (Object)PropertyMap.EMPTY).forGetter(GameProfile::properties)).apply((Applicative)instance, GameProfile::new));
    }

    public static <K, V> Codec<Map<K, V>> map(Codec<Map<K, V>> codec, int maxLength) {
        return codec.validate(map -> {
            if (map.size() > maxLength) {
                return DataResult.error(() -> "Map is too long: " + map.size() + ", expected range [0-" + maxLength + "]");
            }
            return DataResult.success((Object)map);
        });
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> keyCodec) {
        return Codec.unboundedMap(keyCodec, (Codec)Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    @Deprecated
    public static <K, V> MapCodec<V> parameters(final String typeKey, final String parametersKey, final Codec<K> typeCodec, final Function<? super V, ? extends K> typeGetter, final Function<? super K, ? extends Codec<? extends V>> parametersCodecGetter) {
        return new MapCodec<V>(){

            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(typeKey), ops.createString(parametersKey));
            }

            public <T> DataResult<V> decode(DynamicOps<T> ops, MapLike<T> input) {
                Object object = input.get(typeKey);
                if (object == null) {
                    return DataResult.error(() -> "Missing \"" + typeKey + "\" in: " + String.valueOf(input));
                }
                return typeCodec.decode(ops, object).flatMap(pair -> {
                    Object object = Objects.requireNonNullElseGet(input.get(parametersKey), () -> ((DynamicOps)ops).emptyMap());
                    return ((Codec)parametersCodecGetter.apply(pair.getFirst())).decode(ops, object).map(Pair::getFirst);
                });
            }

            public <T> RecordBuilder<T> encode(V input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                Object object = typeGetter.apply(input);
                prefix.add(typeKey, typeCodec.encodeStart(ops, object));
                DataResult<T> dataResult = this.encode((Codec)parametersCodecGetter.apply(object), input, ops);
                if (dataResult.result().isEmpty() || !Objects.equals(dataResult.result().get(), ops.emptyMap())) {
                    prefix.add(parametersKey, dataResult);
                }
                return prefix;
            }

            private <T, V2 extends V> DataResult<T> encode(Codec<V2> codec, V value, DynamicOps<T> ops) {
                return codec.encodeStart(ops, value);
            }
        };
    }

    public static <A> Codec<Optional<A>> optional(final Codec<A> codec) {
        return new Codec<Optional<A>>(){

            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> ops, T input) {
                if (7.isEmpty(ops, input)) {
                    return DataResult.success((Object)Pair.of(Optional.empty(), input));
                }
                return codec.decode(ops, input).map(pair -> pair.mapFirst(Optional::of));
            }

            private static <T> boolean isEmpty(DynamicOps<T> ops, T input) {
                Optional optional = ops.getMap(input).result();
                return optional.isPresent() && ((MapLike)optional.get()).entries().findAny().isEmpty();
            }

            public <T> DataResult<T> encode(Optional<A> optional, DynamicOps<T> dynamicOps, T object) {
                if (optional.isEmpty()) {
                    return DataResult.success((Object)dynamicOps.emptyMap());
                }
                return codec.encode(optional.get(), dynamicOps, object);
            }

            public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
                return this.encode((Optional)input, ops, prefix);
            }
        };
    }

    @Deprecated
    public static <E extends Enum<E>> Codec<E> enumByName(Function<String, E> valueOf) {
        return Codec.STRING.comapFlatMap(id -> {
            try {
                return DataResult.success((Object)((Enum)valueOf.apply((String)id)));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return DataResult.error(() -> "No value with id: " + id);
            }
        }, Enum::toString);
    }

    public record StrictUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements Codec<Map<K, V>>,
    BaseMapCodec<K, V>
    {
        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (Pair pair : input.entries().toList()) {
                DataResult dataResult2;
                DataResult dataResult = this.keyCodec().parse(ops, pair.getFirst());
                DataResult dataResult3 = dataResult.apply2stable(Pair::of, dataResult2 = this.elementCodec().parse(ops, pair.getSecond()));
                Optional optional = dataResult3.error();
                if (optional.isPresent()) {
                    String string = ((DataResult.Error)optional.get()).message();
                    return DataResult.error(() -> {
                        if (dataResult.result().isPresent()) {
                            return "Map entry '" + String.valueOf(dataResult.result().get()) + "' : " + string;
                        }
                        return string;
                    });
                }
                if (dataResult3.result().isPresent()) {
                    Pair pair2 = (Pair)dataResult3.result().get();
                    builder.put(pair2.getFirst(), pair2.getSecond());
                    continue;
                }
                return DataResult.error(() -> "Empty or invalid map contents are not allowed");
            }
            ImmutableMap map = builder.build();
            return DataResult.success((Object)map);
        }

        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> this.decode(ops, (Object)map)).map(map -> Pair.of((Object)map, (Object)input));
        }

        public <T> DataResult<T> encode(Map<K, V> map, DynamicOps<T> dynamicOps, T object) {
            return this.encode(map, dynamicOps, dynamicOps.mapBuilder()).build(object);
        }

        @Override
        public String toString() {
            return "StrictUnboundedMapCodec[" + String.valueOf(this.keyCodec) + " -> " + String.valueOf(this.elementCodec) + "]";
        }

        public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
            return this.encode((Map)input, ops, prefix);
        }
    }

    public record TagEntryId(Identifier id, boolean tag) {
        @Override
        public String toString() {
            return this.asString();
        }

        private String asString() {
            return this.tag ? Codecs.HEX_PREFIX + String.valueOf(this.id) : this.id.toString();
        }
    }

    public static class IdMapper<I, V> {
        private final BiMap<I, V> values = HashBiMap.create();

        public Codec<V> getCodec(Codec<I> idCodec) {
            BiMap biMap = this.values.inverse();
            return Codecs.idChecked(idCodec, arg_0 -> this.values.get(arg_0), arg_0 -> biMap.get(arg_0));
        }

        public IdMapper<I, V> put(I id, V value) {
            Objects.requireNonNull(value, () -> "Value for " + String.valueOf(id) + " is null");
            this.values.put(id, value);
            return this;
        }

        public Set<V> values() {
            return Collections.unmodifiableSet(this.values.values());
        }
    }
}

