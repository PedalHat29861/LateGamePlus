/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class SpawnGroup
extends Enum<SpawnGroup>
implements StringIdentifiable {
    public static final /* enum */ SpawnGroup MONSTER = new SpawnGroup("monster", 70, false, false, 128);
    public static final /* enum */ SpawnGroup CREATURE = new SpawnGroup("creature", 10, true, true, 128);
    public static final /* enum */ SpawnGroup AMBIENT = new SpawnGroup("ambient", 15, true, false, 128);
    public static final /* enum */ SpawnGroup AXOLOTLS = new SpawnGroup("axolotls", 5, true, false, 128);
    public static final /* enum */ SpawnGroup UNDERGROUND_WATER_CREATURE = new SpawnGroup("underground_water_creature", 5, true, false, 128);
    public static final /* enum */ SpawnGroup WATER_CREATURE = new SpawnGroup("water_creature", 5, true, false, 128);
    public static final /* enum */ SpawnGroup WATER_AMBIENT = new SpawnGroup("water_ambient", 20, true, false, 64);
    public static final /* enum */ SpawnGroup MISC = new SpawnGroup("misc", -1, true, true, 128);
    public static final Codec<SpawnGroup> CODEC;
    private final int capacity;
    private final boolean peaceful;
    private final boolean rare;
    private final String name;
    private final int despawnStartRange = 32;
    private final int immediateDespawnRange;
    private static final /* synthetic */ SpawnGroup[] field_6301;

    public static SpawnGroup[] values() {
        return (SpawnGroup[])field_6301.clone();
    }

    public static SpawnGroup valueOf(String string) {
        return Enum.valueOf(SpawnGroup.class, string);
    }

    private SpawnGroup(String name, int spawnCap, boolean peaceful, boolean rare, int immediateDespawnRange) {
        this.name = name;
        this.capacity = spawnCap;
        this.peaceful = peaceful;
        this.rare = rare;
        this.immediateDespawnRange = immediateDespawnRange;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isPeaceful() {
        return this.peaceful;
    }

    public boolean isRare() {
        return this.rare;
    }

    public int getImmediateDespawnRange() {
        return this.immediateDespawnRange;
    }

    public int getDespawnStartRange() {
        return 32;
    }

    private static /* synthetic */ SpawnGroup[] method_36609() {
        return new SpawnGroup[]{MONSTER, CREATURE, AMBIENT, AXOLOTLS, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, WATER_AMBIENT, MISC};
    }

    static {
        field_6301 = SpawnGroup.method_36609();
        CODEC = StringIdentifiable.createCodec(SpawnGroup::values);
    }
}

