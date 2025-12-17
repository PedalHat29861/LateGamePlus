/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class FunctionLoader
implements ResourceReloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final RegistryKey<Registry<CommandFunction<ServerCommandSource>>> FUNCTION_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("function"));
    private static final ResourceFinder FINDER = new ResourceFinder(RegistryKeys.getPath(FUNCTION_REGISTRY_KEY), ".mcfunction");
    private volatile Map<Identifier, CommandFunction<ServerCommandSource>> functions = ImmutableMap.of();
    private final TagGroupLoader<CommandFunction<ServerCommandSource>> tagLoader = new TagGroupLoader((id, required) -> this.get(id), RegistryKeys.getTagPath(FUNCTION_REGISTRY_KEY));
    private volatile Map<Identifier, List<CommandFunction<ServerCommandSource>>> tags = Map.of();
    private final PermissionPredicate permissions;
    private final CommandDispatcher<ServerCommandSource> commandDispatcher;

    public Optional<CommandFunction<ServerCommandSource>> get(Identifier id) {
        return Optional.ofNullable(this.functions.get(id));
    }

    public Map<Identifier, CommandFunction<ServerCommandSource>> getFunctions() {
        return this.functions;
    }

    public List<CommandFunction<ServerCommandSource>> getTagOrEmpty(Identifier id) {
        return this.tags.getOrDefault(id, List.of());
    }

    public Iterable<Identifier> getTags() {
        return this.tags.keySet();
    }

    public FunctionLoader(PermissionPredicate permissions, CommandDispatcher<ServerCommandSource> commandDispatcher) {
        this.permissions = permissions;
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Store store, Executor executor, ResourceReloader.Synchronizer synchronizer, Executor executor2) {
        ResourceManager resourceManager = store.getResourceManager();
        CompletableFuture<Map> completableFuture = CompletableFuture.supplyAsync(() -> this.tagLoader.loadTags(resourceManager), executor);
        CompletionStage completableFuture2 = CompletableFuture.supplyAsync(() -> FINDER.findResources(resourceManager), executor).thenCompose(functions -> {
            HashMap map = Maps.newHashMap();
            ServerCommandSource serverCommandSource = CommandManager.createSource(this.permissions);
            for (Map.Entry entry : functions.entrySet()) {
                Identifier identifier = (Identifier)entry.getKey();
                Identifier identifier2 = FINDER.toResourceId(identifier);
                map.put(identifier2, CompletableFuture.supplyAsync(() -> {
                    List<String> list = FunctionLoader.readLines((Resource)entry.getValue());
                    return CommandFunction.create(identifier2, this.commandDispatcher, serverCommandSource, list);
                }, executor));
            }
            CompletableFuture[] completableFutures = map.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf(completableFutures).handle((unused, ex) -> map);
        });
        return ((CompletableFuture)((CompletableFuture)completableFuture.thenCombine(completableFuture2, Pair::of)).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(intermediate -> {
            Map map = (Map)intermediate.getSecond();
            ImmutableMap.Builder builder = ImmutableMap.builder();
            map.forEach((id, functionFuture) -> ((CompletableFuture)functionFuture.handle((function, ex) -> {
                if (ex != null) {
                    LOGGER.error("Failed to load function {}", id, ex);
                } else {
                    builder.put(id, function);
                }
                return null;
            })).join());
            this.functions = builder.build();
            this.tags = this.tagLoader.buildGroup((Map)intermediate.getFirst());
        }, executor2);
    }

    private static List<String> readLines(Resource resource) {
        List<String> list;
        block8: {
            BufferedReader bufferedReader = resource.getReader();
            try {
                list = bufferedReader.lines().toList();
                if (bufferedReader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException iOException) {
                    throw new CompletionException(iOException);
                }
            }
            bufferedReader.close();
        }
        return list;
    }
}

