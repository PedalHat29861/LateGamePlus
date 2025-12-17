/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntListIterator
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.math.WordPackedArray;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkPalettedStorageFix
extends DataFix {
    private static final int field_29871 = 128;
    private static final int field_29872 = 64;
    private static final int field_29873 = 32;
    private static final int field_29874 = 16;
    private static final int field_29875 = 8;
    private static final int field_29876 = 4;
    private static final int field_29877 = 2;
    private static final int field_29878 = 1;
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29870 = 4096;

    public ChunkPalettedStorageFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public static String getName(Dynamic<?> dynamic) {
        return dynamic.get("Name").asString("");
    }

    public static String getProperty(Dynamic<?> dynamic, String propertyKey) {
        return dynamic.get("Properties").get(propertyKey).asString("");
    }

    public static int addTo(Int2ObjectBiMap<Dynamic<?>> int2ObjectBiMap, Dynamic<?> dynamic) {
        int i = int2ObjectBiMap.getRawId(dynamic);
        if (i == -1) {
            i = int2ObjectBiMap.add(dynamic);
        }
        return i;
    }

    private Dynamic<?> fixChunk(Dynamic<?> chunkDynamic) {
        Optional optional = chunkDynamic.get("Level").result();
        if (optional.isPresent() && ((Dynamic)optional.get()).get("Sections").asStreamOpt().result().isPresent()) {
            return chunkDynamic.set("Level", new Level((Dynamic)optional.get()).transform());
        }
        return chunkDynamic;
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
        Type type2 = this.getOutputSchema().getType(TypeReferences.CHUNK);
        return this.writeFixAndRead("ChunkPalettedStorageFix", type, type2, this::fixChunk);
    }

    public static int getSideToUpgradeFlag(boolean west, boolean east, boolean north, boolean south) {
        int i = 0;
        if (north) {
            i = east ? (i |= 2) : (west ? (i |= 0x80) : (i |= 1));
        } else if (south) {
            i = west ? (i |= 0x20) : (east ? (i |= 8) : (i |= 0x10));
        } else if (east) {
            i |= 4;
        } else if (west) {
            i |= 0x40;
        }
        return i;
    }

    static final class Level {
        private int sidesToUpgrade;
        private final @Nullable Section[] sections = new Section[16];
        private final Dynamic<?> level;
        private final int x;
        private final int z;
        private final Int2ObjectMap<Dynamic<?>> blockEntities = new Int2ObjectLinkedOpenHashMap(16);

        public Level(Dynamic<?> chunkTag) {
            this.level = chunkTag;
            this.x = chunkTag.get("xPos").asInt(0) << 4;
            this.z = chunkTag.get("zPos").asInt(0) << 4;
            chunkTag.get("TileEntities").asStreamOpt().ifSuccess(stream -> stream.forEach(blockEntityTag -> {
                int k;
                int i = blockEntityTag.get("x").asInt(0) - this.x & 0xF;
                int j = blockEntityTag.get("y").asInt(0);
                int l = j << 8 | (k = blockEntityTag.get("z").asInt(0) - this.z & 0xF) << 4 | i;
                if (this.blockEntities.put(l, blockEntityTag) != null) {
                    LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", new Object[]{this.x, this.z, i, j, k});
                }
            }));
            boolean bl = chunkTag.get("convertedFromAlphaFormat").asBoolean(false);
            chunkTag.get("Sections").asStreamOpt().ifSuccess(stream -> stream.forEach(sectionTag -> {
                Section section = new Section((Dynamic<?>)sectionTag);
                this.sidesToUpgrade = section.visit(this.sidesToUpgrade);
                this.sections[section.y] = section;
            }));
            for (Section section : this.sections) {
                if (section == null) continue;
                block30: for (Int2ObjectMap.Entry entry : section.inPlaceUpdates.int2ObjectEntrySet()) {
                    int i = section.y << 12;
                    switch (entry.getIntKey()) {
                        case 2: {
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlock(j |= i);
                                if (!"minecraft:grass_block".equals(ChunkPalettedStorageFix.getName(dynamic)) || !"minecraft:snow".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(Level.adjacentTo(j, Facing.UP)))) && !"minecraft:snow_layer".equals(string)) continue;
                                this.setBlock(j, Mapping.SNOWY_GRASS_BLOCK_STATE);
                            }
                            continue block30;
                        }
                        case 3: {
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlock(j |= i);
                                if (!"minecraft:podzol".equals(ChunkPalettedStorageFix.getName(dynamic)) || !"minecraft:snow".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(Level.adjacentTo(j, Facing.UP)))) && !"minecraft:snow_layer".equals(string)) continue;
                                this.setBlock(j, Mapping.SNOWY_PODZOL_STATE);
                            }
                            continue block30;
                        }
                        case 110: {
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlock(j |= i);
                                if (!"minecraft:mycelium".equals(ChunkPalettedStorageFix.getName(dynamic)) || !"minecraft:snow".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(Level.adjacentTo(j, Facing.UP)))) && !"minecraft:snow_layer".equals(string)) continue;
                                this.setBlock(j, Mapping.SNOWY_MYCELIUM_STATE);
                            }
                            continue block30;
                        }
                        case 25: {
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.removeBlockEntity(j |= i);
                                if (dynamic == null) continue;
                                string = Boolean.toString(dynamic.get("powered").asBoolean(false)) + (byte)Math.min(Math.max(dynamic.get("note").asInt(0), 0), 24);
                                this.setBlock(j, Mapping.NOTE_BLOCK_IDS_TO_STATES.getOrDefault(string, Mapping.NOTE_BLOCK_IDS_TO_STATES.get("false0")));
                            }
                            continue block30;
                        }
                        case 26: {
                            String string2;
                            Dynamic<?> dynamic2;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                int k;
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlockEntity(j |= i);
                                dynamic2 = this.getBlock(j);
                                if (dynamic == null || (k = dynamic.get("color").asInt(0)) == 14 || k < 0 || k >= 16 || !Mapping.BED_IDS_TO_STATES.containsKey(string2 = ChunkPalettedStorageFix.getProperty(dynamic2, "facing") + ChunkPalettedStorageFix.getProperty(dynamic2, "occupied") + ChunkPalettedStorageFix.getProperty(dynamic2, "part") + k)) continue;
                                this.setBlock(j, Mapping.BED_IDS_TO_STATES.get(string2));
                            }
                            continue block30;
                        }
                        case 176: 
                        case 177: {
                            String string2;
                            Dynamic<?> dynamic2;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                int k;
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlockEntity(j |= i);
                                dynamic2 = this.getBlock(j);
                                if (dynamic == null || (k = dynamic.get("Base").asInt(0)) == 15 || k < 0 || k >= 16 || !Mapping.BANNER_IDS_TO_STATES.containsKey(string2 = ChunkPalettedStorageFix.getProperty(dynamic2, entry.getIntKey() == 176 ? "rotation" : "facing") + "_" + k)) continue;
                                this.setBlock(j, Mapping.BANNER_IDS_TO_STATES.get(string2));
                            }
                            continue block30;
                        }
                        case 86: {
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlock(j |= i);
                                if (!"minecraft:carved_pumpkin".equals(ChunkPalettedStorageFix.getName(dynamic)) || !"minecraft:grass_block".equals(string = ChunkPalettedStorageFix.getName(this.getBlock(Level.adjacentTo(j, Facing.DOWN)))) && !"minecraft:dirt".equals(string)) continue;
                                this.setBlock(j, Mapping.PUMPKIN_STATE);
                            }
                            continue block30;
                        }
                        case 140: {
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.removeBlockEntity(j |= i);
                                if (dynamic == null) continue;
                                string = dynamic.get("Item").asString("") + dynamic.get("Data").asInt(0);
                                this.setBlock(j, Mapping.PLANT_TO_FLOWER_POT_STATES.getOrDefault(string, Mapping.PLANT_TO_FLOWER_POT_STATES.get("minecraft:air0")));
                            }
                            continue block30;
                        }
                        case 144: {
                            String string2;
                            Object string;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlockEntity(j |= i);
                                if (dynamic == null) continue;
                                string = String.valueOf(dynamic.get("SkullType").asInt(0));
                                String string3 = ChunkPalettedStorageFix.getProperty(this.getBlock(j), "facing");
                                string2 = "up".equals(string3) || "down".equals(string3) ? (String)string + dynamic.get("Rot").asInt(0) : (String)string + string3;
                                dynamic.remove("SkullType");
                                dynamic.remove("facing");
                                dynamic.remove("Rot");
                                this.setBlock(j, Mapping.SKULL_IDS_TO_STATES.getOrDefault(string2, Mapping.SKULL_IDS_TO_STATES.get("0north")));
                            }
                            continue block30;
                        }
                        case 64: 
                        case 71: 
                        case 193: 
                        case 194: 
                        case 195: 
                        case 196: 
                        case 197: {
                            Dynamic<?> dynamic2;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlock(j |= i);
                                if (!ChunkPalettedStorageFix.getName(dynamic).endsWith("_door") || !"lower".equals(ChunkPalettedStorageFix.getProperty(dynamic2 = this.getBlock(j), "half"))) continue;
                                int k = Level.adjacentTo(j, Facing.UP);
                                Dynamic<?> dynamic3 = this.getBlock(k);
                                String string4 = ChunkPalettedStorageFix.getName(dynamic2);
                                if (!string4.equals(ChunkPalettedStorageFix.getName(dynamic3))) continue;
                                String string5 = ChunkPalettedStorageFix.getProperty(dynamic2, "facing");
                                String string6 = ChunkPalettedStorageFix.getProperty(dynamic2, "open");
                                String string7 = bl ? "left" : ChunkPalettedStorageFix.getProperty(dynamic3, "hinge");
                                String string8 = bl ? "false" : ChunkPalettedStorageFix.getProperty(dynamic3, "powered");
                                this.setBlock(j, Mapping.DOOR_IDS_TO_STATES.get(string4 + string5 + "lower" + string7 + string6 + string8));
                                this.setBlock(k, Mapping.DOOR_IDS_TO_STATES.get(string4 + string5 + "upper" + string7 + string6 + string8));
                            }
                            continue block30;
                        }
                        case 175: {
                            Dynamic<?> dynamic2;
                            Dynamic<?> dynamic;
                            int j;
                            IntListIterator intListIterator = ((IntList)entry.getValue()).iterator();
                            while (intListIterator.hasNext()) {
                                String string3;
                                j = (Integer)intListIterator.next();
                                dynamic = this.getBlock(j |= i);
                                if (!"upper".equals(ChunkPalettedStorageFix.getProperty(dynamic, "half"))) continue;
                                dynamic2 = this.getBlock(Level.adjacentTo(j, Facing.DOWN));
                                switch (string3 = ChunkPalettedStorageFix.getName(dynamic2)) {
                                    case "minecraft:sunflower": {
                                        this.setBlock(j, Mapping.UPPER_HALF_SUNFLOWER_STATE);
                                        break;
                                    }
                                    case "minecraft:lilac": {
                                        this.setBlock(j, Mapping.UPPER_HALF_LILAC_STATE);
                                        break;
                                    }
                                    case "minecraft:tall_grass": {
                                        this.setBlock(j, Mapping.UPPER_HALF_TALL_GRASS_STATE);
                                        break;
                                    }
                                    case "minecraft:large_fern": {
                                        this.setBlock(j, Mapping.UPPER_HALF_LARGE_FERN_STATE);
                                        break;
                                    }
                                    case "minecraft:rose_bush": {
                                        this.setBlock(j, Mapping.UPPER_HALF_ROSE_BUSH_STATE);
                                        break;
                                    }
                                    case "minecraft:peony": {
                                        this.setBlock(j, Mapping.UPPER_HALF_PEONY_STATE);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        private @Nullable Dynamic<?> getBlockEntity(int packedLocalPos) {
            return (Dynamic)this.blockEntities.get(packedLocalPos);
        }

        private @Nullable Dynamic<?> removeBlockEntity(int packedLocalPos) {
            return (Dynamic)this.blockEntities.remove(packedLocalPos);
        }

        public static int adjacentTo(int packedLocalPos, Facing direction) {
            return switch (direction.getAxis().ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    int i = (packedLocalPos & 0xF) + direction.getDirection().getOffset();
                    if (i < 0 || i > 15) {
                        yield -1;
                    }
                    yield packedLocalPos & 0xFFFFFFF0 | i;
                }
                case 1 -> {
                    int i = (packedLocalPos >> 8) + direction.getDirection().getOffset();
                    if (i < 0 || i > 255) {
                        yield -1;
                    }
                    yield packedLocalPos & 0xFF | i << 8;
                }
                case 2 -> {
                    int i = (packedLocalPos >> 4 & 0xF) + direction.getDirection().getOffset();
                    if (i < 0 || i > 15) {
                        yield -1;
                    }
                    yield packedLocalPos & 0xFFFFFF0F | i << 4;
                }
            };
        }

        private void setBlock(int packedLocalPos, Dynamic<?> dynamic) {
            if (packedLocalPos < 0 || packedLocalPos > 65535) {
                return;
            }
            Section section = this.getSection(packedLocalPos);
            if (section == null) {
                return;
            }
            section.setBlock(packedLocalPos & 0xFFF, dynamic);
        }

        private @Nullable Section getSection(int packedLocalPos) {
            int i = packedLocalPos >> 12;
            return i < this.sections.length ? this.sections[i] : null;
        }

        public Dynamic<?> getBlock(int packedLocalPos) {
            if (packedLocalPos < 0 || packedLocalPos > 65535) {
                return Mapping.AIR_STATE;
            }
            Section section = this.getSection(packedLocalPos);
            if (section == null) {
                return Mapping.AIR_STATE;
            }
            return section.getBlock(packedLocalPos & 0xFFF);
        }

        public Dynamic<?> transform() {
            Dynamic dynamic = this.level;
            dynamic = this.blockEntities.isEmpty() ? dynamic.remove("TileEntities") : dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
            Dynamic dynamic2 = dynamic.emptyMap();
            ArrayList list = Lists.newArrayList();
            for (Section section : this.sections) {
                if (section == null) continue;
                list.add(section.transform());
                dynamic2 = dynamic2.set(String.valueOf(section.y), dynamic2.createIntList(Arrays.stream(section.innerPositions.toIntArray())));
            }
            Dynamic dynamic3 = dynamic.emptyMap();
            dynamic3 = dynamic3.set("Sides", dynamic3.createByte((byte)this.sidesToUpgrade));
            dynamic3 = dynamic3.set("Indices", dynamic2);
            return dynamic.set("UpgradeData", dynamic3).set("Sections", dynamic3.createList(list.stream()));
        }
    }

    public static final class Facing
    extends Enum<Facing> {
        public static final /* enum */ Facing DOWN = new Facing(Direction.NEGATIVE, Axis.Y);
        public static final /* enum */ Facing UP = new Facing(Direction.POSITIVE, Axis.Y);
        public static final /* enum */ Facing NORTH = new Facing(Direction.NEGATIVE, Axis.Z);
        public static final /* enum */ Facing SOUTH = new Facing(Direction.POSITIVE, Axis.Z);
        public static final /* enum */ Facing WEST = new Facing(Direction.NEGATIVE, Axis.X);
        public static final /* enum */ Facing EAST = new Facing(Direction.POSITIVE, Axis.X);
        private final Axis axis;
        private final Direction direction;
        private static final /* synthetic */ Facing[] field_15865;

        public static Facing[] values() {
            return (Facing[])field_15865.clone();
        }

        public static Facing valueOf(String string) {
            return Enum.valueOf(Facing.class, string);
        }

        private Facing(Direction direction, Axis axis) {
            this.axis = axis;
            this.direction = direction;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public Axis getAxis() {
            return this.axis;
        }

        private static /* synthetic */ Facing[] method_36590() {
            return new Facing[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
        }

        static {
            field_15865 = Facing.method_36590();
        }

        public static final class Axis
        extends Enum<Axis> {
            public static final /* enum */ Axis X = new Axis();
            public static final /* enum */ Axis Y = new Axis();
            public static final /* enum */ Axis Z = new Axis();
            private static final /* synthetic */ Axis[] field_15868;

            public static Axis[] values() {
                return (Axis[])field_15868.clone();
            }

            public static Axis valueOf(String string) {
                return Enum.valueOf(Axis.class, string);
            }

            private static /* synthetic */ Axis[] method_36591() {
                return new Axis[]{X, Y, Z};
            }

            static {
                field_15868 = Axis.method_36591();
            }
        }

        public static final class Direction
        extends Enum<Direction> {
            public static final /* enum */ Direction POSITIVE = new Direction(1);
            public static final /* enum */ Direction NEGATIVE = new Direction(-1);
            private final int offset;
            private static final /* synthetic */ Direction[] field_15871;

            public static Direction[] values() {
                return (Direction[])field_15871.clone();
            }

            public static Direction valueOf(String string) {
                return Enum.valueOf(Direction.class, string);
            }

            private Direction(int offset) {
                this.offset = offset;
            }

            public int getOffset() {
                return this.offset;
            }

            private static /* synthetic */ Direction[] method_36592() {
                return new Direction[]{POSITIVE, NEGATIVE};
            }

            static {
                field_15871 = Direction.method_36592();
            }
        }
    }

    static class ChunkNibbleArray {
        private static final int CONTENTS_LENGTH = 2048;
        private static final int field_29880 = 4;
        private final byte[] contents;

        public ChunkNibbleArray() {
            this.contents = new byte[2048];
        }

        public ChunkNibbleArray(byte[] contents) {
            this.contents = contents;
            if (contents.length != 2048) {
                throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + contents.length);
            }
        }

        public int get(int x, int y, int z) {
            int i = this.getRawIndex(y << 8 | z << 4 | x);
            if (this.usesLowNibble(y << 8 | z << 4 | x)) {
                return this.contents[i] & 0xF;
            }
            return this.contents[i] >> 4 & 0xF;
        }

        private boolean usesLowNibble(int index) {
            return (index & 1) == 0;
        }

        private int getRawIndex(int index) {
            return index >> 1;
        }
    }

    static class Section {
        private final Int2ObjectBiMap<Dynamic<?>> paletteMap = Int2ObjectBiMap.create(32);
        private final List<Dynamic<?>> paletteData;
        private final Dynamic<?> section;
        private final boolean hasBlocks;
        final Int2ObjectMap<IntList> inPlaceUpdates = new Int2ObjectLinkedOpenHashMap();
        final IntList innerPositions = new IntArrayList();
        public final int y;
        private final Set<Dynamic<?>> seenStates = Sets.newIdentityHashSet();
        private final int[] states = new int[4096];

        public Section(Dynamic<?> section) {
            this.paletteData = Lists.newArrayList();
            this.section = section;
            this.y = section.get("Y").asInt(0);
            this.hasBlocks = section.get("Blocks").result().isPresent();
        }

        public Dynamic<?> getBlock(int index) {
            if (index < 0 || index > 4095) {
                return Mapping.AIR_STATE;
            }
            Dynamic<?> dynamic = this.paletteMap.get(this.states[index]);
            return dynamic == null ? Mapping.AIR_STATE : dynamic;
        }

        public void setBlock(int pos, Dynamic<?> dynamic) {
            if (this.seenStates.add(dynamic)) {
                this.paletteData.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(dynamic)) ? Mapping.AIR_STATE : dynamic);
            }
            this.states[pos] = ChunkPalettedStorageFix.addTo(this.paletteMap, dynamic);
        }

        public int visit(int sidesToUpgrade) {
            if (!this.hasBlocks) {
                return sidesToUpgrade;
            }
            ByteBuffer byteBuffer2 = (ByteBuffer)this.section.get("Blocks").asByteBufferOpt().result().get();
            ChunkNibbleArray chunkNibbleArray = this.section.get("Data").asByteBufferOpt().map(byteBuffer -> new ChunkNibbleArray(DataFixUtils.toArray((ByteBuffer)byteBuffer))).result().orElseGet(ChunkNibbleArray::new);
            ChunkNibbleArray chunkNibbleArray2 = this.section.get("Add").asByteBufferOpt().map(byteBuffer -> new ChunkNibbleArray(DataFixUtils.toArray((ByteBuffer)byteBuffer))).result().orElseGet(ChunkNibbleArray::new);
            this.seenStates.add(Mapping.AIR_STATE);
            ChunkPalettedStorageFix.addTo(this.paletteMap, Mapping.AIR_STATE);
            this.paletteData.add(Mapping.AIR_STATE);
            for (int i = 0; i < 4096; ++i) {
                int j = i & 0xF;
                int k = i >> 8 & 0xF;
                int l = i >> 4 & 0xF;
                int m = chunkNibbleArray2.get(j, k, l) << 12 | (byteBuffer2.get(i) & 0xFF) << 4 | chunkNibbleArray.get(j, k, l);
                if (Mapping.field_52402.get(m >> 4)) {
                    this.addInPlaceUpdate(m >> 4, i);
                }
                if (Mapping.field_52401.get(m >> 4)) {
                    int n = ChunkPalettedStorageFix.getSideToUpgradeFlag(j == 0, j == 15, l == 0, l == 15);
                    if (n == 0) {
                        this.innerPositions.add(i);
                    } else {
                        sidesToUpgrade |= n;
                    }
                }
                this.setBlock(i, BlockStateFlattening.lookupState(m));
            }
            return sidesToUpgrade;
        }

        private void addInPlaceUpdate(int section, int index) {
            IntList intList = (IntList)this.inPlaceUpdates.get(section);
            if (intList == null) {
                intList = new IntArrayList();
                this.inPlaceUpdates.put(section, (Object)intList);
            }
            intList.add(index);
        }

        public Dynamic<?> transform() {
            Dynamic dynamic = this.section;
            if (!this.hasBlocks) {
                return dynamic;
            }
            dynamic = dynamic.set("Palette", dynamic.createList(this.paletteData.stream()));
            int i = Math.max(4, DataFixUtils.ceillog2((int)this.seenStates.size()));
            WordPackedArray wordPackedArray = new WordPackedArray(i, 4096);
            for (int j = 0; j < this.states.length; ++j) {
                wordPackedArray.set(j, this.states[j]);
            }
            dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(wordPackedArray.getAlignedArray())));
            dynamic = dynamic.remove("Blocks");
            dynamic = dynamic.remove("Data");
            dynamic = dynamic.remove("Add");
            return dynamic;
        }
    }

    static class Mapping {
        static final BitSet field_52401 = new BitSet(256);
        static final BitSet field_52402 = new BitSet(256);
        static final Dynamic<?> PUMPKIN_STATE = FixUtil.createBlockState("minecraft:pumpkin");
        static final Dynamic<?> SNOWY_PODZOL_STATE = FixUtil.createBlockState("minecraft:podzol", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_GRASS_BLOCK_STATE = FixUtil.createBlockState("minecraft:grass_block", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_MYCELIUM_STATE = FixUtil.createBlockState("minecraft:mycelium", Map.of("snowy", "true"));
        static final Dynamic<?> UPPER_HALF_SUNFLOWER_STATE = FixUtil.createBlockState("minecraft:sunflower", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_HALF_LILAC_STATE = FixUtil.createBlockState("minecraft:lilac", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_HALF_TALL_GRASS_STATE = FixUtil.createBlockState("minecraft:tall_grass", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_HALF_LARGE_FERN_STATE = FixUtil.createBlockState("minecraft:large_fern", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_HALF_ROSE_BUSH_STATE = FixUtil.createBlockState("minecraft:rose_bush", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_HALF_PEONY_STATE = FixUtil.createBlockState("minecraft:peony", Map.of("half", "upper"));
        static final Map<String, Dynamic<?>> PLANT_TO_FLOWER_POT_STATES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
            map.put("minecraft:air0", FixUtil.createBlockState("minecraft:flower_pot"));
            map.put("minecraft:red_flower0", FixUtil.createBlockState("minecraft:potted_poppy"));
            map.put("minecraft:red_flower1", FixUtil.createBlockState("minecraft:potted_blue_orchid"));
            map.put("minecraft:red_flower2", FixUtil.createBlockState("minecraft:potted_allium"));
            map.put("minecraft:red_flower3", FixUtil.createBlockState("minecraft:potted_azure_bluet"));
            map.put("minecraft:red_flower4", FixUtil.createBlockState("minecraft:potted_red_tulip"));
            map.put("minecraft:red_flower5", FixUtil.createBlockState("minecraft:potted_orange_tulip"));
            map.put("minecraft:red_flower6", FixUtil.createBlockState("minecraft:potted_white_tulip"));
            map.put("minecraft:red_flower7", FixUtil.createBlockState("minecraft:potted_pink_tulip"));
            map.put("minecraft:red_flower8", FixUtil.createBlockState("minecraft:potted_oxeye_daisy"));
            map.put("minecraft:yellow_flower0", FixUtil.createBlockState("minecraft:potted_dandelion"));
            map.put("minecraft:sapling0", FixUtil.createBlockState("minecraft:potted_oak_sapling"));
            map.put("minecraft:sapling1", FixUtil.createBlockState("minecraft:potted_spruce_sapling"));
            map.put("minecraft:sapling2", FixUtil.createBlockState("minecraft:potted_birch_sapling"));
            map.put("minecraft:sapling3", FixUtil.createBlockState("minecraft:potted_jungle_sapling"));
            map.put("minecraft:sapling4", FixUtil.createBlockState("minecraft:potted_acacia_sapling"));
            map.put("minecraft:sapling5", FixUtil.createBlockState("minecraft:potted_dark_oak_sapling"));
            map.put("minecraft:red_mushroom0", FixUtil.createBlockState("minecraft:potted_red_mushroom"));
            map.put("minecraft:brown_mushroom0", FixUtil.createBlockState("minecraft:potted_brown_mushroom"));
            map.put("minecraft:deadbush0", FixUtil.createBlockState("minecraft:potted_dead_bush"));
            map.put("minecraft:tallgrass2", FixUtil.createBlockState("minecraft:potted_fern"));
            map.put("minecraft:cactus0", FixUtil.createBlockState("minecraft:potted_cactus"));
        });
        static final Map<String, Dynamic<?>> SKULL_IDS_TO_STATES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
            Mapping.skull(map, 0, "skeleton", "skull");
            Mapping.skull(map, 1, "wither_skeleton", "skull");
            Mapping.skull(map, 2, "zombie", "head");
            Mapping.skull(map, 3, "player", "head");
            Mapping.skull(map, 4, "creeper", "head");
            Mapping.skull(map, 5, "dragon", "head");
        });
        static final Map<String, Dynamic<?>> DOOR_IDS_TO_STATES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
            Mapping.door(map, "oak_door");
            Mapping.door(map, "iron_door");
            Mapping.door(map, "spruce_door");
            Mapping.door(map, "birch_door");
            Mapping.door(map, "jungle_door");
            Mapping.door(map, "acacia_door");
            Mapping.door(map, "dark_oak_door");
        });
        static final Map<String, Dynamic<?>> NOTE_BLOCK_IDS_TO_STATES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
            for (int i = 0; i < 26; ++i) {
                map.put("true" + i, FixUtil.createBlockState("minecraft:note_block", Map.of("powered", "true", "note", String.valueOf(i))));
                map.put("false" + i, FixUtil.createBlockState("minecraft:note_block", Map.of("powered", "false", "note", String.valueOf(i))));
            }
        });
        private static final Int2ObjectMap<String> COLORS_BY_IDS = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), map -> {
            map.put(0, (Object)"white");
            map.put(1, (Object)"orange");
            map.put(2, (Object)"magenta");
            map.put(3, (Object)"light_blue");
            map.put(4, (Object)"yellow");
            map.put(5, (Object)"lime");
            map.put(6, (Object)"pink");
            map.put(7, (Object)"gray");
            map.put(8, (Object)"light_gray");
            map.put(9, (Object)"cyan");
            map.put(10, (Object)"purple");
            map.put(11, (Object)"blue");
            map.put(12, (Object)"brown");
            map.put(13, (Object)"green");
            map.put(14, (Object)"red");
            map.put(15, (Object)"black");
        });
        static final Map<String, Dynamic<?>> BED_IDS_TO_STATES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
            for (Int2ObjectMap.Entry entry : COLORS_BY_IDS.int2ObjectEntrySet()) {
                if (Objects.equals(entry.getValue(), "red")) continue;
                Mapping.bed(map, entry.getIntKey(), (String)entry.getValue());
            }
        });
        static final Map<String, Dynamic<?>> BANNER_IDS_TO_STATES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
            for (Int2ObjectMap.Entry entry : COLORS_BY_IDS.int2ObjectEntrySet()) {
                if (Objects.equals(entry.getValue(), "white")) continue;
                Mapping.banner(map, 15 - entry.getIntKey(), (String)entry.getValue());
            }
        });
        static final Dynamic<?> AIR_STATE;

        private Mapping() {
        }

        private static void skull(Map<String, Dynamic<?>> map, int id, String entity, String type) {
            map.put(id + "north", FixUtil.createBlockState("minecraft:" + entity + "_wall_" + type, Map.of("facing", "north")));
            map.put(id + "east", FixUtil.createBlockState("minecraft:" + entity + "_wall_" + type, Map.of("facing", "east")));
            map.put(id + "south", FixUtil.createBlockState("minecraft:" + entity + "_wall_" + type, Map.of("facing", "south")));
            map.put(id + "west", FixUtil.createBlockState("minecraft:" + entity + "_wall_" + type, Map.of("facing", "west")));
            for (int i = 0; i < 16; ++i) {
                map.put("" + id + i, FixUtil.createBlockState("minecraft:" + entity + "_" + type, Map.of("rotation", String.valueOf(i))));
            }
        }

        private static void door(Map<String, Dynamic<?>> map, String id) {
            String string = "minecraft:" + id;
            map.put("minecraft:" + id + "eastlowerleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "eastlowerleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "eastlowerlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "eastlowerlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "eastlowerrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "eastlowerrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "eastlowerrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "eastlowerrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "eastupperleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "eastupperleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "eastupperlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "eastupperlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "eastupperrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "eastupperrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "eastupperrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "eastupperrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "northlowerleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "northlowerleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "northlowerlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "northlowerlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "northlowerrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "northlowerrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "northlowerrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "northlowerrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "northupperleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "northupperleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "northupperlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "northupperlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "northupperrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "northupperrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "northupperrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "northupperrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "southlowerleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "southlowerleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "southlowerlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "southlowerlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "southlowerrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "southlowerrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "southlowerrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "southlowerrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "southupperleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "southupperleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "southupperlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "southupperlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "southupperrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "southupperrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "southupperrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "southupperrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "westlowerleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "westlowerleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "westlowerlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "westlowerlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "westlowerrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "westlowerrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "westlowerrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "westlowerrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "westupperleftfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "westupperleftfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "westupperlefttruefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "westupperlefttruetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + id + "westupperrightfalsefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + id + "westupperrightfalsetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + id + "westupperrighttruefalse", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + id + "westupperrighttruetrue", FixUtil.createBlockState(string, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
        }

        private static void bed(Map<String, Dynamic<?>> map, int id, String color) {
            map.put("southfalsefoot" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "south", "occupied", "false", "part", "foot")));
            map.put("westfalsefoot" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "west", "occupied", "false", "part", "foot")));
            map.put("northfalsefoot" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "north", "occupied", "false", "part", "foot")));
            map.put("eastfalsefoot" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "east", "occupied", "false", "part", "foot")));
            map.put("southfalsehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "south", "occupied", "false", "part", "head")));
            map.put("westfalsehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "west", "occupied", "false", "part", "head")));
            map.put("northfalsehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "north", "occupied", "false", "part", "head")));
            map.put("eastfalsehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "east", "occupied", "false", "part", "head")));
            map.put("southtruehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "south", "occupied", "true", "part", "head")));
            map.put("westtruehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "west", "occupied", "true", "part", "head")));
            map.put("northtruehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "north", "occupied", "true", "part", "head")));
            map.put("easttruehead" + id, FixUtil.createBlockState("minecraft:" + color + "_bed", Map.of("facing", "east", "occupied", "true", "part", "head")));
        }

        private static void banner(Map<String, Dynamic<?>> map, int id, String color) {
            for (int i = 0; i < 16; ++i) {
                map.put(i + "_" + id, FixUtil.createBlockState("minecraft:" + color + "_banner", Map.of("rotation", String.valueOf(i))));
            }
            map.put("north_" + id, FixUtil.createBlockState("minecraft:" + color + "_wall_banner", Map.of("facing", "north")));
            map.put("south_" + id, FixUtil.createBlockState("minecraft:" + color + "_wall_banner", Map.of("facing", "south")));
            map.put("west_" + id, FixUtil.createBlockState("minecraft:" + color + "_wall_banner", Map.of("facing", "west")));
            map.put("east_" + id, FixUtil.createBlockState("minecraft:" + color + "_wall_banner", Map.of("facing", "east")));
        }

        static {
            field_52402.set(2);
            field_52402.set(3);
            field_52402.set(110);
            field_52402.set(140);
            field_52402.set(144);
            field_52402.set(25);
            field_52402.set(86);
            field_52402.set(26);
            field_52402.set(176);
            field_52402.set(177);
            field_52402.set(175);
            field_52402.set(64);
            field_52402.set(71);
            field_52402.set(193);
            field_52402.set(194);
            field_52402.set(195);
            field_52402.set(196);
            field_52402.set(197);
            field_52401.set(54);
            field_52401.set(146);
            field_52401.set(25);
            field_52401.set(26);
            field_52401.set(51);
            field_52401.set(53);
            field_52401.set(67);
            field_52401.set(108);
            field_52401.set(109);
            field_52401.set(114);
            field_52401.set(128);
            field_52401.set(134);
            field_52401.set(135);
            field_52401.set(136);
            field_52401.set(156);
            field_52401.set(163);
            field_52401.set(164);
            field_52401.set(180);
            field_52401.set(203);
            field_52401.set(55);
            field_52401.set(85);
            field_52401.set(113);
            field_52401.set(188);
            field_52401.set(189);
            field_52401.set(190);
            field_52401.set(191);
            field_52401.set(192);
            field_52401.set(93);
            field_52401.set(94);
            field_52401.set(101);
            field_52401.set(102);
            field_52401.set(160);
            field_52401.set(106);
            field_52401.set(107);
            field_52401.set(183);
            field_52401.set(184);
            field_52401.set(185);
            field_52401.set(186);
            field_52401.set(187);
            field_52401.set(132);
            field_52401.set(139);
            field_52401.set(199);
            AIR_STATE = FixUtil.createBlockState("minecraft:air");
        }
    }
}

