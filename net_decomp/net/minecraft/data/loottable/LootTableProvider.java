/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.data.loottable;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextType;
import net.minecraft.util.math.random.RandomSequence;
import org.slf4j.Logger;

public class LootTableProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataOutput.PathResolver pathResolver;
    private final Set<RegistryKey<LootTable>> lootTableIds;
    private final List<LootTypeGenerator> lootTypeGenerators;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;

    public LootTableProvider(DataOutput output, Set<RegistryKey<LootTable>> lootTableIds, List<LootTypeGenerator> lootTypeGenerators, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        this.pathResolver = output.getResolver(RegistryKeys.LOOT_TABLE);
        this.lootTypeGenerators = lootTypeGenerators;
        this.lootTableIds = lootTableIds;
        this.registriesFuture = registriesFuture;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registriesFuture.thenCompose(registries -> this.run(writer, (RegistryWrapper.WrapperLookup)registries));
    }

    private CompletableFuture<?> run(DataWriter writer, RegistryWrapper.WrapperLookup registries) {
        SimpleRegistry<LootTable> mutableRegistry = new SimpleRegistry<LootTable>(RegistryKeys.LOOT_TABLE, Lifecycle.experimental());
        Object2ObjectOpenHashMap map = new Object2ObjectOpenHashMap();
        this.lootTypeGenerators.forEach(arg_0 -> LootTableProvider.method_10410(registries, (Map)map, mutableRegistry, arg_0));
        mutableRegistry.freeze();
        ErrorReporter.Impl impl = new ErrorReporter.Impl();
        DynamicRegistryManager.Immutable registryLookup = new DynamicRegistryManager.ImmutableImpl(List.of(mutableRegistry)).toImmutable();
        LootTableReporter lootTableReporter = new LootTableReporter(impl, LootContextTypes.GENERIC, registryLookup);
        Sets.SetView set = Sets.difference(this.lootTableIds, mutableRegistry.getKeys());
        for (RegistryKey registryKey : set) {
            impl.report(new MissingTableError(registryKey));
        }
        mutableRegistry.streamEntries().forEach(entry -> ((LootTable)entry.value()).validate(lootTableReporter.withContextType(((LootTable)entry.value()).getType()).makeChild(new ErrorReporter.LootTableContext(entry.registryKey()), entry.registryKey())));
        if (!impl.isEmpty()) {
            impl.apply((name, error) -> LOGGER.warn("Found validation problem in {}: {}", name, (Object)error.getMessage()));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
        return CompletableFuture.allOf((CompletableFuture[])mutableRegistry.getEntrySet().stream().map(entry -> {
            RegistryKey registryKey = (RegistryKey)entry.getKey();
            LootTable lootTable = (LootTable)entry.getValue();
            Path path = this.pathResolver.resolveJson(registryKey.getValue());
            return DataProvider.writeCodecToPath(writer, registries, LootTable.CODEC, lootTable, path);
        }).toArray(CompletableFuture[]::new));
    }

    private static Identifier getId(RegistryKey<LootTable> lootTableKey) {
        return lootTableKey.getValue();
    }

    @Override
    public final String getName() {
        return "Loot Tables";
    }

    private static /* synthetic */ void method_10410(RegistryWrapper.WrapperLookup wrapperLookup, Map map, MutableRegistry mutableRegistry, LootTypeGenerator lootTypeGenerator) {
        lootTypeGenerator.provider().apply(wrapperLookup).accept((lootTable, builder) -> {
            Identifier identifier = LootTableProvider.getId(lootTable);
            Identifier identifier2 = map.put(RandomSequence.createSeed(identifier), identifier);
            if (identifier2 != null) {
                Util.logErrorOrPause("Loot table random sequence seed collision on " + String.valueOf(identifier2) + " and " + String.valueOf(lootTable.getValue()));
            }
            builder.randomSequenceId(identifier);
            LootTable lootTable2 = builder.type(lootTypeGenerator.paramSet).build();
            mutableRegistry.add(lootTable, lootTable2, RegistryEntryInfo.DEFAULT);
        });
    }

    public record MissingTableError(RegistryKey<LootTable> id) implements ErrorReporter.Error
    {
        @Override
        public String getMessage() {
            return "Missing built-in table: " + String.valueOf(this.id.getValue());
        }
    }

    public static final class LootTypeGenerator
    extends Record {
        private final Function<RegistryWrapper.WrapperLookup, LootTableGenerator> provider;
        final ContextType paramSet;

        public LootTypeGenerator(Function<RegistryWrapper.WrapperLookup, LootTableGenerator> provider, ContextType paramSet) {
            this.provider = provider;
            this.paramSet = paramSet;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LootTypeGenerator.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LootTypeGenerator.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LootTypeGenerator.class, "provider;paramSet", "provider", "paramSet"}, this, object);
        }

        public Function<RegistryWrapper.WrapperLookup, LootTableGenerator> provider() {
            return this.provider;
        }

        public ContextType paramSet() {
            return this.paramSet;
        }
    }
}

