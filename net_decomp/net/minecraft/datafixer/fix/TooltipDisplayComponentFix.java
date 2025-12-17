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
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
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
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;

public class TooltipDisplayComponentFix
extends DataFix {
    private static final List<String> HIDE_ADDITIONAL_TOOLTIP_COMPONENTS = List.of("minecraft:banner_patterns", "minecraft:bees", "minecraft:block_entity_data", "minecraft:block_state", "minecraft:bundle_contents", "minecraft:charged_projectiles", "minecraft:container", "minecraft:container_loot", "minecraft:firework_explosion", "minecraft:fireworks", "minecraft:instrument", "minecraft:map_id", "minecraft:painting/variant", "minecraft:pot_decorations", "minecraft:potion_contents", "minecraft:tropical_fish/pattern", "minecraft:written_book_content");

    public TooltipDisplayComponentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.DATA_COMPONENTS);
        Type type2 = this.getOutputSchema().getType(TypeReferences.DATA_COMPONENTS);
        OpticFinder opticFinder = type.findField("minecraft:can_place_on");
        OpticFinder opticFinder2 = type.findField("minecraft:can_break");
        Type type3 = type2.findFieldType("minecraft:can_place_on");
        Type type4 = type2.findFieldType("minecraft:can_break");
        return this.fixTypeEverywhereTyped("TooltipDisplayComponentFix", type, type2, typed -> TooltipDisplayComponentFix.fix(typed, opticFinder, opticFinder2, type3, type4));
    }

    private static Typed<?> fix(Typed<?> typed, OpticFinder<?> canPlaceOnOpticFinder, OpticFinder<?> canBreakOpticFinder, Type<?> canPlaceOnType, Type<?> canBreakType) {
        HashSet<String> set = new HashSet<String>();
        typed = TooltipDisplayComponentFix.fixAdventureModePredicate(typed, canPlaceOnOpticFinder, canPlaceOnType, "minecraft:can_place_on", set);
        typed = TooltipDisplayComponentFix.fixAdventureModePredicate(typed, canBreakOpticFinder, canBreakType, "minecraft:can_break", set);
        return typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = TooltipDisplayComponentFix.fixComponent(dynamic, "minecraft:trim", set);
            dynamic = TooltipDisplayComponentFix.fixComponent(dynamic, "minecraft:unbreakable", set);
            dynamic = TooltipDisplayComponentFix.fixAndInlineComponent(dynamic, "minecraft:dyed_color", "rgb", set);
            dynamic = TooltipDisplayComponentFix.fixAndInlineComponent(dynamic, "minecraft:attribute_modifiers", "modifiers", set);
            dynamic = TooltipDisplayComponentFix.fixAndInlineComponent(dynamic, "minecraft:enchantments", "levels", set);
            dynamic = TooltipDisplayComponentFix.fixAndInlineComponent(dynamic, "minecraft:stored_enchantments", "levels", set);
            dynamic = TooltipDisplayComponentFix.fixAndInlineComponent(dynamic, "minecraft:jukebox_playable", "song", set);
            boolean bl = dynamic.get("minecraft:hide_tooltip").result().isPresent();
            dynamic = dynamic.remove("minecraft:hide_tooltip");
            boolean bl2 = dynamic.get("minecraft:hide_additional_tooltip").result().isPresent();
            dynamic = dynamic.remove("minecraft:hide_additional_tooltip");
            if (bl2) {
                for (String string : HIDE_ADDITIONAL_TOOLTIP_COMPONENTS) {
                    if (!dynamic.get(string).result().isPresent()) continue;
                    set.add(string);
                }
            }
            if (set.isEmpty() && !bl) {
                return dynamic;
            }
            return dynamic.set("minecraft:tooltip_display", dynamic.createMap(Map.of(dynamic.createString("hide_tooltip"), dynamic.createBoolean(bl), dynamic.createString("hidden_components"), dynamic.createList(set.stream().map(arg_0 -> ((Dynamic)dynamic).createString(arg_0))))));
        });
    }

    private static Dynamic<?> fixComponent(Dynamic<?> dynamic, String id, Set<String> toHide) {
        return TooltipDisplayComponentFix.fixComponent(dynamic, id, toHide, UnaryOperator.identity());
    }

    private static Dynamic<?> fixAndInlineComponent(Dynamic<?> dynamic, String id, String toInline, Set<String> toHide) {
        return TooltipDisplayComponentFix.fixComponent(dynamic, id, toHide, dynamicx -> (Dynamic)DataFixUtils.orElse((Optional)dynamicx.get(toInline).result(), (Object)dynamicx));
    }

    private static Dynamic<?> fixComponent(Dynamic<?> dynamic, String id, Set<String> toHide, UnaryOperator<Dynamic<?>> fixer) {
        return dynamic.update(id, dynamicx -> {
            boolean bl = dynamicx.get("show_in_tooltip").asBoolean(true);
            if (!bl) {
                toHide.add(id);
            }
            return (Dynamic)fixer.apply(dynamicx.remove("show_in_tooltip"));
        });
    }

    private static Typed<?> fixAdventureModePredicate(Typed<?> typed, OpticFinder<?> opticFinder, Type<?> type, String id, Set<String> toHide) {
        return typed.updateTyped(opticFinder, type, typedx -> Util.apply(typedx, type, dynamic -> {
            OptionalDynamic optionalDynamic = dynamic.get("predicates");
            if (optionalDynamic.result().isEmpty()) {
                return dynamic;
            }
            boolean bl = dynamic.get("show_in_tooltip").asBoolean(true);
            if (!bl) {
                toHide.add(id);
            }
            return (Dynamic)optionalDynamic.result().get();
        }));
    }
}

