/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.datafixer.TypeReferences;

public class EquipmentFormatFix
extends DataFix {
    public EquipmentFormatFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getTypeRaw(TypeReferences.ITEM_STACK);
        Type type2 = this.getOutputSchema().getTypeRaw(TypeReferences.ITEM_STACK);
        OpticFinder opticFinder = type.findField("id");
        return this.method_66619(type, type2, opticFinder);
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule method_66619(Type<ItemStackOld> type, Type<ItemStackNew> type2, OpticFinder<?> opticFinder) {
        Type type3 = DSL.named((String)TypeReferences.ENTITY_EQUIPMENT.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"ArmorItems", (Type)DSL.list(type))), (Type)DSL.optional((Type)DSL.field((String)"HandItems", (Type)DSL.list(type))), (Type)DSL.optional((Type)DSL.field((String)"body_armor_item", type)), (Type)DSL.optional((Type)DSL.field((String)"saddle", type))));
        Type type4 = DSL.named((String)TypeReferences.ENTITY_EQUIPMENT.typeName(), (Type)DSL.optional((Type)DSL.field((String)"equipment", (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"mainhand", type2)), (Type)DSL.optional((Type)DSL.field((String)"offhand", type2)), (Type)DSL.optional((Type)DSL.field((String)"feet", type2)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"legs", type2)), (Type)DSL.optional((Type)DSL.field((String)"chest", type2)), (Type)DSL.optional((Type)DSL.field((String)"head", type2)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"body", type2)), (Type)DSL.optional((Type)DSL.field((String)"saddle", type2)), (Type)DSL.remainderType()))))));
        if (!type3.equals((Object)this.getInputSchema().getType(TypeReferences.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!type4.equals((Object)this.getOutputSchema().getType(TypeReferences.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return this.fixTypeEverywhere("EquipmentFormatFix", type3, type4, dynamicOps -> {
            Predicate<Object> predicate = object -> {
                Typed typed = new Typed(type, dynamicOps, object);
                return typed.getOptional(opticFinder).isEmpty();
            };
            return pair -> {
                String string = (String)pair.getFirst();
                Pair pair2 = (Pair)pair.getSecond();
                List list = (List)((Either)pair2.getFirst()).map(Function.identity(), unit -> List.of());
                List list2 = (List)((Either)((Pair)pair2.getSecond()).getFirst()).map(Function.identity(), unit -> List.of());
                Either either = (Either)((Pair)((Pair)pair2.getSecond()).getSecond()).getFirst();
                Either either2 = (Either)((Pair)((Pair)pair2.getSecond()).getSecond()).getSecond();
                Either either3 = EquipmentFormatFix.method_66617(0, list, predicate);
                Either either4 = EquipmentFormatFix.method_66617(1, list, predicate);
                Either either5 = EquipmentFormatFix.method_66617(2, list, predicate);
                Either either6 = EquipmentFormatFix.method_66617(3, list, predicate);
                Either either7 = EquipmentFormatFix.method_66617(0, list2, predicate);
                Either either8 = EquipmentFormatFix.method_66617(1, list2, predicate);
                if (EquipmentFormatFix.method_66623(either, either2, either3, either4, either5, either6, either7, either8)) {
                    return Pair.of((Object)string, (Object)Either.right((Object)Unit.INSTANCE));
                }
                return Pair.of((Object)string, (Object)Either.left((Object)Pair.of(either7, (Object)Pair.of(either8, (Object)Pair.of(either3, (Object)Pair.of(either4, (Object)Pair.of(either5, (Object)Pair.of(either6, (Object)Pair.of((Object)either, (Object)Pair.of((Object)either2, (Object)new Dynamic(dynamicOps)))))))))));
            };
        });
    }

    @SafeVarargs
    private static boolean method_66623(Either<?, Unit> ... eithers) {
        for (Either<?, Unit> either : eithers) {
            if (!either.right().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static <ItemStack> Either<ItemStack, Unit> method_66617(int i, List<ItemStack> list, Predicate<ItemStack> predicate) {
        if (i >= list.size()) {
            return Either.right((Object)Unit.INSTANCE);
        }
        ItemStack object = list.get(i);
        if (predicate.test(object)) {
            return Either.right((Object)Unit.INSTANCE);
        }
        return Either.left(object);
    }
}

