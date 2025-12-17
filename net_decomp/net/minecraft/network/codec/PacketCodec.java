/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.util.Function10
 *  com.mojang.datafixers.util.Function11
 *  com.mojang.datafixers.util.Function12
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Function5
 *  com.mojang.datafixers.util.Function6
 *  com.mojang.datafixers.util.Function7
 *  com.mojang.datafixers.util.Function8
 *  com.mojang.datafixers.util.Function9
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.network.codec.ValueFirstEncoder;

public interface PacketCodec<B, V>
extends PacketDecoder<B, V>,
PacketEncoder<B, V> {
    public static <B, V> PacketCodec<B, V> ofStatic(final PacketEncoder<B, V> encoder, final PacketDecoder<B, V> decoder) {
        return new PacketCodec<B, V>(){

            @Override
            public V decode(B object) {
                return decoder.decode(object);
            }

            @Override
            public void encode(B object, V object2) {
                encoder.encode(object, object2);
            }
        };
    }

    public static <B, V> PacketCodec<B, V> of(final ValueFirstEncoder<B, V> encoder, final PacketDecoder<B, V> decoder) {
        return new PacketCodec<B, V>(){

            @Override
            public V decode(B object) {
                return decoder.decode(object);
            }

            @Override
            public void encode(B object, V object2) {
                encoder.encode(object2, object);
            }
        };
    }

    public static <B, V> PacketCodec<B, V> unit(final V value) {
        return new PacketCodec<B, V>(){

            @Override
            public V decode(B object) {
                return value;
            }

            @Override
            public void encode(B object, V object2) {
                if (!object2.equals(value)) {
                    throw new IllegalStateException("Can't encode '" + String.valueOf(object2) + "', expected '" + String.valueOf(value) + "'");
                }
            }
        };
    }

    default public <O> PacketCodec<B, O> collect(ResultFunction<B, V, O> function) {
        return function.apply(this);
    }

    default public <O> PacketCodec<B, O> xmap(final Function<? super V, ? extends O> to, final Function<? super O, ? extends V> from) {
        return new PacketCodec<B, O>(){

            @Override
            public O decode(B object) {
                return to.apply(PacketCodec.this.decode(object));
            }

            @Override
            public void encode(B object, O object2) {
                PacketCodec.this.encode(object, from.apply(object2));
            }
        };
    }

    default public <O extends ByteBuf> PacketCodec<O, V> mapBuf(final Function<O, ? extends B> function) {
        return new PacketCodec<O, V>(){

            @Override
            public V decode(O byteBuf) {
                Object object = function.apply(byteBuf);
                return PacketCodec.this.decode(object);
            }

            @Override
            public void encode(O byteBuf, V object) {
                Object object2 = function.apply(byteBuf);
                PacketCodec.this.encode(object2, object);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((O)((ByteBuf)object), (V)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((O)((ByteBuf)object));
            }
        };
    }

    default public <U> PacketCodec<B, U> dispatch(final Function<? super U, ? extends V> type, final Function<? super V, ? extends PacketCodec<? super B, ? extends U>> codec) {
        return new PacketCodec<B, U>(){

            @Override
            public U decode(B object) {
                Object object2 = PacketCodec.this.decode(object);
                PacketCodec packetCodec = (PacketCodec)codec.apply(object2);
                return packetCodec.decode(object);
            }

            @Override
            public void encode(B object, U object2) {
                Object object3 = type.apply(object2);
                PacketCodec packetCodec = (PacketCodec)codec.apply(object3);
                PacketCodec.this.encode(object, object3);
                packetCodec.encode(object, object2);
            }
        };
    }

    public static <B, C, T1> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec, final Function<C, T1> from, final Function<T1, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec.decode(object);
                return to.apply(object2);
            }

            @Override
            public void encode(B object, C object2) {
                codec.encode(object, from.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final BiFunction<T1, T2, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                return to.apply(object2, object3);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final Function3<T1, T2, T3, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                return to.apply(object2, object3, object4);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final Function4<T1, T2, T3, T4, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                return to.apply(object2, object3, object4, object5);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final Function5<T1, T2, T3, T4, T5, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                return to.apply(object2, object3, object4, object5, object6);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final Function6<T1, T2, T3, T4, T5, T6, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final PacketCodec<? super B, T7> codec7, final Function<C, T7> from7, final Function7<T1, T2, T3, T4, T5, T6, T7, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                Object object8 = codec7.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final PacketCodec<? super B, T7> codec7, final Function<C, T7> from7, final PacketCodec<? super B, T8> codec8, final Function<C, T8> from8, final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                Object object8 = codec7.decode(object);
                Object object9 = codec8.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final PacketCodec<? super B, T7> codec7, final Function<C, T7> from7, final PacketCodec<? super B, T8> codec8, final Function<C, T8> from8, final PacketCodec<? super B, T9> codec9, final Function<C, T9> from9, final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                Object object8 = codec7.decode(object);
                Object object9 = codec8.decode(object);
                Object object10 = codec9.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final PacketCodec<? super B, T7> codec7, final Function<C, T7> from7, final PacketCodec<? super B, T8> codec8, final Function<C, T8> from8, final PacketCodec<? super B, T9> codec9, final Function<C, T9> from9, final PacketCodec<? super B, T10> codec10, final Function<C, T10> from10, final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                Object object8 = codec7.decode(object);
                Object object9 = codec8.decode(object);
                Object object10 = codec9.decode(object);
                Object object11 = codec10.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final PacketCodec<? super B, T7> codec7, final Function<C, T7> from7, final PacketCodec<? super B, T8> codec8, final Function<C, T8> from8, final PacketCodec<? super B, T9> codec9, final Function<C, T9> from9, final PacketCodec<? super B, T10> codec10, final Function<C, T10> from10, final PacketCodec<? super B, T11> codec11, final Function<C, T11> from11, final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                Object object8 = codec7.decode(object);
                Object object9 = codec8.decode(object);
                Object object10 = codec9.decode(object);
                Object object11 = codec10.decode(object);
                Object object12 = codec11.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> PacketCodec<B, C> tuple(final PacketCodec<? super B, T1> codec1, final Function<C, T1> from1, final PacketCodec<? super B, T2> codec2, final Function<C, T2> from2, final PacketCodec<? super B, T3> codec3, final Function<C, T3> from3, final PacketCodec<? super B, T4> codec4, final Function<C, T4> from4, final PacketCodec<? super B, T5> codec5, final Function<C, T5> from5, final PacketCodec<? super B, T6> codec6, final Function<C, T6> from6, final PacketCodec<? super B, T7> codec7, final Function<C, T7> from7, final PacketCodec<? super B, T8> codec8, final Function<C, T8> from8, final PacketCodec<? super B, T9> codec9, final Function<C, T9> from9, final PacketCodec<? super B, T10> codec10, final Function<C, T10> from10, final PacketCodec<? super B, T11> codec11, final Function<C, T11> from11, final PacketCodec<? super B, T12> codec12, final Function<C, T12> from12, final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> to) {
        return new PacketCodec<B, C>(){

            @Override
            public C decode(B object) {
                Object object2 = codec1.decode(object);
                Object object3 = codec2.decode(object);
                Object object4 = codec3.decode(object);
                Object object5 = codec4.decode(object);
                Object object6 = codec5.decode(object);
                Object object7 = codec6.decode(object);
                Object object8 = codec7.decode(object);
                Object object9 = codec8.decode(object);
                Object object10 = codec9.decode(object);
                Object object11 = codec10.decode(object);
                Object object12 = codec11.decode(object);
                Object object13 = codec12.decode(object);
                return to.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12, object13);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, from1.apply(object2));
                codec2.encode(object, from2.apply(object2));
                codec3.encode(object, from3.apply(object2));
                codec4.encode(object, from4.apply(object2));
                codec5.encode(object, from5.apply(object2));
                codec6.encode(object, from6.apply(object2));
                codec7.encode(object, from7.apply(object2));
                codec8.encode(object, from8.apply(object2));
                codec9.encode(object, from9.apply(object2));
                codec10.encode(object, from10.apply(object2));
                codec11.encode(object, from11.apply(object2));
                codec12.encode(object, from12.apply(object2));
            }
        };
    }

    public static <B, T> PacketCodec<B, T> recursive(final UnaryOperator<PacketCodec<B, T>> codecGetter) {
        return new PacketCodec<B, T>(){
            private final Supplier<PacketCodec<B, T>> codecSupplier = Suppliers.memoize(() -> (PacketCodec)codecGetter.apply(this));

            @Override
            public T decode(B object) {
                return this.codecSupplier.get().decode(object);
            }

            @Override
            public void encode(B object, T object2) {
                this.codecSupplier.get().encode(object, object2);
            }
        };
    }

    default public <S extends B> PacketCodec<S, V> cast() {
        return this;
    }

    @FunctionalInterface
    public static interface ResultFunction<B, S, T> {
        public PacketCodec<B, T> apply(PacketCodec<B, S> var1);
    }
}

