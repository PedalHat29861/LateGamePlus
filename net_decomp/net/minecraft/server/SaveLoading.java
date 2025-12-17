/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;

public class SaveLoading {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <D, R> CompletableFuture<R> load(ServerConfig serverConfig, LoadContextSupplier<D> loadContextSupplier, SaveApplierFactory<D, R> saveApplierFactory, Executor prepareExecutor, Executor applyExecutor) {
        try {
            Pair<DataConfiguration, LifecycledResourceManager> pair = serverConfig.dataPacks.load();
            LifecycledResourceManager lifecycledResourceManager = (LifecycledResourceManager)pair.getSecond();
            CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries = ServerDynamicRegistryType.createCombinedDynamicRegistries();
            List<Registry.PendingTagLoad<?>> list = TagGroupLoader.startReload((ResourceManager)lifecycledResourceManager, combinedDynamicRegistries.get(ServerDynamicRegistryType.STATIC));
            DynamicRegistryManager.Immutable immutable = combinedDynamicRegistries.getPrecedingRegistryManagers(ServerDynamicRegistryType.WORLDGEN);
            List<RegistryWrapper.Impl<?>> list2 = TagGroupLoader.collectRegistries(immutable, list);
            DynamicRegistryManager.Immutable immutable2 = RegistryLoader.loadFromResource(lifecycledResourceManager, list2, RegistryLoader.DYNAMIC_REGISTRIES);
            List<RegistryWrapper.Impl<?>> list3 = Stream.concat(list2.stream(), immutable2.stream()).toList();
            DynamicRegistryManager.Immutable immutable3 = RegistryLoader.loadFromResource(lifecycledResourceManager, list3, RegistryLoader.DIMENSION_REGISTRIES);
            DataConfiguration dataConfiguration = (DataConfiguration)pair.getFirst();
            RegistryWrapper.WrapperLookup wrapperLookup = RegistryWrapper.WrapperLookup.of(list3.stream());
            LoadContext<D> loadContext = loadContextSupplier.get(new LoadContextSupplierContext(lifecycledResourceManager, dataConfiguration, wrapperLookup, immutable3));
            CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries2 = combinedDynamicRegistries.with(ServerDynamicRegistryType.WORLDGEN, immutable2, loadContext.dimensionsRegistryManager);
            return ((CompletableFuture)DataPackContents.reload(lifecycledResourceManager, combinedDynamicRegistries2, list, dataConfiguration.enabledFeatures(), serverConfig.commandEnvironment(), serverConfig.functionCompilationPermissions(), prepareExecutor, applyExecutor).whenComplete((dataPackContents, throwable) -> {
                if (throwable != null) {
                    lifecycledResourceManager.close();
                }
            })).thenApplyAsync(dataPackContents -> {
                dataPackContents.applyPendingTagLoads();
                return saveApplierFactory.create(lifecycledResourceManager, (DataPackContents)dataPackContents, combinedDynamicRegistries2, loadContext.extraData);
            }, applyExecutor);
        }
        catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    public static final class ServerConfig
    extends Record {
        final DataPacks dataPacks;
        private final CommandManager.RegistrationEnvironment commandEnvironment;
        private final PermissionPredicate functionCompilationPermissions;

        public ServerConfig(DataPacks dataPacks, CommandManager.RegistrationEnvironment commandEnvironment, PermissionPredicate functionCompilationPermissions) {
            this.dataPacks = dataPacks;
            this.commandEnvironment = commandEnvironment;
            this.functionCompilationPermissions = functionCompilationPermissions;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ServerConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "dataPacks", "commandEnvironment", "functionCompilationPermissions"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ServerConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "dataPacks", "commandEnvironment", "functionCompilationPermissions"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ServerConfig.class, "packConfig;commandSelection;functionCompilationPermissions", "dataPacks", "commandEnvironment", "functionCompilationPermissions"}, this, object);
        }

        public DataPacks dataPacks() {
            return this.dataPacks;
        }

        public CommandManager.RegistrationEnvironment commandEnvironment() {
            return this.commandEnvironment;
        }

        public PermissionPredicate functionCompilationPermissions() {
            return this.functionCompilationPermissions;
        }
    }

    public record DataPacks(ResourcePackManager manager, DataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<DataConfiguration, LifecycledResourceManager> load() {
            DataConfiguration dataConfiguration = MinecraftServer.loadDataPacks(this.manager, this.initialDataConfig, this.initMode, this.safeMode);
            List<ResourcePack> list = this.manager.createResourcePacks();
            LifecycledResourceManagerImpl lifecycledResourceManager = new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, list);
            return Pair.of((Object)dataConfiguration, (Object)lifecycledResourceManager);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataPacks.class, "packRepository;initialDataConfig;safeMode;initMode", "manager", "initialDataConfig", "safeMode", "initMode"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataPacks.class, "packRepository;initialDataConfig;safeMode;initMode", "manager", "initialDataConfig", "safeMode", "initMode"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataPacks.class, "packRepository;initialDataConfig;safeMode;initMode", "manager", "initialDataConfig", "safeMode", "initMode"}, this, object);
        }
    }

    public record LoadContextSupplierContext(ResourceManager resourceManager, DataConfiguration dataConfiguration, RegistryWrapper.WrapperLookup worldGenRegistryManager, DynamicRegistryManager.Immutable dimensionsRegistryManager) {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoadContextSupplierContext.class, "resources;dataConfiguration;datapackWorldgen;datapackDimensions", "resourceManager", "dataConfiguration", "worldGenRegistryManager", "dimensionsRegistryManager"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoadContextSupplierContext.class, "resources;dataConfiguration;datapackWorldgen;datapackDimensions", "resourceManager", "dataConfiguration", "worldGenRegistryManager", "dimensionsRegistryManager"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoadContextSupplierContext.class, "resources;dataConfiguration;datapackWorldgen;datapackDimensions", "resourceManager", "dataConfiguration", "worldGenRegistryManager", "dimensionsRegistryManager"}, this, object);
        }
    }

    @FunctionalInterface
    public static interface LoadContextSupplier<D> {
        public LoadContext<D> get(LoadContextSupplierContext var1);
    }

    public static final class LoadContext<D>
    extends Record {
        final D extraData;
        final DynamicRegistryManager.Immutable dimensionsRegistryManager;

        public LoadContext(D extraData, DynamicRegistryManager.Immutable dimensionsRegistryManager) {
            this.extraData = extraData;
            this.dimensionsRegistryManager = dimensionsRegistryManager;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoadContext.class, "cookie;finalDimensions", "extraData", "dimensionsRegistryManager"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoadContext.class, "cookie;finalDimensions", "extraData", "dimensionsRegistryManager"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoadContext.class, "cookie;finalDimensions", "extraData", "dimensionsRegistryManager"}, this, object);
        }

        public D extraData() {
            return this.extraData;
        }

        public DynamicRegistryManager.Immutable dimensionsRegistryManager() {
            return this.dimensionsRegistryManager;
        }
    }

    @FunctionalInterface
    public static interface SaveApplierFactory<D, R> {
        public R create(LifecycledResourceManager var1, DataPackContents var2, CombinedDynamicRegistries<ServerDynamicRegistryType> var3, D var4);
    }
}

