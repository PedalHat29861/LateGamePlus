/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  org.apache.commons.lang3.function.TriFunction
 *  org.slf4j.Logger
 */
package net.minecraft.world.waypoint;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.waypoint.EntityTickProgress;
import net.minecraft.world.waypoint.Waypoint;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

public abstract class TrackedWaypoint
implements Waypoint {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final PacketCodec<ByteBuf, TrackedWaypoint> PACKET_CODEC = PacketCodec.of(TrackedWaypoint::writeBuf, TrackedWaypoint::fromBuf);
    protected final Either<UUID, String> source;
    private final Waypoint.Config config;
    private final Type type;

    TrackedWaypoint(Either<UUID, String> source, Waypoint.Config config, Type type) {
        this.source = source;
        this.config = config;
        this.type = type;
    }

    public Either<UUID, String> getSource() {
        return this.source;
    }

    public abstract void handleUpdate(TrackedWaypoint var1);

    public void writeBuf(ByteBuf buf) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(buf);
        packetByteBuf.writeEither(this.source, Uuids.PACKET_CODEC, PacketByteBuf::writeString);
        Waypoint.Config.PACKET_CODEC.encode(packetByteBuf, this.config);
        packetByteBuf.writeEnumConstant(this.type);
        this.writeAdditionalDataToBuf(buf);
    }

    public abstract void writeAdditionalDataToBuf(ByteBuf var1);

    private static TrackedWaypoint fromBuf(ByteBuf buf) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(buf);
        Either<UUID, String> either = packetByteBuf.readEither(Uuids.PACKET_CODEC, PacketByteBuf::readString);
        Waypoint.Config config = (Waypoint.Config)Waypoint.Config.PACKET_CODEC.decode(packetByteBuf);
        Type type = packetByteBuf.readEnumConstant(Type.class);
        return (TrackedWaypoint)type.factory.apply(either, (Object)config, (Object)packetByteBuf);
    }

    public static TrackedWaypoint ofPos(UUID source, Waypoint.Config config, Vec3i pos) {
        return new Positional(source, config, pos);
    }

    public static TrackedWaypoint ofChunk(UUID source, Waypoint.Config config, ChunkPos chunkPos) {
        return new ChunkBased(source, config, chunkPos);
    }

    public static TrackedWaypoint ofAzimuth(UUID source, Waypoint.Config config, float azimuth) {
        return new Azimuth(source, config, azimuth);
    }

    public static TrackedWaypoint empty(UUID uuid) {
        return new Empty(uuid);
    }

    public abstract double getRelativeYaw(World var1, YawProvider var2, EntityTickProgress var3);

    public abstract Pitch getPitch(World var1, PitchProvider var2, EntityTickProgress var3);

    public abstract double squaredDistanceTo(Entity var1);

    public Waypoint.Config getConfig() {
        return this.config;
    }

    static final class Type
    extends Enum<Type> {
        public static final /* enum */ Type EMPTY = new Type((TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint>)((TriFunction)Empty::new));
        public static final /* enum */ Type VEC3I = new Type((TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint>)((TriFunction)Positional::new));
        public static final /* enum */ Type CHUNK = new Type((TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint>)((TriFunction)ChunkBased::new));
        public static final /* enum */ Type AZIMUTH = new Type((TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint>)((TriFunction)Azimuth::new));
        final TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint> factory;
        private static final /* synthetic */ Type[] field_59783;

        public static Type[] values() {
            return (Type[])field_59783.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(TriFunction<Either<UUID, String>, Waypoint.Config, PacketByteBuf, TrackedWaypoint> factory) {
            this.factory = factory;
        }

        private static /* synthetic */ Type[] method_70779() {
            return new Type[]{EMPTY, VEC3I, CHUNK, AZIMUTH};
        }

        static {
            field_59783 = Type.method_70779();
        }
    }

    static class Positional
    extends TrackedWaypoint {
        private Vec3i pos;

        public Positional(UUID uuid, Waypoint.Config config, Vec3i pos) {
            super((Either<UUID, String>)Either.left((Object)uuid), config, Type.VEC3I);
            this.pos = pos;
        }

        public Positional(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.VEC3I);
            this.pos = new Vec3i(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
            if (waypoint instanceof Positional) {
                Positional positional = (Positional)waypoint;
                this.pos = positional.pos;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", waypoint.getClass());
            }
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
            VarInts.write(buf, this.pos.getX());
            VarInts.write(buf, this.pos.getY());
            VarInts.write(buf, this.pos.getZ());
        }

        private Vec3d getSourcePos(World world, EntityTickProgress tickProgress) {
            return this.source.left().map(world::getEntity).map(entity -> {
                if (entity.getBlockPos().getManhattanDistance(this.pos) > 3) {
                    return null;
                }
                return entity.getCameraPosVec(tickProgress.getTickProgress((Entity)entity));
            }).orElseGet(() -> Vec3d.ofCenter(this.pos));
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            Vec3d vec3d = yawProvider.getCameraPos().subtract(this.getSourcePos(world, tickProgress)).rotateYClockwise();
            float f = (float)MathHelper.atan2(vec3d.getZ(), vec3d.getX()) * 57.295776f;
            return MathHelper.subtractAngles(yawProvider.getCameraYaw(), f);
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            double d;
            Vec3d vec3d = cameraProvider.project(this.getSourcePos(world, tickProgress));
            boolean bl = vec3d.z > 1.0;
            double d2 = d = bl ? -vec3d.y : vec3d.y;
            if (d < -1.0) {
                return Pitch.DOWN;
            }
            if (d > 1.0) {
                return Pitch.UP;
            }
            if (bl) {
                if (vec3d.y > 0.0) {
                    return Pitch.UP;
                }
                if (vec3d.y < 0.0) {
                    return Pitch.DOWN;
                }
            }
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return receiver.squaredDistanceTo(Vec3d.ofCenter(this.pos));
        }
    }

    static class ChunkBased
    extends TrackedWaypoint {
        private ChunkPos chunkPos;

        public ChunkBased(UUID source, Waypoint.Config config, ChunkPos chunkPos) {
            super((Either<UUID, String>)Either.left((Object)source), config, Type.CHUNK);
            this.chunkPos = chunkPos;
        }

        public ChunkBased(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.CHUNK);
            this.chunkPos = new ChunkPos(buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
            if (waypoint instanceof ChunkBased) {
                ChunkBased chunkBased = (ChunkBased)waypoint;
                this.chunkPos = chunkBased.chunkPos;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", waypoint.getClass());
            }
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
            VarInts.write(buf, this.chunkPos.x);
            VarInts.write(buf, this.chunkPos.z);
        }

        private Vec3d getChunkCenterPos(double y) {
            return Vec3d.ofCenter(this.chunkPos.getCenterAtY((int)y));
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            Vec3d vec3d = yawProvider.getCameraPos();
            Vec3d vec3d2 = vec3d.subtract(this.getChunkCenterPos(vec3d.getY())).rotateYClockwise();
            float f = (float)MathHelper.atan2(vec3d2.getZ(), vec3d2.getX()) * 57.295776f;
            return MathHelper.subtractAngles(yawProvider.getCameraYaw(), f);
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            double d = cameraProvider.getPitch();
            if (d < -1.0) {
                return Pitch.DOWN;
            }
            if (d > 1.0) {
                return Pitch.UP;
            }
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return receiver.squaredDistanceTo(Vec3d.ofCenter(this.chunkPos.getCenterAtY(receiver.getBlockY())));
        }
    }

    static class Azimuth
    extends TrackedWaypoint {
        private float azimuth;

        public Azimuth(UUID source, Waypoint.Config config, float azimuth) {
            super((Either<UUID, String>)Either.left((Object)source), config, Type.AZIMUTH);
            this.azimuth = azimuth;
        }

        public Azimuth(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.AZIMUTH);
            this.azimuth = buf.readFloat();
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
            if (waypoint instanceof Azimuth) {
                Azimuth azimuth = (Azimuth)waypoint;
                this.azimuth = azimuth.azimuth;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", waypoint.getClass());
            }
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
            buf.writeFloat(this.azimuth);
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            return MathHelper.subtractAngles(yawProvider.getCameraYaw(), this.azimuth * 57.295776f);
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            double d = cameraProvider.getPitch();
            if (d < -1.0) {
                return Pitch.DOWN;
            }
            if (d > 1.0) {
                return Pitch.UP;
            }
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return Double.POSITIVE_INFINITY;
        }
    }

    static class Empty
    extends TrackedWaypoint {
        private Empty(Either<UUID, String> source, Waypoint.Config config, PacketByteBuf buf) {
            super(source, config, Type.EMPTY);
        }

        Empty(UUID source) {
            super((Either<UUID, String>)Either.left((Object)source), Waypoint.Config.DEFAULT, Type.EMPTY);
        }

        @Override
        public void handleUpdate(TrackedWaypoint waypoint) {
        }

        @Override
        public void writeAdditionalDataToBuf(ByteBuf buf) {
        }

        @Override
        public double getRelativeYaw(World world, YawProvider yawProvider, EntityTickProgress tickProgress) {
            return Double.NaN;
        }

        @Override
        public Pitch getPitch(World world, PitchProvider cameraProvider, EntityTickProgress tickProgress) {
            return Pitch.NONE;
        }

        @Override
        public double squaredDistanceTo(Entity receiver) {
            return Double.POSITIVE_INFINITY;
        }
    }

    public static interface YawProvider {
        public float getCameraYaw();

        public Vec3d getCameraPos();
    }

    public static interface PitchProvider {
        public Vec3d project(Vec3d var1);

        public double getPitch();
    }

    public static final class Pitch
    extends Enum<Pitch> {
        public static final /* enum */ Pitch NONE = new Pitch();
        public static final /* enum */ Pitch UP = new Pitch();
        public static final /* enum */ Pitch DOWN = new Pitch();
        private static final /* synthetic */ Pitch[] field_60426;

        public static Pitch[] values() {
            return (Pitch[])field_60426.clone();
        }

        public static Pitch valueOf(String string) {
            return Enum.valueOf(Pitch.class, string);
        }

        private static /* synthetic */ Pitch[] method_71494() {
            return new Pitch[]{NONE, UP, DOWN};
        }

        static {
            field_60426 = Pitch.method_71494();
        }
    }
}

