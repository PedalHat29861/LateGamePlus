/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 */
package net.minecraft.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DynamicRegistriesProvider;
import net.minecraft.data.MetadataProvider;
import net.minecraft.data.SnbtProvider;
import net.minecraft.data.advancement.vanilla.VanillaAdvancementProviders;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.data.loottable.rebalance.TradeRebalanceLootTableProviders;
import net.minecraft.data.loottable.vanilla.VanillaLootTableProviders;
import net.minecraft.data.recipe.VanillaRecipeGenerator;
import net.minecraft.data.report.BiomeParametersProvider;
import net.minecraft.data.report.BlockListProvider;
import net.minecraft.data.report.CommandSyntaxProvider;
import net.minecraft.data.report.DataPackStructureProvider;
import net.minecraft.data.report.ItemListProvider;
import net.minecraft.data.report.PacketReportProvider;
import net.minecraft.data.report.RegistryDumpProvider;
import net.minecraft.data.tag.TagProvider;
import net.minecraft.data.tag.rebalance.TradeRebalanceEnchantmentTagProvider;
import net.minecraft.data.tag.vanilla.VanillaBannerPatternTagProvider;
import net.minecraft.data.tag.vanilla.VanillaBiomeTagProvider;
import net.minecraft.data.tag.vanilla.VanillaBlockTagProvider;
import net.minecraft.data.tag.vanilla.VanillaDamageTypeTagProvider;
import net.minecraft.data.tag.vanilla.VanillaDialogTagProvider;
import net.minecraft.data.tag.vanilla.VanillaEnchantmentTagProvider;
import net.minecraft.data.tag.vanilla.VanillaEntityTypeTagProvider;
import net.minecraft.data.tag.vanilla.VanillaFlatLevelGeneratorPresetTagProvider;
import net.minecraft.data.tag.vanilla.VanillaFluidTagProvider;
import net.minecraft.data.tag.vanilla.VanillaGameEventTagProvider;
import net.minecraft.data.tag.vanilla.VanillaInstrumentTagProvider;
import net.minecraft.data.tag.vanilla.VanillaItemTagProvider;
import net.minecraft.data.tag.vanilla.VanillaPaintingVariantTagProvider;
import net.minecraft.data.tag.vanilla.VanillaPointOfInterestTypeTagProvider;
import net.minecraft.data.tag.vanilla.VanillaStructureTagProvider;
import net.minecraft.data.tag.vanilla.VanillaTimelineTagProvider;
import net.minecraft.data.tag.vanilla.VanillaWorldPresetTagProvider;
import net.minecraft.data.validate.StructureValidatorProvider;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.TradeRebalanceBuiltinRegistries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.dedicated.management.schema.RpcSchemaReferenceJsonProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.SuppressLinter;

