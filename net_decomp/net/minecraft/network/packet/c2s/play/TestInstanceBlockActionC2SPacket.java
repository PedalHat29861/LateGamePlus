/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.c2s.play;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.TestInstance;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record TestInstanceBlockActionC2SPacket(BlockPos pos, Action action, TestInstanceBlockEntity.Data data) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, TestInstanceBlockActionC2SPacket> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, TestInstanceBlockActionC2SPacket::pos, Action.CODEC, TestInstanceBlockActionC2SPacket::action, TestInstanceBlockEntity.Data.PACKET_CODEC, TestInstanceBlockActionC2SPacket::data, TestInstanceBlockActionC2SPacket::new);

    public TestInstanceBlockActionC2SPacket(BlockPos pos, Action actin, Optional<RegistryKey<TestInstance>> optional, Vec3i vec3i, BlockRotation blockRotation, boolean bl) {
        this(pos, actin, new TestInstanceBlockEntity.Data(optional, vec3i, blockRotation, bl, TestInstanceBlockEntity.Status.CLEARED, Optional.empty()));
    }

    @Override
    public PacketType<TestInstanceBlockActionC2SPacket> getPacketType() {
        return PlayPackets.TEST_INSTANCE_BLOCK_ACTION;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onTestInstanceBlockAction(this);
    }

    public static final class Action
    extends Enum<Action> {
        public static final /* enum */ Action INIT = new Action(0);
        public static final /* enum */ Action QUERY = new Action(1);
        public static final /* enum */ Action SET = new Action(2);
        public static final /* enum */ Action RESET = new Action(3);
        public static final /* enum */ Action SAVE = new Action(4);
        public static final /* enum */ Action EXPORT = new Action(5);
        public static final /* enum */ Action RUN = new Action(6);
        private static final IntFunction<Action> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Action> CODEC;
        private final int index;
        private static final /* synthetic */ Action[] field_55930;

        public static Action[] values() {
            return (Action[])field_55930.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private Action(int index) {
            this.index = index;
        }

        private static /* synthetic */ Action[] method_66585() {
            return new Action[]{INIT, QUERY, SET, RESET, SAVE, EXPORT, RUN};
        }

        static {
            field_55930 = Action.method_66585();
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(action -> action.index, Action.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = PacketCodecs.indexed(INDEX_MAPPER, action -> action.index);
        }
    }
}

