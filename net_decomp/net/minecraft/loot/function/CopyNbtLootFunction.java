/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.function;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.context.ContextParameter;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class CopyNbtLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<CopyNbtLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyNbtLootFunction.addConditionsField(instance).and(instance.group((App)LootNbtProviderTypes.CODEC.fieldOf("source").forGetter(function -> function.source), (App)Operation.CODEC.listOf().fieldOf("ops").forGetter(function -> function.operations))).apply((Applicative)instance, CopyNbtLootFunction::new));
    private final LootNbtProvider source;
    private final List<Operation> operations;

    CopyNbtLootFunction(List<LootCondition> conditions, LootNbtProvider source, List<Operation> operations) {
        super(conditions);
        this.source = source;
        this.operations = List.copyOf(operations);
    }

    public LootFunctionType<CopyNbtLootFunction> getType() {
        return LootFunctionTypes.COPY_CUSTOM_DATA;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.source.getRequiredParameters();
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        NbtElement nbtElement = this.source.getNbt(context);
        if (nbtElement == null) {
            return stack;
        }
        @Nullable MutableObject mutableObject = new MutableObject();
        Supplier<NbtElement> supplier = () -> {
            if (mutableObject.get() == null) {
                mutableObject.setValue((Object)stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt());
            }
            return (NbtElement)mutableObject.get();
        };
        this.operations.forEach(operation -> operation.execute(supplier, nbtElement));
        NbtCompound nbtCompound = (NbtCompound)mutableObject.get();
        if (nbtCompound != null) {
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbtCompound);
        }
        return stack;
    }

    @Deprecated
    public static Builder builder(LootNbtProvider source) {
        return new Builder(source);
    }

    public static Builder builder(LootContext.EntityReference target) {
        return new Builder(ContextLootNbtProvider.fromTarget(target));
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final LootNbtProvider source;
        private final List<Operation> operations = Lists.newArrayList();

        Builder(LootNbtProvider source) {
            this.source = source;
        }

        public Builder withOperation(String source, String target, Operator operator) {
            try {
                this.operations.add(new Operation(NbtPathArgumentType.NbtPath.parse(source), NbtPathArgumentType.NbtPath.parse(target), operator));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException(commandSyntaxException);
            }
            return this;
        }

        public Builder withOperation(String source, String target) {
            return this.withOperation(source, target, Operator.REPLACE);
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyNbtLootFunction(this.getConditions(), this.source, this.operations);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    record Operation(NbtPathArgumentType.NbtPath parsedSourcePath, NbtPathArgumentType.NbtPath parsedTargetPath, Operator operator) {
        public static final Codec<Operation> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NbtPathArgumentType.NbtPath.CODEC.fieldOf("source").forGetter(Operation::parsedSourcePath), (App)NbtPathArgumentType.NbtPath.CODEC.fieldOf("target").forGetter(Operation::parsedTargetPath), (App)Operator.CODEC.fieldOf("op").forGetter(Operation::operator)).apply((Applicative)instance, Operation::new));

        public void execute(Supplier<NbtElement> itemNbtGetter, NbtElement sourceEntityNbt) {
            try {
                List<NbtElement> list = this.parsedSourcePath.get(sourceEntityNbt);
                if (!list.isEmpty()) {
                    this.operator.merge(itemNbtGetter.get(), this.parsedTargetPath, list);
                }
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Operation.class, "sourcePath;targetPath;op", "parsedSourcePath", "parsedTargetPath", "operator"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Operation.class, "sourcePath;targetPath;op", "parsedSourcePath", "parsedTargetPath", "operator"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Operation.class, "sourcePath;targetPath;op", "parsedSourcePath", "parsedTargetPath", "operator"}, this, object);
        }
    }

    public static abstract sealed class Operator
    extends Enum<Operator>
    implements StringIdentifiable {
        public static final /* enum */ Operator REPLACE = new Operator("replace"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                targetPath.put(itemNbt, (NbtElement)Iterables.getLast(sourceNbts));
            }
        };
        public static final /* enum */ Operator APPEND = new Operator("append"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                List<NbtElement> list = targetPath.getOrInit(itemNbt, NbtList::new);
                list.forEach(foundNbt -> {
                    if (foundNbt instanceof NbtList) {
                        sourceNbts.forEach(sourceNbt -> ((NbtList)foundNbt).add(sourceNbt.copy()));
                    }
                });
            }
        };
        public static final /* enum */ Operator MERGE = new Operator("merge"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                List<NbtElement> list = targetPath.getOrInit(itemNbt, NbtCompound::new);
                list.forEach(foundNbt -> {
                    if (foundNbt instanceof NbtCompound) {
                        sourceNbts.forEach(sourceNbt -> {
                            if (sourceNbt instanceof NbtCompound) {
                                ((NbtCompound)foundNbt).copyFrom((NbtCompound)sourceNbt);
                            }
                        });
                    }
                });
            }
        };
        public static final Codec<Operator> CODEC;
        private final String name;
        private static final /* synthetic */ Operator[] field_17036;

        public static Operator[] values() {
            return (Operator[])field_17036.clone();
        }

        public static Operator valueOf(String string) {
            return Enum.valueOf(Operator.class, string);
        }

        public abstract void merge(NbtElement var1, NbtPathArgumentType.NbtPath var2, List<NbtElement> var3) throws CommandSyntaxException;

        Operator(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Operator[] method_36795() {
            return new Operator[]{REPLACE, APPEND, MERGE};
        }

        static {
            field_17036 = Operator.method_36795();
            CODEC = StringIdentifiable.createCodec(Operator::values);
        }
    }
}