public class Main {
    @SuppressLinter(reason="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] args) throws IOException {
        SharedConstants.createGameVersion();
        OptionParser optionParser = new OptionParser();
        AbstractOptionSpec optionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder optionSpec2 = optionParser.accepts("server", "Include server generators");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("dev", "Include development tools");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("reports", "Include data reports");
        optionParser.accepts("validate", "Validate inputs");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec optionSpec6 = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo((Object)"generated", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec optionSpec7 = optionParser.accepts("input", "Input folder").withRequiredArg();
        OptionSet optionSet = optionParser.parse(args);
        if (optionSet.has((OptionSpec)optionSpec) || !optionSet.hasOptions()) {
            optionParser.printHelpOn((OutputStream)System.out);
            return;
        }
        Path path = Paths.get((String)optionSpec6.value(optionSet), new String[0]);
        boolean bl = optionSet.has((OptionSpec)optionSpec5);
        boolean bl2 = bl || optionSet.has((OptionSpec)optionSpec2);
        boolean bl3 = bl || optionSet.has((OptionSpec)optionSpec3);
        boolean bl4 = bl || optionSet.has((OptionSpec)optionSpec4);
        List<Path> collection = optionSet.valuesOf((OptionSpec)optionSpec7).stream().map(input -> Paths.get(input, new String[0])).toList();
        DataGenerator dataGenerator = new DataGenerator(path, SharedConstants.getGameVersion(), true);
        Main.create(dataGenerator, collection, bl2, bl3, bl4);
        dataGenerator.run();
        Util.shutdownExecutors();
    }

    private static <T extends DataProvider> DataProvider.Factory<T> toFactory(BiFunction<DataOutput, CompletableFuture<RegistryWrapper.WrapperLookup>, T> baseFactory, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        return output -> (DataProvider)baseFactory.apply(output, registriesFuture);
    }

    public static void create(DataGenerator dataGenerator, Collection<Path> inputs, boolean includeClient, boolean includeServer, boolean includeDev) {
        DataGenerator.Pack pack = dataGenerator.createVanillaPack(includeClient);
        pack.addProvider(output -> new SnbtProvider(output, inputs).addWriter(new StructureValidatorProvider()));
        CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture = CompletableFuture.supplyAsync(BuiltinRegistries::createWrapperLookup, Util.getMainWorkerExecutor());
        DataGenerator.Pack pack2 = dataGenerator.createVanillaPack(includeClient);
        pack2.addProvider(Main.toFactory(DynamicRegistriesProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaAdvancementProviders::createVanillaProvider, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaLootTableProviders::createVanillaProvider, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaRecipeGenerator.Provider::new, completableFuture));
        TagProvider tagProvider = pack2.addProvider(Main.toFactory(VanillaBlockTagProvider::new, completableFuture));
        TagProvider tagProvider2 = pack2.addProvider(Main.toFactory(VanillaItemTagProvider::new, completableFuture));
        TagProvider tagProvider3 = pack2.addProvider(Main.toFactory(VanillaBiomeTagProvider::new, completableFuture));
        TagProvider tagProvider4 = pack2.addProvider(Main.toFactory(VanillaBannerPatternTagProvider::new, completableFuture));
        TagProvider tagProvider5 = pack2.addProvider(Main.toFactory(VanillaStructureTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaDamageTypeTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaDialogTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaEntityTypeTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaFlatLevelGeneratorPresetTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaFluidTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaGameEventTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaInstrumentTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaPaintingVariantTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaPointOfInterestTypeTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaWorldPresetTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaEnchantmentTagProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(VanillaTimelineTagProvider::new, completableFuture));
        pack2 = dataGenerator.createVanillaPack(includeServer);
        pack2.addProvider(output -> new NbtProvider(output, inputs));
        pack2 = dataGenerator.createVanillaPack(includeDev);
        pack2.addProvider(Main.toFactory(BiomeParametersProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(ItemListProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(BlockListProvider::new, completableFuture));
        pack2.addProvider(Main.toFactory(CommandSyntaxProvider::new, completableFuture));
        pack2.addProvider(RegistryDumpProvider::new);
        pack2.addProvider(PacketReportProvider::new);
        pack2.addProvider(DataPackStructureProvider::new);
        pack2.addProvider(RpcSchemaReferenceJsonProvider::new);
        CompletableFuture<RegistryBuilder.FullPatchesRegistriesPair> completableFuture2 = TradeRebalanceBuiltinRegistries.validate(completableFuture);
        CompletionStage completableFuture3 = completableFuture2.thenApply(RegistryBuilder.FullPatchesRegistriesPair::patches);
        DataGenerator.Pack pack3 = dataGenerator.createVanillaSubPack(includeClient, "trade_rebalance");
        pack3.addProvider(Main.toFactory(DynamicRegistriesProvider::new, (CompletableFuture<RegistryWrapper.WrapperLookup>)completableFuture3));
        pack3.addProvider(output -> MetadataProvider.create(output, Text.translatable("dataPack.trade_rebalance.description"), FeatureSet.of(FeatureFlags.TRADE_REBALANCE)));
        pack3.addProvider(Main.toFactory(TradeRebalanceLootTableProviders::createTradeRebalanceProvider, completableFuture));
        pack3.addProvider(Main.toFactory(TradeRebalanceEnchantmentTagProvider::new, completableFuture));
        pack2 = dataGenerator.createVanillaSubPack(includeClient, "redstone_experiments");
        pack2.addProvider(output -> MetadataProvider.create(output, Text.translatable("dataPack.redstone_experiments.description"), FeatureSet.of(FeatureFlags.REDSTONE_EXPERIMENTS)));
        pack2 = dataGenerator.createVanillaSubPack(includeClient, "minecart_improvements");
        pack2.addProvider(output -> MetadataProvider.create(output, Text.translatable("dataPack.minecart_improvements.description"), FeatureSet.of(FeatureFlags.MINECART_IMPROVEMENTS)));
    }
}

