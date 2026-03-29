/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.level.CardinalLighting$Type
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.lwjgl.system.MemoryStack
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

public class Lighting
implements AutoCloseable {
    private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.2f, 1.0f, -0.7f).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.2f, 1.0f, 0.7f).normalize();
    private static final Vector3f NETHER_DIFFUSE_LIGHT_0 = new Vector3f(0.2f, 1.0f, -0.7f).normalize();
    private static final Vector3f NETHER_DIFFUSE_LIGHT_1 = new Vector3f(-0.2f, -1.0f, 0.7f).normalize();
    private static final Vector3f INVENTORY_DIFFUSE_LIGHT_0 = new Vector3f(0.2f, -1.0f, 1.0f).normalize();
    private static final Vector3f INVENTORY_DIFFUSE_LIGHT_1 = new Vector3f(-0.2f, -1.0f, 0.0f).normalize();
    public static final int UBO_SIZE = new Std140SizeCalculator().putVec3().putVec3().get();
    private final GpuBuffer buffer;
    private final long paddedSize;

    public Lighting() {
        GpuDevice device = RenderSystem.getDevice();
        this.paddedSize = Mth.roundToward((int)UBO_SIZE, (int)device.getUniformOffsetAlignment());
        this.buffer = device.createBuffer(() -> "Lighting UBO", 136, this.paddedSize * (long)Entry.values().length);
        Matrix4f flatPose = new Matrix4f().rotationY(-0.3926991f).rotateX(2.3561945f);
        this.updateBuffer(Entry.ITEMS_FLAT, flatPose.transformDirection((Vector3fc)DIFFUSE_LIGHT_0, new Vector3f()), flatPose.transformDirection((Vector3fc)DIFFUSE_LIGHT_1, new Vector3f()));
        Matrix4f item3DPose = new Matrix4f().scaling(1.0f, -1.0f, 1.0f).rotateYXZ(1.0821041f, 3.2375858f, 0.0f).rotateYXZ(-0.3926991f, 2.3561945f, 0.0f);
        this.updateBuffer(Entry.ITEMS_3D, item3DPose.transformDirection((Vector3fc)DIFFUSE_LIGHT_0, new Vector3f()), item3DPose.transformDirection((Vector3fc)DIFFUSE_LIGHT_1, new Vector3f()));
        this.updateBuffer(Entry.ENTITY_IN_UI, INVENTORY_DIFFUSE_LIGHT_0, INVENTORY_DIFFUSE_LIGHT_1);
        Matrix4f playerSkinPose = new Matrix4f();
        this.updateBuffer(Entry.PLAYER_SKIN, playerSkinPose.transformDirection((Vector3fc)INVENTORY_DIFFUSE_LIGHT_0, new Vector3f()), playerSkinPose.transformDirection((Vector3fc)INVENTORY_DIFFUSE_LIGHT_1, new Vector3f()));
    }

    public void updateLevel(CardinalLighting.Type type) {
        switch (type) {
            case DEFAULT: {
                this.updateBuffer(Entry.LEVEL, DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
                break;
            }
            case NETHER: {
                this.updateBuffer(Entry.LEVEL, NETHER_DIFFUSE_LIGHT_0, NETHER_DIFFUSE_LIGHT_1);
            }
        }
    }

    private void updateBuffer(Entry entry, Vector3f light0, Vector3f light1) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = Std140Builder.onStack(stack, UBO_SIZE).putVec3((Vector3fc)light0).putVec3((Vector3fc)light1).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice((long)entry.ordinal() * this.paddedSize, this.paddedSize), byteBuffer);
        }
    }

    public void setupFor(Entry entry) {
        RenderSystem.setShaderLights(this.buffer.slice((long)entry.ordinal() * this.paddedSize, UBO_SIZE));
    }

    @Override
    public void close() {
        this.buffer.close();
    }

    public static enum Entry {
        LEVEL,
        ITEMS_FLAT,
        ITEMS_3D,
        ENTITY_IN_UI,
        PLAYER_SKIN;

    }
}

