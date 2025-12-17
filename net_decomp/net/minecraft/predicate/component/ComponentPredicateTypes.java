/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.predicate.component;

import com.mojang.serialization.Codec;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.predicate.component.CustomDataPredicate;
import net.minecraft.predicate.item.AttributeModifiersPredicate;
import net.minecraft.predicate.item.BundleContentsPredicate;
import net.minecraft.predicate.item.ContainerPredicate;
import net.minecraft.predicate.item.DamagePredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.predicate.item.FireworkExplosionPredicate;
import net.minecraft.predicate.item.FireworksPredicate;
import net.minecraft.predicate.item.JukeboxPlayablePredicate;
import net.minecraft.predicate.item.PotionContentsPredicate;
import net.minecraft.predicate.item.TrimPredicate;
import net.minecraft.predicate.item.WritableBookContentPredicate;
import net.minecraft.predicate.item.WrittenBookContentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ComponentPredicateTypes {
    public static final ComponentPredicate.Type<DamagePredicate> DAMAGE = ComponentPredicateTypes.register("damage", DamagePredicate.CODEC);
    public static final ComponentPredicate.Type<EnchantmentsPredicate.Enchantments> ENCHANTMENTS = ComponentPredicateTypes.register("enchantments", EnchantmentsPredicate.Enchantments.CODEC);
    public static final ComponentPredicate.Type<EnchantmentsPredicate.StoredEnchantments> STORED_ENCHANTMENTS = ComponentPredicateTypes.register("stored_enchantments", EnchantmentsPredicate.StoredEnchantments.CODEC);
    public static final ComponentPredicate.Type<PotionContentsPredicate> POTION_CONTENTS = ComponentPredicateTypes.register("potion_contents", PotionContentsPredicate.CODEC);
    public static final ComponentPredicate.Type<CustomDataPredicate> CUSTOM_DATA = ComponentPredicateTypes.register("custom_data", CustomDataPredicate.CODEC);
    public static final ComponentPredicate.Type<ContainerPredicate> CONTAINER = ComponentPredicateTypes.register("container", ContainerPredicate.CODEC);
    public static final ComponentPredicate.Type<BundleContentsPredicate> BUNDLE_CONTENTS = ComponentPredicateTypes.register("bundle_contents", BundleContentsPredicate.CODEC);
    public static final ComponentPredicate.Type<FireworkExplosionPredicate> FIREWORK_EXPLOSION = ComponentPredicateTypes.register("firework_explosion", FireworkExplosionPredicate.CODEC);
    public static final ComponentPredicate.Type<FireworksPredicate> FIREWORKS = ComponentPredicateTypes.register("fireworks", FireworksPredicate.CODEC);
    public static final ComponentPredicate.Type<WritableBookContentPredicate> WRITABLE_BOOK_CONTENT = ComponentPredicateTypes.register("writable_book_content", WritableBookContentPredicate.CODEC);
    public static final ComponentPredicate.Type<WrittenBookContentPredicate> WRITTEN_BOOK_CONTENT = ComponentPredicateTypes.register("written_book_content", WrittenBookContentPredicate.CODEC);
    public static final ComponentPredicate.Type<AttributeModifiersPredicate> ATTRIBUTE_MODIFIERS = ComponentPredicateTypes.register("attribute_modifiers", AttributeModifiersPredicate.CODEC);
    public static final ComponentPredicate.Type<TrimPredicate> TRIM = ComponentPredicateTypes.register("trim", TrimPredicate.CODEC);
    public static final ComponentPredicate.Type<JukeboxPlayablePredicate> JUKEBOX_PLAYABLE = ComponentPredicateTypes.register("jukebox_playable", JukeboxPlayablePredicate.CODEC);

    private static <T extends ComponentPredicate> ComponentPredicate.Type<T> register(String id, Codec<T> codec) {
        return Registry.register(Registries.DATA_COMPONENT_PREDICATE_TYPE, id, new ComponentPredicate.OfValue<T>(codec));
    }

    public static ComponentPredicate.Type<?> getDefault(Registry<ComponentPredicate.Type<?>> registry) {
        return DAMAGE;
    }
}

