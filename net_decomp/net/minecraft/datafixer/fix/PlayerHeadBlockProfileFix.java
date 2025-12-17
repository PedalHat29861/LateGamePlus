/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

public class PlayerHeadBlockProfileFix
extends ChoiceFix {
    public PlayerHeadBlockProfileFix(Schema outputSchema) {
        super(outputSchema, false, "PlayerHeadBlockProfileFix", TypeReferences.BLOCK_ENTITY, "minecraft:skull");
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixProfile);
    }

    private <T> Dynamic<T> fixProfile(Dynamic<T> dynamic) {
        Optional optional2;
        Optional optional = dynamic.get("SkullOwner").result();
        Optional optional3 = optional.or(() -> PlayerHeadBlockProfileFix.method_58056(optional2 = dynamic.get("ExtraType").result()));
        if (optional3.isEmpty()) {
            return dynamic;
        }
        dynamic = dynamic.remove("SkullOwner").remove("ExtraType");
        dynamic = dynamic.set("profile", ItemStackComponentizationFix.createProfileDynamic((Dynamic)optional3.get()));
        return dynamic;
    }

    private static /* synthetic */ Optional method_58056(Optional optional) {
        return optional;
    }
}

