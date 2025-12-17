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
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ChoiceFix;
import net.minecraft.util.math.random.Random;

public class EntityZombieVillagerTypeFix
extends ChoiceFix {
    private static final int TYPE_COUNT = 6;

    public EntityZombieVillagerTypeFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityZombieVillagerTypeFix", TypeReferences.ENTITY, "Zombie");
    }

    public Dynamic<?> fixZombieType(Dynamic<?> zombieDynamic) {
        if (zombieDynamic.get("IsVillager").asBoolean(false)) {
            if (zombieDynamic.get("ZombieType").result().isEmpty()) {
                int i = this.clampType(zombieDynamic.get("VillagerProfession").asInt(-1));
                if (i == -1) {
                    i = this.clampType(Random.create().nextInt(6));
                }
                zombieDynamic = zombieDynamic.set("ZombieType", zombieDynamic.createInt(i));
            }
            zombieDynamic = zombieDynamic.remove("IsVillager");
        }
        return zombieDynamic;
    }

    private int clampType(int type) {
        if (type < 0 || type >= 6) {
            return -1;
        }
        return type;
    }

    @Override
    protected Typed<?> transform(Typed<?> inputTyped) {
        return inputTyped.update(DSL.remainderFinder(), this::fixZombieType);
    }
}

