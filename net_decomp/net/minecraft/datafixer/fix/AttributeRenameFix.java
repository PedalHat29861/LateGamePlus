/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;

public class AttributeRenameFix
extends DataFix {
    private final String name;
    private final UnaryOperator<String> renamer;

    public AttributeRenameFix(Schema outputSchema, String name, UnaryOperator<String> renamer) {
        super(outputSchema, false);
        this.name = name;
        this.renamer = renamer;
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped(this.name + " (Components)", this.getInputSchema().getType(TypeReferences.DATA_COMPONENTS), this::applyToComponents), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(TypeReferences.ENTITY), this::applyToEntity), this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(TypeReferences.PLAYER), this::applyToEntity)});
    }

    private Typed<?> applyToComponents(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("minecraft:attribute_modifiers", dynamic2 -> dynamic2.update("modifiers", dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asStreamOpt().result().map(stream -> stream.map(this::applyToTypeField)).map(arg_0 -> ((Dynamic)dynamic).createList(arg_0)), (Object)dynamic))));
    }

    private Typed<?> applyToEntity(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("attributes", dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asStreamOpt().result().map(stream -> stream.map(this::applyToIdField)).map(arg_0 -> ((Dynamic)dynamic).createList(arg_0)), (Object)dynamic)));
    }

    private Dynamic<?> applyToIdField(Dynamic<?> dynamic) {
        return FixUtil.apply(dynamic, "id", this.renamer);
    }

    private Dynamic<?> applyToTypeField(Dynamic<?> dynamic) {
        return FixUtil.apply(dynamic, "type", this.renamer);
    }
}

