/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.report;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.HandshakeStates;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.network.state.QueryStates;

public class PacketReportProvider
implements DataProvider {
    private final DataOutput output;

    public PacketReportProvider(DataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("packets.json");
        return DataProvider.writeToPath(writer, this.toJson(), path);
    }

    private JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        Stream.of(HandshakeStates.C2S_FACTORY, QueryStates.S2C_FACTORY, QueryStates.C2S_FACTORY, LoginStates.S2C_FACTORY, LoginStates.C2S_FACTORY, ConfigurationStates.S2C_FACTORY, ConfigurationStates.C2S_FACTORY, PlayStateFactories.S2C, PlayStateFactories.C2S).map(NetworkState.Factory::buildUnbound).collect(Collectors.groupingBy(NetworkState.Unbound::phase)).forEach((phase, states) -> {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject.add(phase.getId(), (JsonElement)jsonObject2);
            states.forEach(state -> {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add(state.side().getName(), (JsonElement)jsonObject2);
                state.forEachPacketType((packetType, protocolId) -> {
                    JsonObject jsonObject2 = new JsonObject();
                    jsonObject2.addProperty("protocol_id", (Number)protocolId);
                    jsonObject2.add(packetType.id().toString(), (JsonElement)jsonObject2);
                });
            });
        });
        return jsonObject;
    }

    @Override
    public String getName() {
        return "Packet Report";
    }
}

