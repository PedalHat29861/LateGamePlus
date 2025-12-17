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
import java.util.List;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.fix.TextFixes;

public class SignTextStrictJsonFix
extends ChoiceFix {
    private static final List<String> TEXT_KEYS = List.of("Text1", "Text2", "Text3", "Text4");

    public SignTextStrictJsonFix(Schema outputSchema) {
        super(outputSchema, false, "SignTextStrictJsonFix", TypeReferences.BLOCK_ENTITY, "Sign");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        for (String string : TEXT_KEYS) {
            OpticFinder opticFinder = inputTyped.getType().findField(string);
            OpticFinder opticFinder2 = DSL.typeFinder((Type)this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT));
            inputTyped = inputTyped.updateTyped(opticFinder, typed -> typed.update(opticFinder2, pair -> pair.mapSecond(TextFixes::parseLenientJson)));
        }
        return inputTyped;
    }
}

