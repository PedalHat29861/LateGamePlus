/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityTransformFix;
import net.minecraft.util.Util;

public class EntityZombieSplitFix
extends EntityTransformFix {
    private final Supplier<Type<?>> ZOMBIE_VILLAGER_TYPE = Suppliers.memoize(() -> this.getOutputSchema().getChoiceType(TypeReferences.ENTITY, "ZombieVillager"));

    public EntityZombieSplitFix(Schema outputSchema) {
        super("EntityZombieSplitFix", outputSchema, true);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> entityTyped) {
        String string;
        if (!choice.equals("Zombie")) {
            return Pair.of((Object)choice, entityTyped);
        }
        Dynamic dynamic = (Dynamic)entityTyped.getOptional(DSL.remainderFinder()).orElseThrow();
        int i = dynamic.get("ZombieType").asInt(0);
        return Pair.of((Object)string, (Object)(switch (i) {
            default -> {
                string = "Zombie";
                yield entityTyped;
            }
            case 1, 2, 3, 4, 5 -> {
                string = "ZombieVillager";
                yield this.setZombieVillagerProfession(entityTyped, i - 1);
            }
            case 6 -> {
                string = "Husk";
                yield entityTyped;
            }
        }).update(DSL.remainderFinder(), entityDynamic -> entityDynamic.remove("ZombieType")));
    }

    private Typed<?> setZombieVillagerProfession(Typed<?> entityTyped, int variant) {
        return Util.apply(entityTyped, this.ZOMBIE_VILLAGER_TYPE.get(), zombieVillagerDynamic -> zombieVillagerDynamic.set("Profession", zombieVillagerDynamic.createInt(variant)));
    }
}

