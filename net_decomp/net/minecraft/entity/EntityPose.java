/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class EntityPose
extends Enum<EntityPose>
implements StringIdentifiable {
    public static final /* enum */ EntityPose STANDING = new EntityPose(0, "standing");
    public static final /* enum */ EntityPose GLIDING = new EntityPose(1, "fall_flying");
    public static final /* enum */ EntityPose SLEEPING = new EntityPose(2, "sleeping");
    public static final /* enum */ EntityPose SWIMMING = new EntityPose(3, "swimming");
    public static final /* enum */ EntityPose SPIN_ATTACK = new EntityPose(4, "spin_attack");
    public static final /* enum */ EntityPose CROUCHING = new EntityPose(5, "crouching");
    public static final /* enum */ EntityPose LONG_JUMPING = new EntityPose(6, "long_jumping");
    public static final /* enum */ EntityPose DYING = new EntityPose(7, "dying");
    public static final /* enum */ EntityPose CROAKING = new EntityPose(8, "croaking");
    public static final /* enum */ EntityPose USING_TONGUE = new EntityPose(9, "using_tongue");
    public static final /* enum */ EntityPose SITTING = new EntityPose(10, "sitting");
    public static final /* enum */ EntityPose ROARING = new EntityPose(11, "roaring");
    public static final /* enum */ EntityPose SNIFFING = new EntityPose(12, "sniffing");
    public static final /* enum */ EntityPose EMERGING = new EntityPose(13, "emerging");
    public static final /* enum */ EntityPose DIGGING = new EntityPose(14, "digging");
    public static final /* enum */ EntityPose SLIDING = new EntityPose(15, "sliding");
    public static final /* enum */ EntityPose SHOOTING = new EntityPose(16, "shooting");
    public static final /* enum */ EntityPose INHALING = new EntityPose(17, "inhaling");
    public static final IntFunction<EntityPose> INDEX_TO_VALUE;
    public static final Codec<EntityPose> CODEC;
    public static final PacketCodec<ByteBuf, EntityPose> PACKET_CODEC;
    private final int index;
    private final String name;
    private static final /* synthetic */ EntityPose[] field_18083;

    public static EntityPose[] values() {
        return (EntityPose[])field_18083.clone();
    }

    public static EntityPose valueOf(String string) {
        return Enum.valueOf(EntityPose.class, string);
    }

    private EntityPose(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ EntityPose[] method_36612() {
        return new EntityPose[]{STANDING, GLIDING, SLEEPING, SWIMMING, SPIN_ATTACK, CROUCHING, LONG_JUMPING, DYING, CROAKING, USING_TONGUE, SITTING, ROARING, SNIFFING, EMERGING, DIGGING, SLIDING, SHOOTING, INHALING};
    }

    static {
        field_18083 = EntityPose.method_36612();
        INDEX_TO_VALUE = ValueLists.createIndexToValueFunction(EntityPose::getIndex, EntityPose.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(EntityPose::values);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, EntityPose::getIndex);
    }
}

