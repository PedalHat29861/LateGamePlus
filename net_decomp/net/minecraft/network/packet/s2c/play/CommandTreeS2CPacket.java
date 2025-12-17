/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public class CommandTreeS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, CommandTreeS2CPacket> CODEC = Packet.createCodec(CommandTreeS2CPacket::write, CommandTreeS2CPacket::new);
    private static final byte NODE_TYPE_MASK = 3;
    private static final byte EXECUTABLE = 4;
    private static final byte HAS_REDIRECT = 8;
    private static final byte HAS_SUGGESTION_PROVIDER = 16;
    private static final byte REQUIRES_LEVEL = 32;
    private static final byte NODE_TYPE_ROOT = 0;
    private static final byte NODE_TYPE_LITERAL = 1;
    private static final byte NODE_TYPE_ARGUMENT = 2;
    private final int rootSize;
    private final List<CommandNodeData> nodes;

    public <S> CommandTreeS2CPacket(RootCommandNode<S> rootIndex, CommandNodeInspector<S> inspector) {
        Object2IntMap<CommandNode<S>> object2IntMap = CommandTreeS2CPacket.traverse(rootIndex);
        this.nodes = CommandTreeS2CPacket.collectNodes(object2IntMap, inspector);
        this.rootSize = object2IntMap.getInt(rootIndex);
    }

    private CommandTreeS2CPacket(PacketByteBuf buf) {
        this.nodes = buf.readList(CommandTreeS2CPacket::readCommandNode);
        this.rootSize = buf.readVarInt();
        CommandTreeS2CPacket.validate(this.nodes);
    }

    private void write(PacketByteBuf buf) {
        buf.writeCollection(this.nodes, (buf2, node) -> node.write((PacketByteBuf)((Object)buf2)));
        buf.writeVarInt(this.rootSize);
    }

    private static void validate(List<CommandNodeData> nodeDatas, BiPredicate<CommandNodeData, IntSet> validator) {
        IntOpenHashSet intSet = new IntOpenHashSet((IntCollection)IntSets.fromTo((int)0, (int)nodeDatas.size()));
        while (!intSet.isEmpty()) {
            boolean bl = intSet.removeIf(arg_0 -> CommandTreeS2CPacket.method_42068(validator, nodeDatas, (IntSet)intSet, arg_0));
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static void validate(List<CommandNodeData> nodeDatas) {
        CommandTreeS2CPacket.validate(nodeDatas, CommandNodeData::validateRedirectNodeIndex);
        CommandTreeS2CPacket.validate(nodeDatas, CommandNodeData::validateChildNodeIndices);
    }

    private static <S> Object2IntMap<CommandNode<S>> traverse(RootCommandNode<S> commandTree) {
        CommandNode commandNode;
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        ArrayDeque<Object> queue = new ArrayDeque<Object>();
        queue.add(commandTree);
        while ((commandNode = (CommandNode)queue.poll()) != null) {
            if (object2IntMap.containsKey((Object)commandNode)) continue;
            int i = object2IntMap.size();
            object2IntMap.put((Object)commandNode, i);
            queue.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() == null) continue;
            queue.add(commandNode.getRedirect());
        }
        return object2IntMap;
    }

    private static <S> List<CommandNodeData> collectNodes(Object2IntMap<CommandNode<S>> nodeOrdinals, CommandNodeInspector<S> inspector) {
        ObjectArrayList objectArrayList = new ObjectArrayList(nodeOrdinals.size());
        objectArrayList.size(nodeOrdinals.size());
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(nodeOrdinals)) {
            objectArrayList.set(entry.getIntValue(), (Object)CommandTreeS2CPacket.createNodeData((CommandNode)entry.getKey(), inspector, nodeOrdinals));
        }
        return objectArrayList;
    }

    private static CommandNodeData readCommandNode(PacketByteBuf buf) {
        byte b = buf.readByte();
        int[] is = buf.readIntArray();
        int i = (b & 8) != 0 ? buf.readVarInt() : 0;
        SuggestableNode suggestableNode = CommandTreeS2CPacket.readArgumentBuilder(buf, b);
        return new CommandNodeData(suggestableNode, b, i, is);
    }

    private static @Nullable SuggestableNode readArgumentBuilder(PacketByteBuf buf, byte flags) {
        int i = flags & 3;
        if (i == 2) {
            String string = buf.readString();
            int j = buf.readVarInt();
            ArgumentSerializer argumentSerializer = (ArgumentSerializer)Registries.COMMAND_ARGUMENT_TYPE.get(j);
            if (argumentSerializer == null) {
                return null;
            }
            Object argumentTypeProperties = argumentSerializer.fromPacket(buf);
            Identifier identifier = (flags & 0x10) != 0 ? buf.readIdentifier() : null;
            return new ArgumentNode(string, (ArgumentSerializer.ArgumentTypeProperties<?>)argumentTypeProperties, identifier);
        }
        if (i == 1) {
            String string = buf.readString();
            return new LiteralNode(string);
        }
        return null;
    }

    private static <S> CommandNodeData createNodeData(CommandNode<S> node, CommandNodeInspector<S> inspector, Object2IntMap<CommandNode<S>> nodeOrdinals) {
        Record suggestableNode;
        int j;
        int i = 0;
        if (node.getRedirect() != null) {
            i |= 8;
            j = nodeOrdinals.getInt((Object)node.getRedirect());
        } else {
            j = 0;
        }
        if (inspector.isExecutable(node)) {
            i |= 4;
        }
        if (inspector.hasRequiredLevel(node)) {
            i |= 0x20;
        }
        CommandNode<S> commandNode = node;
        Objects.requireNonNull(commandNode);
        CommandNode<S> commandNode2 = commandNode;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, ArgumentCommandNode.class, LiteralCommandNode.class}, commandNode2, n)) {
            case 0: {
                RootCommandNode rootCommandNode = (RootCommandNode)commandNode2;
                i |= 0;
                suggestableNode = null;
                break;
            }
            case 1: {
                ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode2;
                Identifier identifier = inspector.getSuggestionProviderId(argumentCommandNode);
                suggestableNode = new ArgumentNode(argumentCommandNode.getName(), ArgumentTypes.getArgumentTypeProperties(argumentCommandNode.getType()), identifier);
                i |= 2;
                if (identifier != null) {
                    i |= 0x10;
                }
                break;
            }
            case 2: {
                LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode2;
                suggestableNode = new LiteralNode(literalCommandNode.getLiteral());
                i |= 1;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown node type " + String.valueOf(node));
            }
        }
        int[] is = node.getChildren().stream().mapToInt(arg_0 -> nodeOrdinals.getInt(arg_0)).toArray();
        return new CommandNodeData((SuggestableNode)((Object)suggestableNode), i, j, is);
    }

    @Override
    public PacketType<CommandTreeS2CPacket> getPacketType() {
        return PlayPackets.COMMANDS;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onCommandTree(this);
    }

    public <S> RootCommandNode<S> getCommandTree(CommandRegistryAccess commandRegistryAccess, NodeFactory<S> nodeFactory) {
        return (RootCommandNode)new CommandTree<S>(commandRegistryAccess, nodeFactory, this.nodes).getNode(this.rootSize);
    }

    private static /* synthetic */ boolean method_42068(BiPredicate index, List list, IntSet intSet, int i) {
        return index.test((CommandNodeData)list.get(i), intSet);
    }

    public static interface CommandNodeInspector<S> {
        public @Nullable Identifier getSuggestionProviderId(ArgumentCommandNode<S, ?> var1);

        public boolean isExecutable(CommandNode<S> var1);

        public boolean hasRequiredLevel(CommandNode<S> var1);
    }

    static final class CommandNodeData
    extends Record {
        final @Nullable SuggestableNode suggestableNode;
        final int flags;
        final int redirectNodeIndex;
        final int[] childNodeIndices;

        CommandNodeData(@Nullable SuggestableNode suggestableNode, int flags, int redirectNodeIndex, int[] childNodeIndices) {
            this.suggestableNode = suggestableNode;
            this.flags = flags;
            this.redirectNodeIndex = redirectNodeIndex;
            this.childNodeIndices = childNodeIndices;
        }

        public void write(PacketByteBuf buf) {
            buf.writeByte(this.flags);
            buf.writeIntArray(this.childNodeIndices);
            if ((this.flags & 8) != 0) {
                buf.writeVarInt(this.redirectNodeIndex);
            }
            if (this.suggestableNode != null) {
                this.suggestableNode.write(buf);
            }
        }

        public boolean validateRedirectNodeIndex(IntSet indices) {
            if ((this.flags & 8) != 0) {
                return !indices.contains(this.redirectNodeIndex);
            }
            return true;
        }

        public boolean validateChildNodeIndices(IntSet indices) {
            for (int i : this.childNodeIndices) {
                if (!indices.contains(i)) continue;
                return false;
            }
            return true;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CommandNodeData.class, "stub;flags;redirect;children", "suggestableNode", "flags", "redirectNodeIndex", "childNodeIndices"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CommandNodeData.class, "stub;flags;redirect;children", "suggestableNode", "flags", "redirectNodeIndex", "childNodeIndices"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CommandNodeData.class, "stub;flags;redirect;children", "suggestableNode", "flags", "redirectNodeIndex", "childNodeIndices"}, this, object);
        }

        public @Nullable SuggestableNode suggestableNode() {
            return this.suggestableNode;
        }

        public int flags() {
            return this.flags;
        }

        public int redirectNodeIndex() {
            return this.redirectNodeIndex;
        }

        public int[] childNodeIndices() {
            return this.childNodeIndices;
        }
    }

    static interface SuggestableNode {
        public <S> ArgumentBuilder<S, ?> createArgumentBuilder(CommandRegistryAccess var1, NodeFactory<S> var2);

        public void write(PacketByteBuf var1);
    }

    record ArgumentNode(String name, ArgumentSerializer.ArgumentTypeProperties<?> properties, @Nullable Identifier id) implements SuggestableNode
    {
        @Override
        public <S> ArgumentBuilder<S, ?> createArgumentBuilder(CommandRegistryAccess commandRegistryAccess, NodeFactory<S> nodeFactory) {
            Object argumentType = this.properties.createType(commandRegistryAccess);
            return nodeFactory.argument(this.name, (ArgumentType<?>)argumentType, this.id);
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeString(this.name);
            ArgumentNode.write(buf, this.properties);
            if (this.id != null) {
                buf.writeIdentifier(this.id);
            }
        }

        private static <A extends ArgumentType<?>> void write(PacketByteBuf buf, ArgumentSerializer.ArgumentTypeProperties<A> properties) {
            ArgumentNode.write(buf, properties.getSerializer(), properties);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void write(PacketByteBuf buf, ArgumentSerializer<A, T> serializer, ArgumentSerializer.ArgumentTypeProperties<A> properties) {
            buf.writeVarInt(Registries.COMMAND_ARGUMENT_TYPE.getRawId(serializer));
            serializer.writePacket(properties, buf);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ArgumentNode.class, "id;argumentType;suggestionId", "name", "properties", "id"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ArgumentNode.class, "id;argumentType;suggestionId", "name", "properties", "id"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ArgumentNode.class, "id;argumentType;suggestionId", "name", "properties", "id"}, this, object);
        }
    }

    record LiteralNode(String literal) implements SuggestableNode
    {
        @Override
        public <S> ArgumentBuilder<S, ?> createArgumentBuilder(CommandRegistryAccess commandRegistryAccess, NodeFactory<S> nodeFactory) {
            return nodeFactory.literal(this.literal);
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeString(this.literal);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LiteralNode.class, "id", "literal"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LiteralNode.class, "id", "literal"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LiteralNode.class, "id", "literal"}, this, object);
        }
    }

    static class CommandTree<S> {
        private final CommandRegistryAccess commandRegistryAccess;
        private final NodeFactory<S> nodeFactory;
        private final List<CommandNodeData> nodeDatas;
        private final List<CommandNode<S>> nodes;

        CommandTree(CommandRegistryAccess commandRegistryAccess, NodeFactory<S> nodeFactory, List<CommandNodeData> nodeDatas) {
            this.commandRegistryAccess = commandRegistryAccess;
            this.nodeFactory = nodeFactory;
            this.nodeDatas = nodeDatas;
            ObjectArrayList objectArrayList = new ObjectArrayList();
            objectArrayList.size(nodeDatas.size());
            this.nodes = objectArrayList;
        }

        public CommandNode<S> getNode(int index) {
            RootCommandNode commandNode2;
            CommandNode<S> commandNode = this.nodes.get(index);
            if (commandNode != null) {
                return commandNode;
            }
            CommandNodeData commandNodeData = this.nodeDatas.get(index);
            if (commandNodeData.suggestableNode == null) {
                commandNode2 = new RootCommandNode();
            } else {
                ArgumentBuilder<S, ?> argumentBuilder = commandNodeData.suggestableNode.createArgumentBuilder(this.commandRegistryAccess, this.nodeFactory);
                if ((commandNodeData.flags & 8) != 0) {
                    argumentBuilder.redirect(this.getNode(commandNodeData.redirectNodeIndex));
                }
                boolean bl = (commandNodeData.flags & 4) != 0;
                boolean bl2 = (commandNodeData.flags & 0x20) != 0;
                commandNode2 = this.nodeFactory.modifyNode(argumentBuilder, bl, bl2).build();
            }
            this.nodes.set(index, (CommandNode<S>)commandNode2);
            for (int i : commandNodeData.childNodeIndices) {
                CommandNode<S> commandNode3 = this.getNode(i);
                if (commandNode3 instanceof RootCommandNode) continue;
                commandNode2.addChild(commandNode3);
            }
            return commandNode2;
        }
    }

    public static interface NodeFactory<S> {
        public ArgumentBuilder<S, ?> literal(String var1);

        public ArgumentBuilder<S, ?> argument(String var1, ArgumentType<?> var2, @Nullable Identifier var3);

        public ArgumentBuilder<S, ?> modifyNode(ArgumentBuilder<S, ?> var1, boolean var2, boolean var3);
    }
}

