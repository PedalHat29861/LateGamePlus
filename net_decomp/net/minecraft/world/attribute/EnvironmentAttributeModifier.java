/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.BooleanModifier;
import net.minecraft.world.attribute.ColorModifier;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.FloatModifier;

public interface EnvironmentAttributeModifier<Subject, Argument> {
    public static final Map<Type, EnvironmentAttributeModifier<Boolean, ?>> BOOLEAN_MODIFIERS = Map.of(Type.AND, BooleanModifier.AND, Type.NAND, BooleanModifier.NAND, Type.OR, BooleanModifier.OR, Type.NOR, BooleanModifier.NOR, Type.XOR, BooleanModifier.XOR, Type.XNOR, BooleanModifier.XNOR);
    public static final Map<Type, EnvironmentAttributeModifier<Float, ?>> FLOAT_MODIFIERS = Map.of(Type.ALPHA_BLEND, FloatModifier.ALPHA_BLEND, Type.ADD, FloatModifier.ADD, Type.SUBTRACT, FloatModifier.SUBTRACT, Type.MULTIPLY, FloatModifier.MULTIPLY, Type.MINIMUM, FloatModifier.MINIMUM, Type.MAXIMUM, FloatModifier.MAXIMUM);
    public static final Map<Type, EnvironmentAttributeModifier<Integer, ?>> RGB = Map.of(Type.ALPHA_BLEND, ColorModifier.ALPHA_BLEND, Type.ADD, ColorModifier.ADD, Type.SUBTRACT, ColorModifier.SUBTRACT, Type.MULTIPLY, ColorModifier.MULTIPLY_RGB, Type.BLEND_TO_GRAY, ColorModifier.BLEND_TO_GRAY);
    public static final Map<Type, EnvironmentAttributeModifier<Integer, ?>> ARGB = Map.of(Type.ALPHA_BLEND, ColorModifier.ALPHA_BLEND, Type.ADD, ColorModifier.ADD, Type.SUBTRACT, ColorModifier.SUBTRACT, Type.MULTIPLY, ColorModifier.MULTIPLY_ARGB, Type.BLEND_TO_GRAY, ColorModifier.BLEND_TO_GRAY);

    public static <Value> EnvironmentAttributeModifier<Value, Value> override() {
        return OverrideModifier.INSTANCE;
    }

    public Subject apply(Subject var1, Argument var2);

    public Codec<Argument> argumentCodec(EnvironmentAttribute<Subject> var1);

    public Interpolator<Argument> argumentKeyframeLerp(EnvironmentAttribute<Subject> var1);

    public record OverrideModifier<Value>() implements EnvironmentAttributeModifier<Value, Value>
    {
        static final OverrideModifier<?> INSTANCE = new OverrideModifier();

        @Override
        public Value apply(Value object, Value object2) {
            return object2;
        }

        @Override
        public Codec<Value> argumentCodec(EnvironmentAttribute<Value> environmentAttribute) {
            return environmentAttribute.getCodec();
        }

        @Override
        public Interpolator<Value> argumentKeyframeLerp(EnvironmentAttribute<Value> environmentAttribute) {
            return environmentAttribute.getType().keyframeLerp();
        }
    }

    public static final class Type
    extends Enum<Type>
    implements StringIdentifiable {
        public static final /* enum */ Type OVERRIDE = new Type("override");
        public static final /* enum */ Type ALPHA_BLEND = new Type("alpha_blend");
        public static final /* enum */ Type ADD = new Type("add");
        public static final /* enum */ Type SUBTRACT = new Type("subtract");
        public static final /* enum */ Type MULTIPLY = new Type("multiply");
        public static final /* enum */ Type BLEND_TO_GRAY = new Type("blend_to_gray");
        public static final /* enum */ Type MINIMUM = new Type("minimum");
        public static final /* enum */ Type MAXIMUM = new Type("maximum");
        public static final /* enum */ Type AND = new Type("and");
        public static final /* enum */ Type NAND = new Type("nand");
        public static final /* enum */ Type OR = new Type("or");
        public static final /* enum */ Type NOR = new Type("nor");
        public static final /* enum */ Type XOR = new Type("xor");
        public static final /* enum */ Type XNOR = new Type("xnor");
        public static final Codec<Type> CODEC;
        private final String name;
        private static final /* synthetic */ Type[] field_63786;

        public static Type[] values() {
            return (Type[])field_63786.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Type[] method_75714() {
            return new Type[]{OVERRIDE, ALPHA_BLEND, ADD, SUBTRACT, MULTIPLY, BLEND_TO_GRAY, MINIMUM, MAXIMUM, AND, NAND, OR, NOR, XOR, XNOR};
        }

        static {
            field_63786 = Type.method_75714();
            CODEC = StringIdentifiable.createCodec(Type::values);
        }
    }
}

