/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.LinkedHashMultiset
 *  com.google.common.collect.Multiset
 *  com.google.common.collect.Multisets
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.jspecify.annotations.Nullable;

public class FilledMapItem
extends Item {
    public static final int field_30907 = 128;
    public static final int field_30908 = 128;

    public FilledMapItem(Item.Settings settings) {
        super(settings);
    }

    public static ItemStack createMap(ServerWorld world, int x, int z, byte scale, boolean showIcons, boolean unlimitedTracking) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapIdComponent mapIdComponent = FilledMapItem.allocateMapId(world, x, z, scale, showIcons, unlimitedTracking, world.getRegistryKey());
        itemStack.set(DataComponentTypes.MAP_ID, mapIdComponent);
        return itemStack;
    }

    public static @Nullable MapState getMapState(@Nullable MapIdComponent id, World world) {
        return id == null ? null : world.getMapState(id);
    }

    public static @Nullable MapState getMapState(ItemStack map, World world) {
        MapIdComponent mapIdComponent = map.get(DataComponentTypes.MAP_ID);
        return FilledMapItem.getMapState(mapIdComponent, world);
    }

    private static MapIdComponent allocateMapId(ServerWorld world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
        MapState mapState = MapState.of(x, z, (byte)scale, showIcons, unlimitedTracking, dimension);
        MapIdComponent mapIdComponent = world.increaseAndGetMapId();
        world.putMapState(mapIdComponent, mapState);
        return mapIdComponent;
    }

    public void updateColors(World world, Entity entity, MapState state) {
        if (world.getRegistryKey() != state.dimension || !(entity instanceof PlayerEntity)) {
            return;
        }
        int i = 1 << state.scale;
        int j = state.centerX;
        int k = state.centerZ;
        int l = MathHelper.floor(entity.getX() - (double)j) / i + 64;
        int m = MathHelper.floor(entity.getZ() - (double)k) / i + 64;
        int n = 128 / i;
        if (world.getDimension().hasCeiling()) {
            n /= 2;
        }
        MapState.PlayerUpdateTracker playerUpdateTracker = state.getPlayerSyncData((PlayerEntity)entity);
        ++playerUpdateTracker.field_131;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos.Mutable mutable2 = new BlockPos.Mutable();
        boolean bl = false;
        for (int o = l - n + 1; o < l + n; ++o) {
            if ((o & 0xF) != (playerUpdateTracker.field_131 & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int p = m - n - 1; p < m + n; ++p) {
                double f;
                if (o < 0 || p < -1 || o >= 128 || p >= 128) continue;
                int q = MathHelper.square(o - l) + MathHelper.square(p - m);
                boolean bl2 = q > (n - 2) * (n - 2);
                int r = (j / i + o - 64) * i;
                int s = (k / i + p - 64) * i;
                LinkedHashMultiset multiset = LinkedHashMultiset.create();
                WorldChunk worldChunk = world.getChunk(ChunkSectionPos.getSectionCoord(r), ChunkSectionPos.getSectionCoord(s));
                if (worldChunk.isEmpty()) continue;
                int t = 0;
                double e = 0.0;
                if (world.getDimension().hasCeiling()) {
                    u = r + s * 231871;
                    if (((u = u * u * 31287121 + u * 11) >> 20 & 1) == 0) {
                        multiset.add((Object)Blocks.DIRT.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 10);
                    } else {
                        multiset.add((Object)Blocks.STONE.getDefaultState().getMapColor(world, BlockPos.ORIGIN), 100);
                    }
                    e = 100.0;
                } else {
                    for (u = 0; u < i; ++u) {
                        for (int v = 0; v < i; ++v) {
                            BlockState blockState;
                            mutable.set(r + u, 0, s + v);
                            int w = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, mutable.getX(), mutable.getZ()) + 1;
                            if (w > world.getBottomY()) {
                                do {
                                    mutable.setY(--w);
                                } while ((blockState = worldChunk.getBlockState(mutable)).getMapColor(world, mutable) == MapColor.CLEAR && w > world.getBottomY());
                                if (w > world.getBottomY() && !blockState.getFluidState().isEmpty()) {
                                    BlockState blockState2;
                                    int x = w - 1;
                                    mutable2.set(mutable);
                                    do {
                                        mutable2.setY(x--);
                                        blockState2 = worldChunk.getBlockState(mutable2);
                                        ++t;
                                    } while (x > world.getBottomY() && !blockState2.getFluidState().isEmpty());
                                    blockState = this.getFluidStateIfVisible(world, blockState, mutable);
                                }
                            } else {
                                blockState = Blocks.BEDROCK.getDefaultState();
                            }
                            state.removeBanner(world, mutable.getX(), mutable.getZ());
                            e += (double)w / (double)(i * i);
                            multiset.add((Object)blockState.getMapColor(world, mutable));
                        }
                    }
                }
                MapColor mapColor = (MapColor)Iterables.getFirst((Iterable)Multisets.copyHighestCountFirst((Multiset)multiset), (Object)MapColor.CLEAR);
                MapColor.Brightness brightness = mapColor == MapColor.WATER_BLUE ? ((f = (double)(t /= i * i) * 0.1 + (double)(o + p & 1) * 0.2) < 0.5 ? MapColor.Brightness.HIGH : (f > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL)) : ((f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4) > 0.6 ? MapColor.Brightness.HIGH : (f < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL));
                d = e;
                if (p < 0 || q >= n * n || bl2 && (o + p & 1) == 0) continue;
                bl |= state.putColor(o, p, mapColor.getRenderColorByte(brightness));
            }
        }
    }

    private BlockState getFluidStateIfVisible(World world, BlockState state, BlockPos pos) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && !state.isSideSolidFullSquare(world, pos, Direction.UP)) {
            return fluidState.getBlockState();
        }
        return state;
    }

    private static boolean isAquaticBiome(boolean[] biomes, int x, int z) {
        return biomes[z * 128 + x];
    }

    public static void fillExplorationMap(ServerWorld world, ItemStack map) {
        int o;
        int n;
        MapState mapState = FilledMapItem.getMapState(map, (World)world);
        if (mapState == null) {
            return;
        }
        if (world.getRegistryKey() != mapState.dimension) {
            return;
        }
        int i = 1 << mapState.scale;
        int j = mapState.centerX;
        int k = mapState.centerZ;
        boolean[] bls = new boolean[16384];
        int l = j / i - 64;
        int m = k / i - 64;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (n = 0; n < 128; ++n) {
            for (o = 0; o < 128; ++o) {
                RegistryEntry<Biome> registryEntry = world.getBiome(mutable.set((l + o) * i, 0, (m + n) * i));
                bls[n * 128 + o] = registryEntry.isIn(BiomeTags.WATER_ON_MAP_OUTLINES);
            }
        }
        for (n = 1; n < 127; ++n) {
            for (o = 1; o < 127; ++o) {
                int p = 0;
                for (int q = -1; q < 2; ++q) {
                    for (int r = -1; r < 2; ++r) {
                        if (q == 0 && r == 0 || !FilledMapItem.isAquaticBiome(bls, n + q, o + r)) continue;
                        ++p;
                    }
                }
                MapColor.Brightness brightness = MapColor.Brightness.LOWEST;
                MapColor mapColor = MapColor.CLEAR;
                if (FilledMapItem.isAquaticBiome(bls, n, o)) {
                    mapColor = MapColor.ORANGE;
                    if (p > 7 && o % 2 == 0) {
                        switch ((n + (int)(MathHelper.sin((float)o + 0.0f) * 7.0f)) / 8 % 5) {
                            case 0: 
                            case 4: {
                                brightness = MapColor.Brightness.LOW;
                                break;
                            }
                            case 1: 
                            case 3: {
                                brightness = MapColor.Brightness.NORMAL;
                                break;
                            }
                            case 2: {
                                brightness = MapColor.Brightness.HIGH;
                            }
                        }
                    } else if (p > 7) {
                        mapColor = MapColor.CLEAR;
                    } else if (p > 5) {
                        brightness = MapColor.Brightness.NORMAL;
                    } else if (p > 3) {
                        brightness = MapColor.Brightness.LOW;
                    } else if (p > 1) {
                        brightness = MapColor.Brightness.LOW;
                    }
                } else if (p > 0) {
                    mapColor = MapColor.BROWN;
                    brightness = p > 3 ? MapColor.Brightness.NORMAL : MapColor.Brightness.LOWEST;
                }
                if (mapColor == MapColor.CLEAR) continue;
                mapState.setColor(n, o, mapColor.getRenderColorByte(brightness));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        MapState mapState = FilledMapItem.getMapState(stack, (World)world);
        if (mapState == null) {
            return;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            mapState.update(playerEntity, stack);
        }
        if (!mapState.locked && slot != null && slot.getType() == EquipmentSlot.Type.HAND) {
            this.updateColors(world, entity, mapState);
        }
    }

    @Override
    public void onCraft(ItemStack stack, World world) {
        MapPostProcessingComponent mapPostProcessingComponent = stack.remove(DataComponentTypes.MAP_POST_PROCESSING);
        if (mapPostProcessingComponent == null) {
            return;
        }
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            switch (mapPostProcessingComponent) {
                case LOCK: {
                    FilledMapItem.copyMap(stack, serverWorld);
                    break;
                }
                case SCALE: {
                    FilledMapItem.scale(stack, serverWorld);
                }
            }
        }
    }

    private static void scale(ItemStack map, ServerWorld world) {
        MapState mapState = FilledMapItem.getMapState(map, (World)world);
        if (mapState != null) {
            MapIdComponent mapIdComponent = world.increaseAndGetMapId();
            world.putMapState(mapIdComponent, mapState.zoomOut());
            map.set(DataComponentTypes.MAP_ID, mapIdComponent);
        }
    }

    private static void copyMap(ItemStack stack, ServerWorld world) {
        MapState mapState = FilledMapItem.getMapState(stack, (World)world);
        if (mapState != null) {
            MapIdComponent mapIdComponent = world.increaseAndGetMapId();
            MapState mapState2 = mapState.copy();
            world.putMapState(mapIdComponent, mapState2);
            stack.set(DataComponentTypes.MAP_ID, mapIdComponent);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
        if (blockState.isIn(BlockTags.BANNERS)) {
            MapState mapState;
            if (!context.getWorld().isClient() && (mapState = FilledMapItem.getMapState(context.getStack(), context.getWorld())) != null && !mapState.addBanner(context.getWorld(), context.getBlockPos())) {
                return ActionResult.FAIL;
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }
}

