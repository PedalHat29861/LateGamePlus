/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.context.ContextParameter;

public record BlockStatePropertyLootCondition(RegistryEntry<Block> block, Optional<StatePredicate> properties) implements LootCondition
{
    public static final MapCodec<BlockStatePropertyLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Registries.BLOCK.getEntryCodec().fieldOf("block").forGetter(BlockStatePropertyLootCondition::block), (App)StatePredicate.CODEC.optionalFieldOf("properties").forGetter(BlockStatePropertyLootCondition::properties)).apply((Applicative)instance, BlockStatePropertyLootCondition::new)).validate(BlockStatePropertyLootCondition::validateHasProperties);

    private static DataResult<BlockStatePropertyLootCondition> validateHasProperties(BlockStatePropertyLootCondition condition) {
        return condition.properties().flatMap(predicate -> predicate.findMissing(condition.block().value().getStateManager())).map(property -> DataResult.error(() -> "Block " + String.valueOf(condition.block()) + " has no property" + property)).orElse(DataResult.success((Object)condition));
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.BLOCK_STATE_PROPERTY;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext lootContext) {
        BlockState blockState = lootContext.get(LootContextParameters.BLOCK_STATE);
        return blockState != null && blockState.isOf(this.block) && (this.properties.isEmpty() || this.properties.get().test(blockState));
    }

    public static Builder builder(Block block) {
        return new Builder(block);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private final RegistryEntry<Block> block;
        private Optional<StatePredicate> propertyValues = Optional.empty();

        public Builder(Block block) {
            this.block = block.getRegistryEntry();
        }

        public Builder properties(StatePredicate.Builder builder) {
            this.propertyValues = builder.build();
            return this;
        }

        @Override
        public LootCondition build() {
            return new BlockStatePropertyLootCondition(this.block, this.propertyValues);
        }
    }
}

