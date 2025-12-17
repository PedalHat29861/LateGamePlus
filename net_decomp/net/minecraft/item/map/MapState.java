/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.item.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapFrameMarker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MapState
extends PersistentState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE = 128;
    private static final int SIZE_HALF = 64;
    public static final int MAX_SCALE = 4;
    public static final int MAX_DECORATIONS = 256;
    private static final String FRAME_PREFIX = "frame-";
    public static final Codec<MapState> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)World.CODEC.fieldOf("dimension").forGetter(mapState -> mapState.dimension), (App)Codec.INT.fieldOf("xCenter").forGetter(mapState -> mapState.centerX), (App)Codec.INT.fieldOf("zCenter").forGetter(mapState -> mapState.centerZ), (App)Codec.BYTE.optionalFieldOf("scale", (Object)0).forGetter(mapState -> mapState.scale), (App)Codec.BYTE_BUFFER.fieldOf("colors").forGetter(mapState -> ByteBuffer.wrap(mapState.colors)), (App)Codec.BOOL.optionalFieldOf("trackingPosition", (Object)true).forGetter(mapState -> mapState.showDecorations), (App)Codec.BOOL.optionalFieldOf("unlimitedTracking", (Object)false).forGetter(mapState -> mapState.unlimitedTracking), (App)Codec.BOOL.optionalFieldOf("locked", (Object)false).forGetter(mapState -> mapState.locked), (App)MapBannerMarker.CODEC.listOf().optionalFieldOf("banners", List.of()).forGetter(mapState -> List.copyOf(mapState.banners.values())), (App)MapFrameMarker.CODEC.listOf().optionalFieldOf("frames", List.of()).forGetter(mapState -> List.copyOf(mapState.frames.values()))).apply((Applicative)instance, MapState::new));
    public final int centerX;
    public final int centerZ;
    public final RegistryKey<World> dimension;
    private final boolean showDecorations;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<PlayerUpdateTracker> updateTrackers = Lists.newArrayList();
    private final Map<PlayerEntity, PlayerUpdateTracker> updateTrackersByPlayer = Maps.newHashMap();
    private final Map<String, MapBannerMarker> banners = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrameMarker> frames = Maps.newHashMap();
    private int decorationCount;

    public static PersistentStateType<MapState> createStateType(MapIdComponent mapId) {
        return new PersistentStateType<MapState>(mapId.asString(), () -> {
            throw new IllegalStateException("Should never create an empty map saved data");
        }, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    private MapState(int centerX, int centerZ, byte scale, boolean showDecorations, boolean unlimitedTracking, boolean locked, RegistryKey<World> dimension) {
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.dimension = dimension;
        this.showDecorations = showDecorations;
        this.unlimitedTracking = unlimitedTracking;
        this.locked = locked;
    }

    private MapState(RegistryKey<World> dimension, int centerX, int centerZ, byte scale, ByteBuffer colors, boolean showDecorations, boolean unlimitedTracking, boolean locked, List<MapBannerMarker> banners, List<MapFrameMarker> frames) {
        this(centerX, centerZ, (byte)MathHelper.clamp(scale, 0, 4), showDecorations, unlimitedTracking, locked, dimension);
        if (colors.array().length == 16384) {
            this.colors = colors.array();
        }
        for (MapBannerMarker mapBannerMarker : banners) {
            this.banners.put(mapBannerMarker.getKey(), mapBannerMarker);
            this.addDecoration(mapBannerMarker.getDecorationType(), null, mapBannerMarker.getKey(), mapBannerMarker.pos().getX(), mapBannerMarker.pos().getZ(), 180.0, mapBannerMarker.name().orElse(null));
        }
        for (MapFrameMarker mapFrameMarker : frames) {
            this.frames.put(mapFrameMarker.getKey(), mapFrameMarker);
            this.addDecoration(MapDecorationTypes.FRAME, null, MapState.getFrameDecorationKey(mapFrameMarker.entityId()), mapFrameMarker.pos().getX(), mapFrameMarker.pos().getZ(), mapFrameMarker.rotation(), null);
        }
    }

    public static MapState of(double centerX, double centerZ, byte scale, boolean showDecorations, boolean unlimitedTracking, RegistryKey<World> dimension) {
        int i = 128 * (1 << scale);
        int j = MathHelper.floor((centerX + 64.0) / (double)i);
        int k = MathHelper.floor((centerZ + 64.0) / (double)i);
        int l = j * i + i / 2 - 64;
        int m = k * i + i / 2 - 64;
        return new MapState(l, m, scale, showDecorations, unlimitedTracking, false, dimension);
    }

    public static MapState of(byte scale, boolean locked, RegistryKey<World> dimension) {
        return new MapState(0, 0, scale, false, false, locked, dimension);
    }

    public MapState copy() {
        MapState mapState = new MapState(this.centerX, this.centerZ, this.scale, this.showDecorations, this.unlimitedTracking, true, this.dimension);
        mapState.banners.putAll(this.banners);
        mapState.decorations.putAll(this.decorations);
        mapState.decorationCount = this.decorationCount;
        System.arraycopy(this.colors, 0, mapState.colors, 0, this.colors.length);
        return mapState;
    }

    public MapState zoomOut() {
        return MapState.of(this.centerX, this.centerZ, (byte)MathHelper.clamp(this.scale + 1, 0, 4), this.showDecorations, this.unlimitedTracking, this.dimension);
    }

    private static Predicate<ItemStack> getEqualPredicate(ItemStack stack) {
        MapIdComponent mapIdComponent = stack.get(DataComponentTypes.MAP_ID);
        return other -> {
            if (other == stack) {
                return true;
            }
            return other.isOf(stack.getItem()) && Objects.equals(mapIdComponent, other.get(DataComponentTypes.MAP_ID));
        };
    }

    public void update(PlayerEntity player, ItemStack stack) {
        if (!this.updateTrackersByPlayer.containsKey(player)) {
            PlayerUpdateTracker playerUpdateTracker = new PlayerUpdateTracker(player);
            this.updateTrackersByPlayer.put(player, playerUpdateTracker);
            this.updateTrackers.add(playerUpdateTracker);
        }
        Predicate<ItemStack> predicate = MapState.getEqualPredicate(stack);
        if (!player.getInventory().contains(predicate)) {
            this.removeDecoration(player.getStringifiedName());
        }
        for (int i = 0; i < this.updateTrackers.size(); ++i) {
            PlayerUpdateTracker playerUpdateTracker2 = this.updateTrackers.get(i);
            PlayerEntity playerEntity = playerUpdateTracker2.player;
            String string = playerEntity.getStringifiedName();
            if (playerEntity.isRemoved() || !playerEntity.getInventory().contains(predicate) && !stack.isInFrame()) {
                this.updateTrackersByPlayer.remove(playerEntity);
                this.updateTrackers.remove(playerUpdateTracker2);
                this.removeDecoration(string);
            } else if (!stack.isInFrame() && playerEntity.getEntityWorld().getRegistryKey() == this.dimension && this.showDecorations) {
                this.addDecoration(MapDecorationTypes.PLAYER, playerEntity.getEntityWorld(), string, playerEntity.getX(), playerEntity.getZ(), playerEntity.getYaw(), null);
            }
            if (playerEntity.equals(player) || !MapState.hasMapInvisibilityEquipment(playerEntity)) continue;
            this.removeDecoration(string);
        }
        if (stack.isInFrame() && this.showDecorations) {
            ItemFrameEntity itemFrameEntity = stack.getFrame();
            BlockPos blockPos = itemFrameEntity.getAttachedBlockPos();
            MapFrameMarker mapFrameMarker = this.frames.get(MapFrameMarker.getKey(blockPos));
            if (mapFrameMarker != null && itemFrameEntity.getId() != mapFrameMarker.entityId() && this.frames.containsKey(mapFrameMarker.getKey())) {
                this.removeDecoration(MapState.getFrameDecorationKey(mapFrameMarker.entityId()));
            }
            MapFrameMarker mapFrameMarker2 = new MapFrameMarker(blockPos, itemFrameEntity.getHorizontalFacing().getHorizontalQuarterTurns() * 90, itemFrameEntity.getId());
            this.addDecoration(MapDecorationTypes.FRAME, player.getEntityWorld(), MapState.getFrameDecorationKey(itemFrameEntity.getId()), blockPos.getX(), blockPos.getZ(), itemFrameEntity.getHorizontalFacing().getHorizontalQuarterTurns() * 90, null);
            MapFrameMarker mapFrameMarker3 = this.frames.put(mapFrameMarker2.getKey(), mapFrameMarker2);
            if (!mapFrameMarker2.equals(mapFrameMarker3)) {
                this.markDirty();
            }
        }
        MapDecorationsComponent mapDecorationsComponent = stack.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
        if (!this.decorations.keySet().containsAll(mapDecorationsComponent.decorations().keySet())) {
            mapDecorationsComponent.decorations().forEach((id, decoration) -> {
                if (!this.decorations.containsKey(id)) {
                    this.addDecoration(decoration.type(), player.getEntityWorld(), (String)id, decoration.x(), decoration.z(), decoration.rotation(), null);
                }
            });
        }
    }

    private static boolean hasMapInvisibilityEquipment(PlayerEntity player) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND || !player.getEquippedStack(equipmentSlot).isIn(ItemTags.MAP_INVISIBILITY_EQUIPMENT)) continue;
            return true;
        }
        return false;
    }

    private void removeDecoration(String id) {
        MapDecoration mapDecoration = this.decorations.remove(id);
        if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
            --this.decorationCount;
        }
        this.markDecorationsDirty();
    }

    public static void addDecorationsNbt(ItemStack stack, BlockPos pos, String id, RegistryEntry<MapDecorationType> decorationType) {
        MapDecorationsComponent.Decoration decoration = new MapDecorationsComponent.Decoration(decorationType, pos.getX(), pos.getZ(), 180.0f);
        stack.apply(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT, decorations -> decorations.with(id, decoration));
        if (decorationType.value().hasMapColor()) {
            stack.set(DataComponentTypes.MAP_COLOR, new MapColorComponent(decorationType.value().mapColor()));
        }
    }

    private void addDecoration(RegistryEntry<MapDecorationType> type, @Nullable WorldAccess world, String key, double x, double z, double rotation, @Nullable Text text) {
        MapDecoration mapDecoration2;
        int i = 1 << this.scale;
        float f = (float)(x - (double)this.centerX) / (float)i;
        float g = (float)(z - (double)this.centerZ) / (float)i;
        Marker marker = this.getMarker(type, world, rotation, f, g);
        if (marker == null) {
            this.removeDecoration(key);
            return;
        }
        MapDecoration mapDecoration = new MapDecoration(marker.type(), marker.x(), marker.y(), marker.rot(), Optional.ofNullable(text));
        if (!mapDecoration.equals(mapDecoration2 = this.decorations.put(key, mapDecoration))) {
            if (mapDecoration2 != null && mapDecoration2.type().value().trackCount()) {
                --this.decorationCount;
            }
            if (marker.type().value().trackCount()) {
                ++this.decorationCount;
            }
            this.markDecorationsDirty();
        }
    }

    private @Nullable Marker getMarker(RegistryEntry<MapDecorationType> type, @Nullable WorldAccess world, double rotation, float dx, float dz) {
        byte b = MapState.offsetToMarkerPosition(dx);
        byte c = MapState.offsetToMarkerPosition(dz);
        if (type.matches(MapDecorationTypes.PLAYER)) {
            Pair<RegistryEntry<MapDecorationType>, Byte> pair = this.getPlayerMarkerAndRotation(type, world, rotation, dx, dz);
            return pair == null ? null : new Marker((RegistryEntry)pair.getFirst(), b, c, (Byte)pair.getSecond());
        }
        if (MapState.isInBounds(dx, dz) || this.unlimitedTracking) {
            return new Marker(type, b, c, this.getPlayerMarkerRotation(world, rotation));
        }
        return null;
    }

    private @Nullable Pair<RegistryEntry<MapDecorationType>, Byte> getPlayerMarkerAndRotation(RegistryEntry<MapDecorationType> type, @Nullable WorldAccess world, double rotation, float dx, float dz) {
        if (MapState.isInBounds(dx, dz)) {
            return Pair.of(type, (Object)this.getPlayerMarkerRotation(world, rotation));
        }
        RegistryEntry<MapDecorationType> registryEntry = this.getPlayerMarker(dx, dz);
        if (registryEntry == null) {
            return null;
        }
        return Pair.of(registryEntry, (Object)0);
    }

    private byte getPlayerMarkerRotation(@Nullable WorldAccess world, double rotation) {
        if (this.dimension == World.NETHER && world != null) {
            int i = (int)(world.getTime() / 10L);
            return (byte)(i * i * 34187121 + i * 121 >> 15 & 0xF);
        }
        double d = rotation < 0.0 ? rotation - 8.0 : rotation + 8.0;
        return (byte)(d * 16.0 / 360.0);
    }

    private static boolean isInBounds(float dx, float dz) {
        int i = 63;
        return dx >= -63.0f && dz >= -63.0f && dx <= 63.0f && dz <= 63.0f;
    }

    private @Nullable RegistryEntry<MapDecorationType> getPlayerMarker(float dx, float dz) {
        boolean bl;
        int i = 320;
        boolean bl2 = bl = Math.abs(dx) < 320.0f && Math.abs(dz) < 320.0f;
        if (bl) {
            return MapDecorationTypes.PLAYER_OFF_MAP;
        }
        return this.unlimitedTracking ? MapDecorationTypes.PLAYER_OFF_LIMITS : null;
    }

    private static byte offsetToMarkerPosition(float d) {
        int i = 63;
        if (d <= -63.0f) {
            return -128;
        }
        if (d >= 63.0f) {
            return 127;
        }
        return (byte)((double)(d * 2.0f) + 0.5);
    }

    public @Nullable Packet<?> getPlayerMarkerPacket(MapIdComponent mapId, PlayerEntity player) {
        PlayerUpdateTracker playerUpdateTracker = this.updateTrackersByPlayer.get(player);
        if (playerUpdateTracker == null) {
            return null;
        }
        return playerUpdateTracker.getPacket(mapId);
    }

    private void markDirty(int x, int z) {
        this.markDirty();
        for (PlayerUpdateTracker playerUpdateTracker : this.updateTrackers) {
            playerUpdateTracker.markDirty(x, z);
        }
    }

    private void markDecorationsDirty() {
        this.updateTrackers.forEach(PlayerUpdateTracker::markDecorationsDirty);
    }

    public PlayerUpdateTracker getPlayerSyncData(PlayerEntity player) {
        PlayerUpdateTracker playerUpdateTracker = this.updateTrackersByPlayer.get(player);
        if (playerUpdateTracker == null) {
            playerUpdateTracker = new PlayerUpdateTracker(player);
            this.updateTrackersByPlayer.put(player, playerUpdateTracker);
            this.updateTrackers.add(playerUpdateTracker);
        }
        return playerUpdateTracker;
    }

    public boolean addBanner(WorldAccess world, BlockPos pos) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getZ() + 0.5;
        int i = 1 << this.scale;
        double f = (d - (double)this.centerX) / (double)i;
        double g = (e - (double)this.centerZ) / (double)i;
        int j = 63;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapBannerMarker mapBannerMarker = MapBannerMarker.fromWorldBlock(world, pos);
            if (mapBannerMarker == null) {
                return false;
            }
            if (this.banners.remove(mapBannerMarker.getKey(), mapBannerMarker)) {
                this.removeDecoration(mapBannerMarker.getKey());
                this.markDirty();
                return true;
            }
            if (!this.decorationCountNotLessThan(256)) {
                this.banners.put(mapBannerMarker.getKey(), mapBannerMarker);
                this.addDecoration(mapBannerMarker.getDecorationType(), world, mapBannerMarker.getKey(), d, e, 180.0, mapBannerMarker.name().orElse(null));
                this.markDirty();
                return true;
            }
        }
        return false;
    }

    public void removeBanner(BlockView world, int x, int z) {
        Iterator<MapBannerMarker> iterator = this.banners.values().iterator();
        while (iterator.hasNext()) {
            MapBannerMarker mapBannerMarker2;
            MapBannerMarker mapBannerMarker = iterator.next();
            if (mapBannerMarker.pos().getX() != x || mapBannerMarker.pos().getZ() != z || mapBannerMarker.equals(mapBannerMarker2 = MapBannerMarker.fromWorldBlock(world, mapBannerMarker.pos()))) continue;
            iterator.remove();
            this.removeDecoration(mapBannerMarker.getKey());
            this.markDirty();
        }
    }

    public Collection<MapBannerMarker> getBanners() {
        return this.banners.values();
    }

    public void removeFrame(BlockPos pos, int id) {
        this.removeDecoration(MapState.getFrameDecorationKey(id));
        this.frames.remove(MapFrameMarker.getKey(pos));
        this.markDirty();
    }

    public boolean putColor(int x, int z, byte color) {
        byte b = this.colors[x + z * 128];
        if (b != color) {
            this.setColor(x, z, color);
            return true;
        }
        return false;
    }

    public void setColor(int x, int z, byte color) {
        this.colors[x + z * 128] = color;
        this.markDirty(x, z);
    }

    public boolean hasExplorationMapDecoration() {
        for (MapDecoration mapDecoration : this.decorations.values()) {
            if (!mapDecoration.type().value().explorationMapElement()) continue;
            return true;
        }
        return false;
    }

    public void replaceDecorations(List<MapDecoration> decorations) {
        this.decorations.clear();
        this.decorationCount = 0;
        for (int i = 0; i < decorations.size(); ++i) {
            MapDecoration mapDecoration = decorations.get(i);
            this.decorations.put("icon-" + i, mapDecoration);
            if (!mapDecoration.type().value().trackCount()) continue;
            ++this.decorationCount;
        }
    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean decorationCountNotLessThan(int decorationCount) {
        return this.decorationCount >= decorationCount;
    }

    private static String getFrameDecorationKey(int id) {
        return FRAME_PREFIX + id;
    }

    public class PlayerUpdateTracker {
        public final PlayerEntity player;
        private boolean dirty = true;
        private int startX;
        private int startZ;
        private int endX = 127;
        private int endZ = 127;
        private boolean decorationsDirty = true;
        private int emptyPacketsRequested;
        public int field_131;

        PlayerUpdateTracker(PlayerEntity player) {
            this.player = player;
        }

        private UpdateData getMapUpdateData() {
            int i = this.startX;
            int j = this.startZ;
            int k = this.endX + 1 - this.startX;
            int l = this.endZ + 1 - this.startZ;
            byte[] bs = new byte[k * l];
            for (int m = 0; m < k; ++m) {
                for (int n = 0; n < l; ++n) {
                    bs[m + n * k] = MapState.this.colors[i + m + (j + n) * 128];
                }
            }
            return new UpdateData(i, j, k, l, bs);
        }

        @Nullable Packet<?> getPacket(MapIdComponent mapId) {
            Collection<MapDecoration> collection;
            UpdateData updateData;
            if (this.dirty) {
                this.dirty = false;
                updateData = this.getMapUpdateData();
            } else {
                updateData = null;
            }
            if (this.decorationsDirty && this.emptyPacketsRequested++ % 5 == 0) {
                this.decorationsDirty = false;
                collection = MapState.this.decorations.values();
            } else {
                collection = null;
            }
            if (collection != null || updateData != null) {
                return new MapUpdateS2CPacket(mapId, MapState.this.scale, MapState.this.locked, collection, updateData);
            }
            return null;
        }

        void markDirty(int startX, int startZ) {
            if (this.dirty) {
                this.startX = Math.min(this.startX, startX);
                this.startZ = Math.min(this.startZ, startZ);
                this.endX = Math.max(this.endX, startX);
                this.endZ = Math.max(this.endZ, startZ);
            } else {
                this.dirty = true;
                this.startX = startX;
                this.startZ = startZ;
                this.endX = startX;
                this.endZ = startZ;
            }
        }

        private void markDecorationsDirty() {
            this.decorationsDirty = true;
        }
    }

    record Marker(RegistryEntry<MapDecorationType> type, byte x, byte y, byte rot) {
    }

    public record UpdateData(int startX, int startZ, int width, int height, byte[] colors) {
        public static final PacketCodec<ByteBuf, Optional<UpdateData>> CODEC = PacketCodec.ofStatic(UpdateData::encode, UpdateData::decode);

        private static void encode(ByteBuf buf, Optional<UpdateData> updateData) {
            if (updateData.isPresent()) {
                UpdateData updateData2 = updateData.get();
                buf.writeByte(updateData2.width);
                buf.writeByte(updateData2.height);
                buf.writeByte(updateData2.startX);
                buf.writeByte(updateData2.startZ);
                PacketByteBuf.writeByteArray(buf, updateData2.colors);
            } else {
                buf.writeByte(0);
            }
        }

        private static Optional<UpdateData> decode(ByteBuf buf) {
            short i = buf.readUnsignedByte();
            if (i > 0) {
                short j = buf.readUnsignedByte();
                short k = buf.readUnsignedByte();
                short l = buf.readUnsignedByte();
                byte[] bs = PacketByteBuf.readByteArray(buf);
                return Optional.of(new UpdateData(k, l, i, j, bs));
            }
            return Optional.empty();
        }

        public void setColorsTo(MapState mapState) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapState.setColor(this.startX + i, this.startZ + j, this.colors[i + j * this.width]);
                }
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{UpdateData.class, "startX;startY;width;height;mapColors", "startX", "startZ", "width", "height", "colors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UpdateData.class, "startX;startY;width;height;mapColors", "startX", "startZ", "width", "height", "colors"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UpdateData.class, "startX;startY;width;height;mapColors", "startX", "startZ", "width", "height", "colors"}, this, object);
        }
    }
}

