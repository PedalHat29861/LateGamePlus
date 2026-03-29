/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWVidMode
 *  org.lwjgl.glfw.GLFWVidMode$Buffer
 */
package com.mojang.blaze3d.platform;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.VideoMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public final class Monitor {
    private final long monitor;
    private final List<VideoMode> videoModes;
    private VideoMode currentMode;
    private int x;
    private int y;

    public Monitor(long monitor) {
        this.monitor = monitor;
        this.videoModes = Lists.newArrayList();
        this.refreshVideoModes();
    }

    public void refreshVideoModes() {
        this.videoModes.clear();
        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes((long)this.monitor);
        for (int i = modes.limit() - 1; i >= 0; --i) {
            modes.position(i);
            VideoMode mode = new VideoMode(modes);
            if (mode.getRedBits() < 8 || mode.getGreenBits() < 8 || mode.getBlueBits() < 8) continue;
            this.videoModes.add(mode);
        }
        int[] x = new int[1];
        int[] y = new int[1];
        GLFW.glfwGetMonitorPos((long)this.monitor, (int[])x, (int[])y);
        this.x = x[0];
        this.y = y[0];
        GLFWVidMode mode = GLFW.glfwGetVideoMode((long)this.monitor);
        this.currentMode = new VideoMode(mode);
    }

    public VideoMode getPreferredVidMode(Optional<VideoMode> expectedMode) {
        if (expectedMode.isPresent()) {
            VideoMode videoMode = expectedMode.get();
            for (VideoMode mode : this.videoModes) {
                if (!mode.equals(videoMode)) continue;
                return mode;
            }
        }
        return this.getCurrentMode();
    }

    public int getVideoModeIndex(VideoMode videoMode) {
        return this.videoModes.indexOf(videoMode);
    }

    public VideoMode getCurrentMode() {
        return this.currentMode;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public VideoMode getMode(int mode) {
        return this.videoModes.get(mode);
    }

    public int getModeCount() {
        return this.videoModes.size();
    }

    public long getMonitor() {
        return this.monitor;
    }

    public String toString() {
        return String.format(Locale.ROOT, "Monitor[%s %sx%s %s]", this.monitor, this.x, this.y, this.currentMode);
    }
}

