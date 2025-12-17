/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

public final class SpawnReason
extends Enum<SpawnReason> {
    public static final /* enum */ SpawnReason NATURAL = new SpawnReason();
    public static final /* enum */ SpawnReason CHUNK_GENERATION = new SpawnReason();
    public static final /* enum */ SpawnReason SPAWNER = new SpawnReason();
    public static final /* enum */ SpawnReason STRUCTURE = new SpawnReason();
    public static final /* enum */ SpawnReason BREEDING = new SpawnReason();
    public static final /* enum */ SpawnReason MOB_SUMMONED = new SpawnReason();
    public static final /* enum */ SpawnReason JOCKEY = new SpawnReason();
    public static final /* enum */ SpawnReason EVENT = new SpawnReason();
    public static final /* enum */ SpawnReason CONVERSION = new SpawnReason();
    public static final /* enum */ SpawnReason REINFORCEMENT = new SpawnReason();
    public static final /* enum */ SpawnReason TRIGGERED = new SpawnReason();
    public static final /* enum */ SpawnReason BUCKET = new SpawnReason();
    public static final /* enum */ SpawnReason SPAWN_ITEM_USE = new SpawnReason();
    public static final /* enum */ SpawnReason COMMAND = new SpawnReason();
    public static final /* enum */ SpawnReason DISPENSER = new SpawnReason();
    public static final /* enum */ SpawnReason PATROL = new SpawnReason();
    public static final /* enum */ SpawnReason TRIAL_SPAWNER = new SpawnReason();
    public static final /* enum */ SpawnReason LOAD = new SpawnReason();
    public static final /* enum */ SpawnReason DIMENSION_TRAVEL = new SpawnReason();
    private static final /* synthetic */ SpawnReason[] field_16464;

    public static SpawnReason[] values() {
        return (SpawnReason[])field_16464.clone();
    }

    public static SpawnReason valueOf(String string) {
        return Enum.valueOf(SpawnReason.class, string);
    }

    public static boolean isAnySpawner(SpawnReason reason) {
        return reason == SPAWNER || reason == TRIAL_SPAWNER;
    }

    public static boolean isTrialSpawner(SpawnReason reason) {
        return reason == TRIAL_SPAWNER;
    }

    private static /* synthetic */ SpawnReason[] method_36610() {
        return new SpawnReason[]{NATURAL, CHUNK_GENERATION, SPAWNER, STRUCTURE, BREEDING, MOB_SUMMONED, JOCKEY, EVENT, CONVERSION, REINFORCEMENT, TRIGGERED, BUCKET, SPAWN_ITEM_USE, COMMAND, DISPENSER, PATROL, TRIAL_SPAWNER, LOAD, DIMENSION_TRAVEL};
    }

    static {
        field_16464 = SpawnReason.method_36610();
    }
}

