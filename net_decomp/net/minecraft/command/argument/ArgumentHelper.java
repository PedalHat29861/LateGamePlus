/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  org.slf4j.Logger
 */
package net.minecraft.command.argument;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.command.permission.PermissionSourcePredicate;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

public class ArgumentHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte MIN_FLAG = 1;
    private static final byte MAX_FLAG = 2;

    public static int getMinMaxFlag(boolean hasMin, boolean hasMax) {
        int i = 0;
        if (hasMin) {
            i |= 1;
        }
        if (hasMax) {
            i |= 2;
        }
        return i;
    }

    public static boolean hasMinFlag(byte flags) {
        return (flags & 1) != 0;
    }

    public static boolean hasMaxFlag(byte flags) {
        return (flags & 2) != 0;
    }

    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void writeArgumentTypeProperties(JsonObject json, ArgumentSerializer<A, T> serializer, ArgumentSerializer.ArgumentTypeProperties<A> properties) {
        serializer.writeJson(properties, json);
    }

    private static <T extends ArgumentType<?>> void writeArgument(JsonObject json, T argumentType) {
        ArgumentSerializer.ArgumentTypeProperties<T> argumentTypeProperties = ArgumentTypes.getArgumentTypeProperties(argumentType);
        json.addProperty("type", "argument");
        json.addProperty("parser", String.valueOf(Registries.COMMAND_ARGUMENT_TYPE.getId(argumentTypeProperties.getSerializer())));
        JsonObject jsonObject = new JsonObject();
        ArgumentHelper.writeArgumentTypeProperties(jsonObject, argumentTypeProperties.getSerializer(), argumentTypeProperties);
        if (!jsonObject.isEmpty()) {
            json.add("properties", (JsonElement)jsonObject);
        }
    }

    public static <S> JsonObject toJson(CommandDispatcher<S> dispatcher, CommandNode<S> node) {
        Collection collection2;
        Object rootCommandNode;
        JsonObject jsonObject = new JsonObject();
        CommandNode<S> commandNode = node;
        Objects.requireNonNull(commandNode);
        CommandNode<S> commandNode2 = commandNode;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, LiteralCommandNode.class, ArgumentCommandNode.class}, commandNode2, n)) {
            case 0: {
                rootCommandNode = (RootCommandNode)commandNode2;
                jsonObject.addProperty("type", "root");
                break;
            }
            case 1: {
                LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode2;
                jsonObject.addProperty("type", "literal");
                break;
            }
            case 2: {
                ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode2;
                ArgumentHelper.writeArgument(jsonObject, argumentCommandNode.getType());
                break;
            }
            default: {
                LOGGER.error("Could not serialize node {} ({})!", node, node.getClass());
                jsonObject.addProperty("type", "unknown");
            }
        }
        Collection collection = node.getChildren();
        if (!collection.isEmpty()) {
            JsonObject jsonObject2 = new JsonObject();
            rootCommandNode = collection.iterator();
            while (rootCommandNode.hasNext()) {
                CommandNode commandNode3 = (CommandNode)rootCommandNode.next();
                jsonObject2.add(commandNode3.getName(), (JsonElement)ArgumentHelper.toJson(dispatcher, commandNode3));
            }
            jsonObject.add("children", (JsonElement)jsonObject2);
        }
        if (node.getCommand() != null) {
            jsonObject.addProperty("executable", Boolean.valueOf(true));
        }
        if ((rootCommandNode = node.getRequirement()) instanceof PermissionSourcePredicate) {
            PermissionSourcePredicate permissionSourcePredicate = (PermissionSourcePredicate)rootCommandNode;
            JsonElement jsonElement = (JsonElement)PermissionCheck.CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)permissionSourcePredicate.test()).getOrThrow(error -> new IllegalStateException("Failed to serialize requirement: " + error));
            jsonObject.add("permissions", jsonElement);
        }
        if (node.getRedirect() != null && !(collection2 = dispatcher.getPath(node.getRedirect())).isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            for (String string : collection2) {
                jsonArray.add(string);
            }
            jsonObject.add("redirect", (JsonElement)jsonArray);
        }
        return jsonObject;
    }

    public static <T> Set<ArgumentType<?>> collectUsedArgumentTypes(CommandNode<T> rootNode) {
        ReferenceOpenHashSet set = new ReferenceOpenHashSet();
        HashSet set2 = new HashSet();
        ArgumentHelper.collectUsedArgumentTypes(rootNode, set2, set);
        return set2;
    }

    private static <T> void collectUsedArgumentTypes(CommandNode<T> node, Set<ArgumentType<?>> usedArgumentTypes, Set<CommandNode<T>> visitedNodes) {
        if (!visitedNodes.add(node)) {
            return;
        }
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)node;
            usedArgumentTypes.add(argumentCommandNode.getType());
        }
        node.getChildren().forEach(child -> ArgumentHelper.collectUsedArgumentTypes(child, usedArgumentTypes, visitedNodes));
        CommandNode commandNode = node.getRedirect();
        if (commandNode != null) {
            ArgumentHelper.collectUsedArgumentTypes(commandNode, usedArgumentTypes, visitedNodes);
        }
    }
}

