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
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;
import org.jspecify.annotations.Nullable;

public abstract class ComponentFix
extends DataFix {
    private final String name;
    private final String oldComponentId;
    private final String newComponentId;

    public ComponentFix(Schema outputSchema, String name, String componentId) {
        this(outputSchema, name, componentId, componentId);
    }

    public ComponentFix(Schema outputSchema, String name, String oldComponentId, String newComponentId) {
        super(outputSchema, false);
        this.name = name;
        this.oldComponentId = oldComponentId;
        this.newComponentId = newComponentId;
    }

    public final TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.DATA_COMPONENTS);
        return this.fixTypeEverywhereTyped(this.name, type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional = dynamic.get(this.oldComponentId).result();
            if (optional.isEmpty()) {
                return dynamic;
            }
            Dynamic dynamic2 = this.fixComponent((Dynamic)optional.get());
            return dynamic.remove(this.oldComponentId).setFieldIfPresent(this.newComponentId, Optional.ofNullable(dynamic2));
        }));
    }

    protected abstract <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> var1);
}

