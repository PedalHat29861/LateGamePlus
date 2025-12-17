/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.server.world.ChunkLevelType;

public final class EntityTrackingStatus
extends Enum<EntityTrackingStatus> {
    public static final /* enum */ EntityTrackingStatus HIDDEN = new EntityTrackingStatus(false, false);
    public static final /* enum */ EntityTrackingStatus TRACKED = new EntityTrackingStatus(true, false);
    public static final /* enum */ EntityTrackingStatus TICKING = new EntityTrackingStatus(true, true);
    private final boolean tracked;
    private final boolean tick;
    private static final /* synthetic */ EntityTrackingStatus[] field_27294;

    public static EntityTrackingStatus[] values() {
        return (EntityTrackingStatus[])field_27294.clone();
    }

    public static EntityTrackingStatus valueOf(String string) {
        return Enum.valueOf(EntityTrackingStatus.class, string);
    }

    private EntityTrackingStatus(boolean tracked, boolean tick) {
        this.tracked = tracked;
        this.tick = tick;
    }

    public boolean shouldTick() {
        return this.tick;
    }

    public boolean shouldTrack() {
        return this.tracked;
    }

    public static EntityTrackingStatus fromLevelType(ChunkLevelType levelType) {
        if (levelType.isAfter(ChunkLevelType.ENTITY_TICKING)) {
            return TICKING;
        }
        if (levelType.isAfter(ChunkLevelType.FULL)) {
            return TRACKED;
        }
        return HIDDEN;
    }

    private static /* synthetic */ EntityTrackingStatus[] method_36747() {
        return new EntityTrackingStatus[]{HIDDEN, TRACKED, TICKING};
    }

    static {
        field_27294 = EntityTrackingStatus.method_36747();
    }
}

