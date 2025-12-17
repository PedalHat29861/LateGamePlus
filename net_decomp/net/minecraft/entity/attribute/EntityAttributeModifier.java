/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.entity.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public record EntityAttributeModifier(Identifier id, double value, Operation operation) {
    public static final MapCodec<EntityAttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("id").forGetter(EntityAttributeModifier::id), (App)Codec.DOUBLE.fieldOf("amount").forGetter(EntityAttributeModifier::value), (App)Operation.CODEC.fieldOf("operation").forGetter(EntityAttributeModifier::operation)).apply((Applicative)instance, EntityAttributeModifier::new));
    public static final Codec<EntityAttributeModifier> CODEC = MAP_CODEC.codec();
    public static final PacketCodec<ByteBuf, EntityAttributeModifier> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, EntityAttributeModifier::id, PacketCodecs.DOUBLE, EntityAttributeModifier::value, Operation.PACKET_CODEC, EntityAttributeModifier::operation, EntityAttributeModifier::new);

    public boolean idMatches(Identifier id) {
        return id.equals(this.id);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityAttributeModifier.class, "id;amount;operation", "id", "value", "operation"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityAttributeModifier.class, "id;amount;operation", "id", "value", "operation"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityAttributeModifier.class, "id;amount;operation", "id", "value", "operation"}, this, o);
    }

    public static final class Operation
    extends Enum<Operation>
    implements StringIdentifiable {
        public static final /* enum */ Operation ADD_VALUE = new Operation("add_value", 0);
        public static final /* enum */ Operation ADD_MULTIPLIED_BASE = new Operation("add_multiplied_base", 1);
        public static final /* enum */ Operation ADD_MULTIPLIED_TOTAL = new Operation("add_multiplied_total", 2);
        public static final IntFunction<Operation> ID_TO_VALUE;
        public static final PacketCodec<ByteBuf, Operation> PACKET_CODEC;
        public static final Codec<Operation> CODEC;
        private final String name;
        private final int id;
        private static final /* synthetic */ Operation[] field_6333;

        public static Operation[] values() {
            return (Operation[])field_6333.clone();
        }

        public static Operation valueOf(String string) {
            return Enum.valueOf(Operation.class, string);
        }

        private Operation(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Operation[] method_36614() {
            return new Operation[]{ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL};
        }

        static {
            field_6333 = Operation.method_36614();
            ID_TO_VALUE = ValueLists.createIndexToValueFunction(Operation::getId, Operation.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, Operation::getId);
            CODEC = StringIdentifiable.createCodec(Operation::values);
        }
    }
}

