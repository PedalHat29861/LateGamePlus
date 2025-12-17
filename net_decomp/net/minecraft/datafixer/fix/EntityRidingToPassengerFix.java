/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class EntityRidingToPassengerFix
extends DataFix {
    public EntityRidingToPassengerFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Schema schema2 = this.getOutputSchema();
        Type type = schema.getTypeRaw(TypeReferences.ENTITY_TREE);
        Type type2 = schema2.getTypeRaw(TypeReferences.ENTITY_TREE);
        Type type3 = schema.getTypeRaw(TypeReferences.ENTITY);
        return this.fixEntityTree(schema, schema2, type, type2, type3);
    }

    private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule fixEntityTree(Schema inputSchema, Schema outputSchema, Type<OldEntityTree> inputEntityTreeType, Type<NewEntityTree> outputEntityTreeType, Type<Entity> inputEntityType) {
        Type type = DSL.named((String)TypeReferences.ENTITY_TREE.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"Riding", inputEntityTreeType)), inputEntityType));
        Type type2 = DSL.named((String)TypeReferences.ENTITY_TREE.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"Passengers", (Type)DSL.list(outputEntityTreeType))), inputEntityType));
        Type type3 = inputSchema.getType(TypeReferences.ENTITY_TREE);
        Type type4 = outputSchema.getType(TypeReferences.ENTITY_TREE);
        if (!Objects.equals(type3, type)) {
            throw new IllegalStateException("Old entity type is not what was expected.");
        }
        if (!type4.equals((Object)type2, true, true)) {
            throw new IllegalStateException("New entity type is not what was expected.");
        }
        OpticFinder opticFinder = DSL.typeFinder((Type)type);
        OpticFinder opticFinder2 = DSL.typeFinder((Type)type2);
        OpticFinder opticFinder3 = DSL.typeFinder(outputEntityTreeType);
        Type type5 = inputSchema.getType(TypeReferences.PLAYER);
        Type type6 = outputSchema.getType(TypeReferences.PLAYER);
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhere("EntityRidingToPassengerFix", type, type2, dynamicOps -> pair2 -> {
            Optional<Object> optional = Optional.empty();
            Pair pair22 = pair2;
            while (true) {
                Either either = (Either)DataFixUtils.orElse(optional.map(pair -> {
                    Typed typed = (Typed)outputEntityTreeType.pointTyped(dynamicOps).orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                    Object object = typed.set(opticFinder2, pair).getOptional(opticFinder3).orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                    return Either.left((Object)ImmutableList.of(object));
                }), (Object)Either.right((Object)DSL.unit()));
                optional = Optional.of(Pair.of((Object)TypeReferences.ENTITY_TREE.typeName(), (Object)Pair.of((Object)either, (Object)((Pair)pair22.getSecond()).getSecond())));
                Optional optional2 = ((Either)((Pair)pair22.getSecond()).getFirst()).left();
                if (optional2.isEmpty()) break;
                pair22 = (Pair)new Typed(inputEntityTreeType, dynamicOps, optional2.get()).getOptional(opticFinder).orElseThrow(() -> new IllegalStateException("Should always have an entity here"));
            }
            return (Pair)optional.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
        }), (TypeRewriteRule)this.writeAndRead("player RootVehicle injecter", type5, type6));
    }
}

