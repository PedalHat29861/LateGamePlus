/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.datafixer.TypeReferences;

public class RemoveFeatureTogglesFix
extends DataFix {
    private final String name;
    private final Set<String> featureToggleIds;

    public RemoveFeatureTogglesFix(Schema outputSchema, String name, Set<String> featureToggleIds) {
        super(outputSchema, false);
        this.name = name;
        this.featureToggleIds = featureToggleIds;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(TypeReferences.LIGHTWEIGHT_LEVEL), levelTyped -> levelTyped.update(DSL.remainderFinder(), this::removeFeatureToggles));
    }

    private <T> Dynamic<T> removeFeatureToggles(Dynamic<T> dynamic) {
        List list = dynamic.get("removed_features").asStream().collect(Collectors.toCollection(ArrayList::new));
        Dynamic dynamic2 = dynamic.update("enabled_features", enabledFeatures -> (Dynamic)DataFixUtils.orElse(enabledFeatures.asStreamOpt().result().map(stream -> stream.filter(enabledFeature -> {
            Optional optional = enabledFeature.asString().result();
            if (optional.isEmpty()) {
                return true;
            }
            boolean bl = this.featureToggleIds.contains(optional.get());
            if (bl) {
                list.add(dynamic.createString((String)optional.get()));
            }
            return !bl;
        })).map(arg_0 -> ((Dynamic)dynamic).createList(arg_0)), (Object)enabledFeatures));
        if (!list.isEmpty()) {
            dynamic2 = dynamic2.set("removed_features", dynamic.createList(list.stream()));
        }
        return dynamic2;
    }
}

