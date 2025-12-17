/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Unit;
import org.slf4j.Logger;

public class DataPackContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> COMPLETED_UNIT = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final ReloadableRegistries.Lookup reloadableRegistries;
    private final CommandManager commandManager;
    private final ServerRecipeManager recipeManager;
    private final ServerAdvancementLoader serverAdvancementLoader;
    private final FunctionLoader functionLoader;
    private final List<Registry.PendingTagLoad<?>> pendingTagLoads;

    private DataPackContents(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, List<Registry.PendingTagLoad<?>> pendingTagLoads, PermissionPredicate permissions) {
        this.reloadableRegistries = new ReloadableRegistries.Lookup(dynamicRegistries.getCombinedRegistryManager());
        this.pendingTagLoads = pendingTagLoads;
        this.recipeManager = new ServerRecipeManager(registries);
        this.commandManager = new CommandManager(environment, CommandRegistryAccess.of(registries, enabledFeatures));
        this.serverAdvancementLoader = new ServerAdvancementLoader(registries);
        this.functionLoader = new FunctionLoader(permissions, this.commandManager.getDispatcher());
    }

    public FunctionLoader getFunctionLoader() {
        return this.functionLoader;
    }

    public ReloadableRegistries.Lookup getReloadableRegistries() {
        return this.reloadableRegistries;
    }

    public ServerRecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public ServerAdvancementLoader getServerAdvancementLoader() {
        return this.serverAdvancementLoader;
    }

    public List<ResourceReloader> getContents() {
        return List.of(this.recipeManager, this.functionLoader, this.serverAdvancementLoader);
    }

    public static CompletableFuture<DataPackContents> reload(ResourceManager resourceManager, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, List<Registry.PendingTagLoad<?>> pendingTagLoads, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, PermissionPredicate permissions, Executor prepareExecutor, Executor applyExecutor) {
        return ReloadableRegistries.reload(dynamicRegistries, pendingTagLoads, resourceManager, prepareExecutor).thenCompose(reloadResult -> {
            DataPackContents dataPackContents = new DataPackContents(reloadResult.layers(), reloadResult.lookupWithUpdatedTags(), enabledFeatures, environment, pendingTagLoads, permissions);
            return SimpleResourceReload.start(resourceManager, dataPackContents.getContents(), prepareExecutor, applyExecutor, COMPLETED_UNIT, LOGGER.isDebugEnabled()).whenComplete().thenApply(void_ -> dataPackContents);
        });
    }

    public void applyPendingTagLoads() {
        this.pendingTagLoads.forEach(Registry.PendingTagLoad::apply);
    }
}

