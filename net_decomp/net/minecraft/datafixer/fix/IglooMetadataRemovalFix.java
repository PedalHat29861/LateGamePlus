/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class IglooMetadataRemovalFix
extends DataFix {
    public IglooMetadataRemovalFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", type, structureFeatureTyped -> structureFeatureTyped.update(DSL.remainderFinder(), IglooMetadataRemovalFix::removeMetadata));
    }

    private static <T> Dynamic<T> removeMetadata(Dynamic<T> structureFeatureDynamic) {
        boolean bl = structureFeatureDynamic.get("Children").asStreamOpt().map(stream -> stream.allMatch(IglooMetadataRemovalFix::isIgloo)).result().orElse(false);
        if (bl) {
            return structureFeatureDynamic.set("id", structureFeatureDynamic.createString("Igloo")).remove("Children");
        }
        return structureFeatureDynamic.update("Children", IglooMetadataRemovalFix::removeIgloos);
    }

    private static <T> Dynamic<T> removeIgloos(Dynamic<T> structureFeatureDynamic) {
        return structureFeatureDynamic.asStreamOpt().map(stream -> stream.filter(dynamic -> !IglooMetadataRemovalFix.isIgloo(dynamic))).map(arg_0 -> structureFeatureDynamic.createList(arg_0)).result().orElse(structureFeatureDynamic);
    }

    private static boolean isIgloo(Dynamic<?> structureFeatureDynamic) {
        return structureFeatureDynamic.get("id").asString("").equals("Iglu");
    }
}

