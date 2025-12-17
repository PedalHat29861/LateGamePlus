/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public final class Path {
    public static final PacketCodec<PacketByteBuf, Path> PACKET_CODEC = PacketCodec.ofStatic((buf, path) -> path.toBuf((PacketByteBuf)((Object)buf)), Path::fromBuf);
    private final List<PathNode> nodes;
    private @Nullable DebugNodeInfo debugNodeInfos;
    private int currentNodeIndex;
    private final BlockPos target;
    private final float manhattanDistanceFromTarget;
    private final boolean reachesTarget;

    public Path(List<PathNode> nodes, BlockPos target, boolean reachesTarget) {
        this.nodes = nodes;
        this.target = target;
        this.manhattanDistanceFromTarget = nodes.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).getManhattanDistance(this.target);
        this.reachesTarget = reachesTarget;
    }

    public void next() {
        ++this.currentNodeIndex;
    }

    public boolean isStart() {
        return this.currentNodeIndex <= 0;
    }

    public boolean isFinished() {
        return this.currentNodeIndex >= this.nodes.size();
    }

    public @Nullable PathNode getEnd() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public PathNode getNode(int index) {
        return this.nodes.get(index);
    }

    public void setLength(int length) {
        if (this.nodes.size() > length) {
            this.nodes.subList(length, this.nodes.size()).clear();
        }
    }

    public void setNode(int index, PathNode node) {
        this.nodes.set(index, node);
    }

    public int getLength() {
        return this.nodes.size();
    }

    public int getCurrentNodeIndex() {
        return this.currentNodeIndex;
    }

    public void setCurrentNodeIndex(int nodeIndex) {
        this.currentNodeIndex = nodeIndex;
    }

    public Vec3d getNodePosition(Entity entity, int index) {
        PathNode pathNode = this.nodes.get(index);
        double d = (double)pathNode.x + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        double e = pathNode.y;
        double f = (double)pathNode.z + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        return new Vec3d(d, e, f);
    }

    public BlockPos getNodePos(int index) {
        return this.nodes.get(index).getBlockPos();
    }

    public Vec3d getNodePosition(Entity entity) {
        return this.getNodePosition(entity, this.currentNodeIndex);
    }

    public BlockPos getCurrentNodePos() {
        return this.nodes.get(this.currentNodeIndex).getBlockPos();
    }

    public PathNode getCurrentNode() {
        return this.nodes.get(this.currentNodeIndex);
    }

    public @Nullable PathNode getLastNode() {
        return this.currentNodeIndex > 0 ? this.nodes.get(this.currentNodeIndex - 1) : null;
    }

    public boolean equalsPath(@Nullable Path path) {
        return path != null && this.nodes.equals(path.nodes);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Path)) {
            return false;
        }
        Path path = (Path)o;
        return this.currentNodeIndex == path.currentNodeIndex && this.debugNodeInfos == path.debugNodeInfos && this.reachesTarget == path.reachesTarget && this.target.equals(path.target) && this.nodes.equals(path.nodes);
    }

    public int hashCode() {
        return this.currentNodeIndex + this.nodes.hashCode() * 31;
    }

    public boolean reachesTarget() {
        return this.reachesTarget;
    }

    @Debug
    void setDebugInfo(PathNode[] debugNodes, PathNode[] debugSecondNodes, Set<TargetPathNode> debugTargetNodes) {
        this.debugNodeInfos = new DebugNodeInfo(debugNodes, debugSecondNodes, debugTargetNodes);
    }

    public @Nullable DebugNodeInfo getDebugNodeInfos() {
        return this.debugNodeInfos;
    }

    public void toBuf(PacketByteBuf buf2) {
        if (this.debugNodeInfos == null || this.debugNodeInfos.targetNodes.isEmpty()) {
            throw new IllegalStateException("Missing debug data");
        }
        buf2.writeBoolean(this.reachesTarget);
        buf2.writeInt(this.currentNodeIndex);
        buf2.writeBlockPos(this.target);
        buf2.writeCollection(this.nodes, (buf, node) -> node.write((PacketByteBuf)((Object)buf)));
        this.debugNodeInfos.write(buf2);
    }

    public static Path fromBuf(PacketByteBuf buf) {
        boolean bl = buf.readBoolean();
        int i = buf.readInt();
        BlockPos blockPos = buf.readBlockPos();
        List<PathNode> list = buf.readList(PathNode::fromBuf);
        DebugNodeInfo debugNodeInfo = DebugNodeInfo.fromBuf(buf);
        Path path = new Path(list, blockPos, bl);
        path.debugNodeInfos = debugNodeInfo;
        path.currentNodeIndex = i;
        return path;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getManhattanDistanceFromTarget() {
        return this.manhattanDistanceFromTarget;
    }

    static PathNode[] nodesFromBuf(PacketByteBuf buf) {
        PathNode[] pathNodes = new PathNode[buf.readVarInt()];
        for (int i = 0; i < pathNodes.length; ++i) {
            pathNodes[i] = PathNode.fromBuf(buf);
        }
        return pathNodes;
    }

    static void write(PacketByteBuf buf, PathNode[] nodes) {
        buf.writeVarInt(nodes.length);
        for (PathNode pathNode : nodes) {
            pathNode.write(buf);
        }
    }

    public Path copy() {
        Path path = new Path(this.nodes, this.target, this.reachesTarget);
        path.debugNodeInfos = this.debugNodeInfos;
        path.currentNodeIndex = this.currentNodeIndex;
        return path;
    }

    public static final class DebugNodeInfo
    extends Record {
        private final PathNode[] openSet;
        private final PathNode[] closedSet;
        final Set<TargetPathNode> targetNodes;

        public DebugNodeInfo(PathNode[] openSet, PathNode[] closedSet, Set<TargetPathNode> targetNodes) {
            this.openSet = openSet;
            this.closedSet = closedSet;
            this.targetNodes = targetNodes;
        }

        public void write(PacketByteBuf buf2) {
            buf2.writeCollection(this.targetNodes, (buf, node) -> node.write((PacketByteBuf)((Object)buf)));
            Path.write(buf2, this.openSet);
            Path.write(buf2, this.closedSet);
        }

        public static DebugNodeInfo fromBuf(PacketByteBuf buf) {
            HashSet hashSet = buf.readCollection(HashSet::new, TargetPathNode::fromBuffer);
            PathNode[] pathNodes = Path.nodesFromBuf(buf);
            PathNode[] pathNodes2 = Path.nodesFromBuf(buf);
            return new DebugNodeInfo(pathNodes, pathNodes2, hashSet);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DebugNodeInfo.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DebugNodeInfo.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DebugNodeInfo.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this, object);
        }

        public PathNode[] openSet() {
            return this.openSet;
        }

        public PathNode[] closedSet() {
            return this.closedSet;
        }

        public Set<TargetPathNode> targetNodes() {
            return this.targetNodes;
        }
    }
}

