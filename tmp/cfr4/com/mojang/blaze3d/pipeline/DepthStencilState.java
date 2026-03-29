/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.CompareOp;

public record DepthStencilState(CompareOp depthTest, boolean writeDepth, float depthBiasScaleFactor, float depthBiasConstant) {
    public static final DepthStencilState DEFAULT = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true);

    public DepthStencilState(CompareOp depthTest, boolean depthWrite) {
        this(depthTest, depthWrite, 0.0f, 0.0f);
    }
}

