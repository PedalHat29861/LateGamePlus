/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.util.Util;

public class StatusEffectFix
extends DataFix {
    private static final Int2ObjectMap<String> OLD_TO_NEW_IDS = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), idMap -> {
        idMap.put(1, (Object)"minecraft:speed");
        idMap.put(2, (Object)"minecraft:slowness");
        idMap.put(3, (Object)"minecraft:haste");
        idMap.put(4, (Object)"minecraft:mining_fatigue");
        idMap.put(5, (Object)"minecraft:strength");
        idMap.put(6, (Object)"minecraft:instant_health");
        idMap.put(7, (Object)"minecraft:instant_damage");
        idMap.put(8, (Object)"minecraft:jump_boost");
        idMap.put(9, (Object)"minecraft:nausea");
        idMap.put(10, (Object)"minecraft:regeneration");
        idMap.put(11, (Object)"minecraft:resistance");
        idMap.put(12, (Object)"minecraft:fire_resistance");
        idMap.put(13, (Object)"minecraft:water_breathing");
        idMap.put(14, (Object)"minecraft:invisibility");
        idMap.put(15, (Object)"minecraft:blindness");
        idMap.put(16, (Object)"minecraft:night_vision");
        idMap.put(17, (Object)"minecraft:hunger");
        idMap.put(18, (Object)"minecraft:weakness");
        idMap.put(19, (Object)"minecraft:poison");
        idMap.put(20, (Object)"minecraft:wither");
        idMap.put(21, (Object)"minecraft:health_boost");
        idMap.put(22, (Object)"minecraft:absorption");
        idMap.put(23, (Object)"minecraft:saturation");
        idMap.put(24, (Object)"minecraft:glowing");
        idMap.put(25, (Object)"minecraft:levitation");
        idMap.put(26, (Object)"minecraft:luck");
        idMap.put(27, (Object)"minecraft:unluck");
        idMap.put(28, (Object)"minecraft:slow_falling");
        idMap.put(29, (Object)"minecraft:conduit_power");
        idMap.put(30, (Object)"minecraft:dolphins_grace");
        idMap.put(31, (Object)"minecraft:bad_omen");
        idMap.put(32, (Object)"minecraft:hero_of_the_village");
        idMap.put(33, (Object)"minecraft:darkness");
    });
    private static final Set<String> POTION_ITEM_IDS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public StatusEffectFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    private static <T> Optional<Dynamic<T>> updateId(Dynamic<T> dynamic, String idKey) {
        return dynamic.get(idKey).asNumber().result().map(oldId -> (String)OLD_TO_NEW_IDS.get(oldId.intValue())).map(arg_0 -> dynamic.createString(arg_0));
    }

    private static <T> Dynamic<T> renameKeyAndUpdateId(Dynamic<T> dynamic, String oldKey, Dynamic<T> dynamic2, String newKey) {
        Optional<Dynamic<T>> optional = StatusEffectFix.updateId(dynamic, oldKey);
        return dynamic2.replaceField(oldKey, newKey, optional);
    }

    private static <T> Dynamic<T> renameKeyAndUpdateId(Dynamic<T> dynamic, String oldKey, String newKey) {
        return StatusEffectFix.renameKeyAndUpdateId(dynamic, oldKey, dynamic, newKey);
    }

    private static <T> Dynamic<T> fixEffect(Dynamic<T> effectDynamic) {
        effectDynamic = StatusEffectFix.renameKeyAndUpdateId(effectDynamic, "Id", "id");
        effectDynamic = effectDynamic.renameField("Ambient", "ambient");
        effectDynamic = effectDynamic.renameField("Amplifier", "amplifier");
        effectDynamic = effectDynamic.renameField("Duration", "duration");
        effectDynamic = effectDynamic.renameField("ShowParticles", "show_particles");
        effectDynamic = effectDynamic.renameField("ShowIcon", "show_icon");
        Optional<Dynamic> optional = effectDynamic.get("HiddenEffect").result().map(StatusEffectFix::fixEffect);
        return effectDynamic.replaceField("HiddenEffect", "hidden_effect", optional);
    }

    private static <T> Dynamic<T> fixEffectList(Dynamic<T> dynamic, String oldEffectListKey, String newEffectListKey) {
        Optional<Dynamic> optional = dynamic.get(oldEffectListKey).asStreamOpt().result().map(oldEffects -> dynamic.createList(oldEffects.map(StatusEffectFix::fixEffect)));
        return dynamic.replaceField(oldEffectListKey, newEffectListKey, optional);
    }

    private static <T> Dynamic<T> fixSuspiciousStewEffect(Dynamic<T> effectDynamicIn, Dynamic<T> effectDynamicOut) {
        effectDynamicOut = StatusEffectFix.renameKeyAndUpdateId(effectDynamicIn, "EffectId", effectDynamicOut, "id");
        Optional optional = effectDynamicIn.get("EffectDuration").result();
        return effectDynamicOut.replaceField("EffectDuration", "duration", optional);
    }

    private static <T> Dynamic<T> fixSuspiciousStewEffect(Dynamic<T> effectDynamic) {
        return StatusEffectFix.fixSuspiciousStewEffect(effectDynamic, effectDynamic);
    }

    private Typed<?> fixEntityEffects(Typed<?> entityTyped, DSL.TypeReference entityTypeReference, String entityId, Function<Dynamic<?>, Dynamic<?>> effectsFixer) {
        Type type = this.getInputSchema().getChoiceType(entityTypeReference, entityId);
        Type type2 = this.getOutputSchema().getChoiceType(entityTypeReference, entityId);
        return entityTyped.updateTyped(DSL.namedChoice((String)entityId, (Type)type), type2, matchingEntityTyped -> matchingEntityTyped.update(DSL.remainderFinder(), effectsFixer));
    }

    private TypeRewriteRule makeBlockEntitiesRule() {
        Type type = this.getInputSchema().getType(TypeReferences.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("BlockEntityMobEffectIdFix", type, typed -> {
            typed = this.fixEntityEffects((Typed<?>)typed, TypeReferences.BLOCK_ENTITY, "minecraft:beacon", dynamic -> {
                dynamic = StatusEffectFix.renameKeyAndUpdateId(dynamic, "Primary", "primary_effect");
                return StatusEffectFix.renameKeyAndUpdateId(dynamic, "Secondary", "secondary_effect");
            });
            return typed;
        });
    }

    private static <T> Dynamic<T> fixStewEffectsKey(Dynamic<T> dynamic) {
        Dynamic dynamic2 = dynamic.emptyMap();
        Dynamic<T> dynamic3 = StatusEffectFix.fixSuspiciousStewEffect(dynamic, dynamic2);
        if (!dynamic3.equals((Object)dynamic2)) {
            dynamic = dynamic.set("stew_effects", dynamic.createList(Stream.of(dynamic3)));
        }
        return dynamic.remove("EffectId").remove("EffectDuration");
    }

    private static <T> Dynamic<T> fixCustomPotionEffectsKey(Dynamic<T> dynamic) {
        return StatusEffectFix.fixEffectList(dynamic, "CustomPotionEffects", "custom_potion_effects");
    }

    private static <T> Dynamic<T> fixEffectsKey(Dynamic<T> dynamic) {
        return StatusEffectFix.fixEffectList(dynamic, "Effects", "effects");
    }

    private static Dynamic<?> fixActiveEffectsKey(Dynamic<?> dynamic) {
        return StatusEffectFix.fixEffectList(dynamic, "ActiveEffects", "active_effects");
    }

    private TypeRewriteRule makeEntitiesRule() {
        Type type = this.getInputSchema().getType(TypeReferences.ENTITY);
        return this.fixTypeEverywhereTyped("EntityMobEffectIdFix", type, entityTyped -> {
            entityTyped = this.fixEntityEffects((Typed<?>)entityTyped, TypeReferences.ENTITY, "minecraft:mooshroom", StatusEffectFix::fixStewEffectsKey);
            entityTyped = this.fixEntityEffects((Typed<?>)entityTyped, TypeReferences.ENTITY, "minecraft:arrow", StatusEffectFix::fixCustomPotionEffectsKey);
            entityTyped = this.fixEntityEffects((Typed<?>)entityTyped, TypeReferences.ENTITY, "minecraft:area_effect_cloud", StatusEffectFix::fixEffectsKey);
            entityTyped = entityTyped.update(DSL.remainderFinder(), StatusEffectFix::fixActiveEffectsKey);
            return entityTyped;
        });
    }

    private TypeRewriteRule makePlayersRule() {
        Type type = this.getInputSchema().getType(TypeReferences.PLAYER);
        return this.fixTypeEverywhereTyped("PlayerMobEffectIdFix", type, typed -> typed.update(DSL.remainderFinder(), StatusEffectFix::fixActiveEffectsKey));
    }

    private static <T> Dynamic<T> fixSuspiciousStewEffects(Dynamic<T> tagTyped) {
        Optional<Dynamic> optional = tagTyped.get("Effects").asStreamOpt().result().map(effects -> tagTyped.createList(effects.map(StatusEffectFix::fixSuspiciousStewEffect)));
        return tagTyped.replaceField("Effects", "effects", optional);
    }

    private TypeRewriteRule makeItemStacksRule() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder opticFinder2 = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemStackMobEffectIdFix", type, itemStackTyped -> {
            Optional optional = itemStackTyped.getOptional(opticFinder);
            if (optional.isPresent()) {
                String string = (String)((Pair)optional.get()).getSecond();
                if (string.equals("minecraft:suspicious_stew")) {
                    return itemStackTyped.updateTyped(opticFinder2, tagTyped -> tagTyped.update(DSL.remainderFinder(), StatusEffectFix::fixSuspiciousStewEffects));
                }
                if (POTION_ITEM_IDS.contains(string)) {
                    return itemStackTyped.updateTyped(opticFinder2, tagTyped -> tagTyped.update(DSL.remainderFinder(), tagDynamic -> StatusEffectFix.fixEffectList(tagDynamic, "CustomPotionEffects", "custom_potion_effects")));
                }
            }
            return itemStackTyped;
        });
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.makeBlockEntitiesRule(), (TypeRewriteRule[])new TypeRewriteRule[]{this.makeEntitiesRule(), this.makePlayersRule(), this.makeItemStacksRule()});
    }
}

