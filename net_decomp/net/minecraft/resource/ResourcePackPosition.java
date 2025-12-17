/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import net.minecraft.resource.ResourcePackProfile;

public record ResourcePackPosition(boolean required, ResourcePackProfile.InsertionPosition defaultPosition, boolean fixedPosition) {
}

