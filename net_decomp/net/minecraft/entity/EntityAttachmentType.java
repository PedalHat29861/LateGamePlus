/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import java.util.List;
import net.minecraft.util.math.Vec3d;

public final class EntityAttachmentType
extends Enum<EntityAttachmentType> {
    public static final /* enum */ EntityAttachmentType PASSENGER = new EntityAttachmentType(Point.AT_HEIGHT);
    public static final /* enum */ EntityAttachmentType VEHICLE = new EntityAttachmentType(Point.ZERO);
    public static final /* enum */ EntityAttachmentType NAME_TAG = new EntityAttachmentType(Point.AT_HEIGHT);
    public static final /* enum */ EntityAttachmentType WARDEN_CHEST = new EntityAttachmentType(Point.WARDEN_CHEST);
    private final Point point;
    private static final /* synthetic */ EntityAttachmentType[] field_47747;

    public static EntityAttachmentType[] values() {
        return (EntityAttachmentType[])field_47747.clone();
    }

    public static EntityAttachmentType valueOf(String string) {
        return Enum.valueOf(EntityAttachmentType.class, string);
    }

    private EntityAttachmentType(Point point) {
        this.point = point;
    }

    public List<Vec3d> createPoint(float width, float height) {
        return this.point.create(width, height);
    }

    private static /* synthetic */ EntityAttachmentType[] method_55669() {
        return new EntityAttachmentType[]{PASSENGER, VEHICLE, NAME_TAG, WARDEN_CHEST};
    }

    static {
        field_47747 = EntityAttachmentType.method_55669();
    }

    public static interface Point {
        public static final List<Vec3d> NONE = List.of(Vec3d.ZERO);
        public static final Point ZERO = (width, height) -> NONE;
        public static final Point AT_HEIGHT = (width, height) -> List.of(new Vec3d(0.0, height, 0.0));
        public static final Point WARDEN_CHEST = (width, height) -> List.of(new Vec3d(0.0, (double)height / 2.0, 0.0));

        public List<Vec3d> create(float var1, float var2);
    }
}

