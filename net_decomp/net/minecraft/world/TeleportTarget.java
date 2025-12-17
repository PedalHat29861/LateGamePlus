/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProperties;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public final class TeleportTarget
extends Record {
    private final ServerWorld world;
    private final Vec3d position;
    private final Vec3d velocity;
    private final float yaw;
    private final float pitch;
    private final boolean missingRespawnBlock;
    private final boolean asPassenger;
    private final Set<PositionFlag> relatives;
    private final PostDimensionTransition postTeleportTransition;
    public static final PostDimensionTransition NO_OP = entity -> {};
    public static final PostDimensionTransition SEND_TRAVEL_THROUGH_PORTAL_PACKET = TeleportTarget::sendTravelThroughPortalPacket;
    public static final PostDimensionTransition ADD_PORTAL_CHUNK_TICKET = TeleportTarget::addPortalChunkTicket;

    public TeleportTarget(ServerWorld world, Vec3d pos, Vec3d velocity, float yaw, float pitch, PostDimensionTransition postDimensionTransition) {
        this(world, pos, velocity, yaw, pitch, Set.of(), postDimensionTransition);
    }

    public TeleportTarget(ServerWorld world, Vec3d pos, Vec3d velocity, float yaw, float pitch, Set<PositionFlag> flags, PostDimensionTransition postDimensionTransition) {
        this(world, pos, velocity, yaw, pitch, false, false, flags, postDimensionTransition);
    }

    public TeleportTarget(ServerWorld world, Vec3d position, Vec3d velocity, float yaw, float pitch, boolean missingRespawnBlock, boolean asPassenger, Set<PositionFlag> relatives, PostDimensionTransition postTeleportTransition) {
        this.world = world;
        this.position = position;
        this.velocity = velocity;
        this.yaw = yaw;
        this.pitch = pitch;
        this.missingRespawnBlock = missingRespawnBlock;
        this.asPassenger = asPassenger;
        this.relatives = relatives;
        this.postTeleportTransition = postTeleportTransition;
    }

    private static void sendTravelThroughPortalPacket(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            serverPlayerEntity.networkHandler.sendPacket(new WorldEventS2CPacket(1032, BlockPos.ORIGIN, 0, false));
        }
    }

    private static void addPortalChunkTicket(Entity entity) {
        entity.addPortalChunkTicketAt(BlockPos.ofFloored(entity.getEntityPos()));
    }

    public static TeleportTarget noRespawnPointSet(ServerPlayerEntity player, PostDimensionTransition postDimensionTransition) {
        ServerWorld serverWorld = player.getEntityWorld().getServer().getSpawnWorld();
        WorldProperties.SpawnPoint spawnPoint = serverWorld.getSpawnPoint();
        return new TeleportTarget(serverWorld, TeleportTarget.getWorldSpawnPos(serverWorld, player), Vec3d.ZERO, spawnPoint.yaw(), spawnPoint.pitch(), false, false, Set.of(), postDimensionTransition);
    }

    public static TeleportTarget missingSpawnBlock(ServerPlayerEntity player, PostDimensionTransition postDimensionTransition) {
        ServerWorld serverWorld = player.getEntityWorld().getServer().getSpawnWorld();
        WorldProperties.SpawnPoint spawnPoint = serverWorld.getSpawnPoint();
        return new TeleportTarget(serverWorld, TeleportTarget.getWorldSpawnPos(serverWorld, player), Vec3d.ZERO, spawnPoint.yaw(), spawnPoint.pitch(), true, false, Set.of(), postDimensionTransition);
    }

    private static Vec3d getWorldSpawnPos(ServerWorld world, Entity entity) {
        return entity.getWorldSpawnPos(world, world.getSpawnPoint().getPos()).toBottomCenterPos();
    }

    public TeleportTarget withRotation(float yaw, float pitch) {
        return new TeleportTarget(this.world(), this.position(), this.velocity(), yaw, pitch, this.missingRespawnBlock(), this.asPassenger(), this.relatives(), this.postTeleportTransition());
    }

    public TeleportTarget withPosition(Vec3d position) {
        return new TeleportTarget(this.world(), position, this.velocity(), this.yaw(), this.pitch(), this.missingRespawnBlock(), this.asPassenger(), this.relatives(), this.postTeleportTransition());
    }

    public TeleportTarget asPassenger() {
        return new TeleportTarget(this.world(), this.position(), this.velocity(), this.yaw(), this.pitch(), this.missingRespawnBlock(), true, this.relatives(), this.postTeleportTransition());
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{TeleportTarget.class, "newLevel;position;deltaMovement;yRot;xRot;missingRespawnBlock;asPassenger;relatives;postTeleportTransition", "world", "position", "velocity", "yaw", "pitch", "missingRespawnBlock", "asPassenger", "relatives", "postTeleportTransition"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TeleportTarget.class, "newLevel;position;deltaMovement;yRot;xRot;missingRespawnBlock;asPassenger;relatives;postTeleportTransition", "world", "position", "velocity", "yaw", "pitch", "missingRespawnBlock", "asPassenger", "relatives", "postTeleportTransition"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TeleportTarget.class, "newLevel;position;deltaMovement;yRot;xRot;missingRespawnBlock;asPassenger;relatives;postTeleportTransition", "world", "position", "velocity", "yaw", "pitch", "missingRespawnBlock", "asPassenger", "relatives", "postTeleportTransition"}, this, object);
    }

    public ServerWorld world() {
        return this.world;
    }

    public Vec3d position() {
        return this.position;
    }

    public Vec3d velocity() {
        return this.velocity;
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public boolean missingRespawnBlock() {
        return this.missingRespawnBlock;
    }

    public boolean asPassenger() {
        return this.asPassenger;
    }

    public Set<PositionFlag> relatives() {
        return this.relatives;
    }

    public PostDimensionTransition postTeleportTransition() {
        return this.postTeleportTransition;
    }

    @FunctionalInterface
    public static interface PostDimensionTransition {
        public void onTransition(Entity var1);

        default public PostDimensionTransition then(PostDimensionTransition next) {
            return entity -> {
                this.onTransition(entity);
                next.onTransition(entity);
            };
        }
    }
}

