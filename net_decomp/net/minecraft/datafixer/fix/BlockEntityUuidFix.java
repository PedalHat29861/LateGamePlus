/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;

public class BlockEntityUuidFix
extends AbstractUuidFix {
    public BlockEntityUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.BLOCK_ENTITY);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), typed -> {
            typed = this.updateTyped((Typed<?>)typed, "minecraft:conduit", this::updateConduit);
            typed = this.updateTyped((Typed<?>)typed, "minecraft:skull", this::updateSkull);
            return typed;
        });
    }

    private Dynamic<?> updateSkull(Dynamic<?> skullDynamic) {
        return skullDynamic.get("Owner").get().map(ownerDynamic -> BlockEntityUuidFix.updateStringUuid(ownerDynamic, "Id", "Id").orElse((Dynamic<?>)ownerDynamic)).map(ownerDynamic -> skullDynamic.remove("Owner").set("SkullOwner", ownerDynamic)).result().orElse(skullDynamic);
    }

    private Dynamic<?> updateConduit(Dynamic<?> conduitDynamic) {
        return BlockEntityUuidFix.updateCompoundUuid(conduitDynamic, "target_uuid", "Target").orElse(conduitDynamic);
    }
}

