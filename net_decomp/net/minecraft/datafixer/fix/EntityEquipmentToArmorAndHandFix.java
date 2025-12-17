/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class EntityEquipmentToArmorAndHandFix
extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        return this.fixEquipment(this.getInputSchema().getTypeRaw(TypeReferences.ITEM_STACK), this.getOutputSchema().getTypeRaw(TypeReferences.ITEM_STACK));
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule fixEquipment(Type<ItemStackOld> type, Type<ItemStackNew> type2) {
        Type type3 = DSL.named((String)TypeReferences.ENTITY_EQUIPMENT.typeName(), (Type)DSL.optional((Type)DSL.field((String)"Equipment", (Type)DSL.list(type))));
        Type type4 = DSL.named((String)TypeReferences.ENTITY_EQUIPMENT.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"ArmorItems", (Type)DSL.list(type2))), (Type)DSL.optional((Type)DSL.field((String)"HandItems", (Type)DSL.list(type2))), (Type)DSL.optional((Type)DSL.field((String)"body_armor_item", type2)), (Type)DSL.optional((Type)DSL.field((String)"saddle", type2))));
        if (!type3.equals((Object)this.getInputSchema().getType(TypeReferences.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!type4.equals((Object)this.getOutputSchema().getType(TypeReferences.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix - drop chances", this.getInputSchema().getType(TypeReferences.ENTITY), typed -> typed.update(DSL.remainderFinder(), EntityEquipmentToArmorAndHandFix::method_66596)), (TypeRewriteRule)this.fixTypeEverywhere("EntityEquipmentToArmorAndHandFix - equipment", type3, type4, dynamicOps -> {
            Object object = ((Pair)type2.read(new Dynamic(dynamicOps).emptyMap()).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack."))).getFirst();
            Either either = Either.right((Object)DSL.unit());
            return pair -> pair.mapSecond(either2 -> {
                List list = (List)either2.map(Function.identity(), unit -> List.of());
                Either either3 = Either.right((Object)DSL.unit());
                Either either4 = Either.right((Object)DSL.unit());
                if (!list.isEmpty()) {
                    either3 = Either.left((Object)Lists.newArrayList((Object[])new Object[]{list.getFirst(), object}));
                }
                if (list.size() > 1) {
                    ArrayList list2 = Lists.newArrayList((Object[])new Object[]{object, object, object, object});
                    for (int i = 1; i < Math.min(list.size(), 5); ++i) {
                        list2.set(i - 1, list.get(i));
                    }
                    either4 = Either.left((Object)list2);
                }
                return Pair.of((Object)either4, (Object)Pair.of((Object)either3, (Object)Pair.of((Object)either, (Object)either)));
            });
        }));
    }

    private static Dynamic<?> method_66596(Dynamic<?> dynamic2) {
        Optional optional = dynamic2.get("DropChances").asStreamOpt().result();
        dynamic2 = dynamic2.remove("DropChances");
        if (optional.isPresent()) {
            Iterator iterator = Stream.concat(((Stream)optional.get()).map(dynamic -> Float.valueOf(dynamic.asFloat(0.0f))), Stream.generate(() -> Float.valueOf(0.0f))).iterator();
            float f = ((Float)iterator.next()).floatValue();
            if (dynamic2.get("HandDropChances").result().isEmpty()) {
                dynamic2 = dynamic2.set("HandDropChances", dynamic2.createList(Stream.of(Float.valueOf(f), Float.valueOf(0.0f)).map(arg_0 -> ((Dynamic)dynamic2).createFloat(arg_0))));
            }
            if (dynamic2.get("ArmorDropChances").result().isEmpty()) {
                dynamic2 = dynamic2.set("ArmorDropChances", dynamic2.createList(Stream.of((Float)iterator.next(), (Float)iterator.next(), (Float)iterator.next(), (Float)iterator.next()).map(arg_0 -> ((Dynamic)dynamic2).createFloat(arg_0))));
            }
        }
        return dynamic2;
    }
}

