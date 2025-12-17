/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import org.jspecify.annotations.Nullable;

public class ItemSpawnEggFix
extends DataFix {
    private static final @Nullable String[] DAMAGE_TO_ENTITY_IDS = (String[])DataFixUtils.make((Object)new String[256], ids -> {
        ids[1] = "Item";
        ids[2] = "XPOrb";
        ids[7] = "ThrownEgg";
        ids[8] = "LeashKnot";
        ids[9] = "Painting";
        ids[10] = "Arrow";
        ids[11] = "Snowball";
        ids[12] = "Fireball";
        ids[13] = "SmallFireball";
        ids[14] = "ThrownEnderpearl";
        ids[15] = "EyeOfEnderSignal";
        ids[16] = "ThrownPotion";
        ids[17] = "ThrownExpBottle";
        ids[18] = "ItemFrame";
        ids[19] = "WitherSkull";
        ids[20] = "PrimedTnt";
        ids[21] = "FallingSand";
        ids[22] = "FireworksRocketEntity";
        ids[23] = "TippedArrow";
        ids[24] = "SpectralArrow";
        ids[25] = "ShulkerBullet";
        ids[26] = "DragonFireball";
        ids[30] = "ArmorStand";
        ids[41] = "Boat";
        ids[42] = "MinecartRideable";
        ids[43] = "MinecartChest";
        ids[44] = "MinecartFurnace";
        ids[45] = "MinecartTNT";
        ids[46] = "MinecartHopper";
        ids[47] = "MinecartSpawner";
        ids[40] = "MinecartCommandBlock";
        ids[50] = "Creeper";
        ids[51] = "Skeleton";
        ids[52] = "Spider";
        ids[53] = "Giant";
        ids[54] = "Zombie";
        ids[55] = "Slime";
        ids[56] = "Ghast";
        ids[57] = "PigZombie";
        ids[58] = "Enderman";
        ids[59] = "CaveSpider";
        ids[60] = "Silverfish";
        ids[61] = "Blaze";
        ids[62] = "LavaSlime";
        ids[63] = "EnderDragon";
        ids[64] = "WitherBoss";
        ids[65] = "Bat";
        ids[66] = "Witch";
        ids[67] = "Endermite";
        ids[68] = "Guardian";
        ids[69] = "Shulker";
        ids[90] = "Pig";
        ids[91] = "Sheep";
        ids[92] = "Cow";
        ids[93] = "Chicken";
        ids[94] = "Squid";
        ids[95] = "Wolf";
        ids[96] = "MushroomCow";
        ids[97] = "SnowMan";
        ids[98] = "Ozelot";
        ids[99] = "VillagerGolem";
        ids[100] = "EntityHorse";
        ids[101] = "Rabbit";
        ids[120] = "Villager";
        ids[200] = "EnderCrystal";
    });

    public ItemSpawnEggFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type type = schema.getType(TypeReferences.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"id", (Type)DSL.string());
        OpticFinder opticFinder3 = type.findField("tag");
        OpticFinder opticFinder4 = opticFinder3.type().findField("EntityTag");
        OpticFinder opticFinder5 = DSL.typeFinder((Type)schema.getTypeRaw(TypeReferences.ENTITY));
        return this.fixTypeEverywhereTyped("ItemSpawnEggFix", type, typed2 -> {
            Optional optional = typed2.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:spawn_egg")) {
                Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
                short s = dynamic.get("Damage").asShort((short)0);
                Optional optional2 = typed2.getOptionalTyped(opticFinder3);
                Optional optional3 = optional2.flatMap(tagTyped -> tagTyped.getOptionalTyped(opticFinder4));
                Optional optional4 = optional3.flatMap(entityTagTyped -> entityTagTyped.getOptionalTyped(opticFinder5));
                Optional optional5 = optional4.flatMap(entityTyped -> entityTyped.getOptional(opticFinder2));
                Typed typed22 = typed2;
                String string = DAMAGE_TO_ENTITY_IDS[s & 0xFF];
                if (string != null && (optional5.isEmpty() || !Objects.equals(optional5.get(), string))) {
                    Typed typed3 = typed2.getOrCreateTyped(opticFinder3);
                    Dynamic dynamic2 = (Dynamic)DataFixUtils.orElse(typed3.getOptionalTyped(opticFinder4).map(typed -> (Dynamic)typed.write().getOrThrow()), (Object)dynamic.emptyMap());
                    dynamic2 = dynamic2.set("id", dynamic2.createString(string));
                    typed22 = typed22.set(opticFinder3, FixUtil.method_67590(typed3, opticFinder4, dynamic2));
                }
                if (s != 0) {
                    dynamic = dynamic.set("Damage", dynamic.createShort((short)0));
                    typed22 = typed22.set(DSL.remainderFinder(), (Object)dynamic);
                }
                return typed22;
            }
            return typed2;
        });
    }
}

