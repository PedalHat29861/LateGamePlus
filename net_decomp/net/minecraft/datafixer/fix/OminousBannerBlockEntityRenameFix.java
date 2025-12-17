/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;

public class OminousBannerBlockEntityRenameFix
extends ChoiceFix {
    public OminousBannerBlockEntityRenameFix(Schema schema, boolean bl) {
        super(schema, bl, "OminousBannerBlockEntityRenameFix", TypeReferences.BLOCK_ENTITY, "minecraft:banner");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        OpticFinder opticFinder = inputTyped.getType().findField("CustomName");
        OpticFinder opticFinder2 = DSL.typeFinder((Type)this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT));
        return inputTyped.updateTyped(opticFinder, typed -> typed.update(opticFinder2, pair -> pair.mapSecond(string -> string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\""))));
    }
}

