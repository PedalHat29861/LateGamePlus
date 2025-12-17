/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.data.loottable;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.AnyOfLootCondition;
import net.minecraft.loot.condition.DamageSourcePropertiesLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.predicate.component.ComponentPredicateTypes;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.SheepPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.DyeColor;

public abstract class EntityLootTableGenerator
implements LootTableGenerator {
    protected final RegistryWrapper.WrapperLookup registries;
    private final FeatureSet requiredFeatures;
    private final FeatureSet featureSet;
    private final Map<EntityType<?>, Map<RegistryKey<LootTable>, LootTable.Builder>> lootTables = Maps.newHashMap();

    protected final AnyOfLootCondition.Builder createSmeltLootCondition() {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return AnyOfLootCondition.builder(EntityPropertiesLootCondition.builder(LootContext.EntityReference.THIS, EntityPredicate.Builder.create().flags(EntityFlagsPredicate.Builder.create().onFire(true))), EntityPropertiesLootCondition.builder(LootContext.EntityReference.DIRECT_ATTACKER, EntityPredicate.Builder.create().equipment(EntityEquipmentPredicate.Builder.create().mainhand(ItemPredicate.Builder.create().components(ComponentsPredicate.Builder.create().partial(ComponentPredicateTypes.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(impl.getOrThrow(EnchantmentTags.SMELTS_LOOT), NumberRange.IntRange.ANY)))).build())))));
    }

    protected EntityLootTableGenerator(FeatureSet requiredFeatures, RegistryWrapper.WrapperLookup registries) {
        this(requiredFeatures, requiredFeatures, registries);
    }

    protected EntityLootTableGenerator(FeatureSet requiredFeatures, FeatureSet featureSet, RegistryWrapper.WrapperLookup registries) {
        this.requiredFeatures = requiredFeatures;
        this.featureSet = featureSet;
        this.registries = registries;
    }

    public static LootPool.Builder createForSheep(Map<DyeColor, RegistryKey<LootTable>> colorLootTables) {
        AlternativeEntry.Builder builder = AlternativeEntry.builder(new LootPoolEntry.Builder[0]);
        for (Map.Entry<DyeColor, RegistryKey<LootTable>> entry : colorLootTables.entrySet()) {
            builder = builder.alternatively((LootPoolEntry.Builder<?>)LootTableEntry.builder(entry.getValue()).conditionally(EntityPropertiesLootCondition.builder(LootContext.EntityReference.THIS, EntityPredicate.Builder.create().components(ComponentsPredicate.Builder.create().exact(ComponentMapPredicate.of(DataComponentTypes.SHEEP_COLOR, entry.getKey())).build()).typeSpecific(SheepPredicate.unsheared()))));
        }
        return LootPool.builder().with(builder);
    }

    public abstract void generate();

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        this.generate();
        HashSet set = new HashSet();
        Registries.ENTITY_TYPE.streamEntries().forEach(entityType -> {
            EntityType entityType2 = (EntityType)entityType.value();
            if (!entityType2.isEnabled(this.requiredFeatures)) {
                return;
            }
            Optional<RegistryKey<LootTable>> optional = entityType2.getLootTableKey();
            if (optional.isPresent()) {
                Map<RegistryKey<LootTable>, LootTable.Builder> map = this.lootTables.remove(entityType2);
                if (entityType2.isEnabled(this.featureSet) && (map == null || !map.containsKey(optional.get()))) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", optional.get(), entityType.registryKey().getValue()));
                }
                if (map != null) {
                    map.forEach((tableKey, lootTableBuilder) -> {
                        if (!set.add(tableKey)) {
                            throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", tableKey, entityType.registryKey().getValue()));
                        }
                        lootTableBiConsumer.accept((RegistryKey<LootTable>)tableKey, (LootTable.Builder)lootTableBuilder);
                    });
                }
            } else {
                Map<RegistryKey<LootTable>, LootTable.Builder> map = this.lootTables.remove(entityType2);
                if (map != null) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot", map.keySet().stream().map(key -> key.getValue().toString()).collect(Collectors.joining(",")), entityType.registryKey().getValue()));
                }
            }
        });
        if (!this.lootTables.isEmpty()) {
            throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + String.valueOf(this.lootTables.keySet()));
        }
    }

    protected LootCondition.Builder killedByFrog(RegistryEntryLookup<EntityType<?>> entityTypeLookup) {
        return DamageSourcePropertiesLootCondition.builder(DamageSourcePredicate.Builder.create().sourceEntity(EntityPredicate.Builder.create().type(entityTypeLookup, EntityType.FROG)));
    }

    protected LootCondition.Builder killedByFrog(RegistryEntryLookup<EntityType<?>> entityTypeLookup, RegistryEntryLookup<FrogVariant> frogVariantLookup, RegistryKey<FrogVariant> frogVariant) {
        return DamageSourcePropertiesLootCondition.builder(DamageSourcePredicate.Builder.create().sourceEntity(EntityPredicate.Builder.create().type(entityTypeLookup, EntityType.FROG).components(ComponentsPredicate.Builder.create().exact(ComponentMapPredicate.of(DataComponentTypes.FROG_VARIANT, frogVariantLookup.getOrThrow(frogVariant))).build())));
    }

    protected void register(EntityType<?> entityType, LootTable.Builder lootTable) {
        this.register(entityType, entityType.getLootTableKey().orElseThrow(() -> new IllegalStateException("Entity " + String.valueOf(entityType) + " has no loot table")), lootTable);
    }

    protected void register(EntityType<?> entityType, RegistryKey<LootTable> tableKey, LootTable.Builder lootTable) {
        this.lootTables.computeIfAbsent(entityType, type -> new HashMap()).put(tableKey, lootTable);
    }
}

