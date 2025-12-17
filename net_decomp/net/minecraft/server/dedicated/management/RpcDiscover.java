/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dedicated.management;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.server.dedicated.management.IncomingRpcMethod;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.RpcMethodInfo;
import net.minecraft.server.dedicated.management.schema.RpcSchema;
import net.minecraft.server.dedicated.management.schema.RpcSchemaEntry;

public class RpcDiscover {
    public static Document handleRpcDiscover(List<RpcSchemaEntry<?>> entries) {
        ArrayList list = new ArrayList(Registries.INCOMING_RPC_METHOD.size() + Registries.OUTGOING_RPC_METHOD.size());
        Registries.INCOMING_RPC_METHOD.streamEntries().forEach(entry -> {
            if (((IncomingRpcMethod)entry.value()).attributes().discoverable()) {
                list.add(((IncomingRpcMethod)entry.value()).info().toEntry(entry.registryKey().getValue()));
            }
        });
        Registries.OUTGOING_RPC_METHOD.streamEntries().forEach(entry -> {
            if (((OutgoingRpcMethod)entry.value()).attributes().discoverable()) {
                list.add(((OutgoingRpcMethod)entry.value()).info().toEntry(entry.registryKey().getValue()));
            }
        });
        HashMap map = new HashMap();
        for (RpcSchemaEntry<?> rpcSchemaEntry : entries) {
            map.put(rpcSchemaEntry.name(), rpcSchemaEntry.schema().copy());
        }
        Info info = new Info("Minecraft Server JSON-RPC", "2.0.0");
        return new Document("1.3.2", info, list, new Components(map));
    }

    public record Info(String title, String version) {
        public static final MapCodec<Info> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("title").forGetter(Info::title), (App)Codec.STRING.fieldOf("version").forGetter(Info::version)).apply((Applicative)instance, Info::new));
    }

    public record Document(String jsonRpcProtocolVersion, Info discoverInfo, List<RpcMethodInfo.Entry<?, ?>> methods, Components components) {
        public static final MapCodec<Document> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("openrpc").forGetter(Document::jsonRpcProtocolVersion), (App)Info.CODEC.codec().fieldOf("info").forGetter(Document::discoverInfo), (App)Codec.list(RpcMethodInfo.Entry.CODEC).fieldOf("methods").forGetter(Document::methods), (App)Components.CODEC.codec().fieldOf("components").forGetter(Document::components)).apply((Applicative)instance, Document::new));
    }

    public record Components(Map<String, RpcSchema<?>> schemas) {
        public static final MapCodec<Components> CODEC = Components.createCodec();

        private static MapCodec<Components> createCodec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap((Codec)Codec.STRING, RpcSchema.CODEC).fieldOf("schemas").forGetter(Components::schemas)).apply((Applicative)instance, Components::new));
        }
    }
}

