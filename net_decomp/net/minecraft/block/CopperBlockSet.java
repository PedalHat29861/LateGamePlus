/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableBiMap
 *  com.google.common.collect.ImmutableList
 *  org.apache.commons.lang3.function.TriFunction
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import org.apache.commons.lang3.function.TriFunction;

public record CopperBlockSet(Block unaffected, Block exposed, Block weathered, Block oxidized, Block waxed, Block waxedExposed, Block waxedWeathered, Block waxedOxidized) {
    public static <WaxedBlock extends Block, WeatheringBlock extends Block> CopperBlockSet create(String baseId, TriFunction<String, Function<AbstractBlock.Settings, Block>, AbstractBlock.Settings, Block> registerFunction, Function<AbstractBlock.Settings, WaxedBlock> waxedBlockFactory, BiFunction<Oxidizable.OxidationLevel, AbstractBlock.Settings, WeatheringBlock> unwaxedBlockFactory, Function<Oxidizable.OxidationLevel, AbstractBlock.Settings> settingsFromOxidationLevel) {
        return new CopperBlockSet((Block)registerFunction.apply((Object)baseId, settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.UNAFFECTED, (AbstractBlock.Settings)settings), (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.UNAFFECTED)), (Block)registerFunction.apply((Object)("exposed_" + baseId), settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.EXPOSED, (AbstractBlock.Settings)settings), (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.EXPOSED)), (Block)registerFunction.apply((Object)("weathered_" + baseId), settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.WEATHERED, (AbstractBlock.Settings)settings), (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.WEATHERED)), (Block)registerFunction.apply((Object)("oxidized_" + baseId), settings -> (Block)unwaxedBlockFactory.apply(Oxidizable.OxidationLevel.OXIDIZED, (AbstractBlock.Settings)settings), (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.OXIDIZED)), (Block)registerFunction.apply((Object)("waxed_" + baseId), waxedBlockFactory::apply, (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.UNAFFECTED)), (Block)registerFunction.apply((Object)("waxed_exposed_" + baseId), waxedBlockFactory::apply, (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.EXPOSED)), (Block)registerFunction.apply((Object)("waxed_weathered_" + baseId), waxedBlockFactory::apply, (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.WEATHERED)), (Block)registerFunction.apply((Object)("waxed_oxidized_" + baseId), waxedBlockFactory::apply, (Object)settingsFromOxidationLevel.apply(Oxidizable.OxidationLevel.OXIDIZED)));
    }

    public ImmutableBiMap<Block, Block> getOxidizingMap() {
        return ImmutableBiMap.of((Object)this.unaffected, (Object)this.exposed, (Object)this.exposed, (Object)this.weathered, (Object)this.weathered, (Object)this.oxidized);
    }

    public ImmutableBiMap<Block, Block> getWaxingMap() {
        return ImmutableBiMap.of((Object)this.unaffected, (Object)this.waxed, (Object)this.exposed, (Object)this.waxedExposed, (Object)this.weathered, (Object)this.waxedWeathered, (Object)this.oxidized, (Object)this.waxedOxidized);
    }

    public ImmutableList<Block> getAll() {
        return ImmutableList.of((Object)this.unaffected, (Object)this.waxed, (Object)this.exposed, (Object)this.waxedExposed, (Object)this.weathered, (Object)this.waxedWeathered, (Object)this.oxidized, (Object)this.waxedOxidized);
    }

    public void forEach(Consumer<Block> consumer) {
        consumer.accept(this.unaffected);
        consumer.accept(this.exposed);
        consumer.accept(this.weathered);
        consumer.accept(this.oxidized);
        consumer.accept(this.waxed);
        consumer.accept(this.waxedExposed);
        consumer.accept(this.waxedWeathered);
        consumer.accept(this.waxedOxidized);
    }
}

