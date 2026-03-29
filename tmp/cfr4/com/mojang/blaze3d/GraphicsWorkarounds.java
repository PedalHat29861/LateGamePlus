/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OS
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.GpuDevice;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class GraphicsWorkarounds {
    private static final List<String> INTEL_GEN11_CORE = List.of("i3-1000g1", "i3-1000g4", "i3-1000ng4", "i3-1005g1", "i3-l13g4", "i5-1030g4", "i5-1030g7", "i5-1030ng7", "i5-1034g1", "i5-1035g1", "i5-1035g4", "i5-1035g7", "i5-1038ng7", "i5-l16g7", "i7-1060g7", "i7-1060ng7", "i7-1065g7", "i7-1068g7", "i7-1068ng7");
    private static final List<String> INTEL_GEN11_ATOM = List.of("x6211e", "x6212re", "x6214re", "x6413e", "x6414re", "x6416re", "x6425e", "x6425re", "x6427fe");
    private static final List<String> INTEL_GEN11_CELERON = List.of("j6412", "j6413", "n4500", "n4505", "n5095", "n5095a", "n5100", "n5105", "n6210", "n6211");
    private static final List<String> INTEL_GEN11_PENTIUM = List.of("6805", "j6426", "n6415", "n6000", "n6005");
    private static @Nullable GraphicsWorkarounds instance;
    private final WeakReference<GpuDevice> gpuDevice;
    private final boolean alwaysCreateFreshImmediateBuffer;
    private final boolean isGlOnDx12;
    private final boolean isAmd;

    private GraphicsWorkarounds(GpuDevice gpuDevice) {
        this.gpuDevice = new WeakReference<GpuDevice>(gpuDevice);
        this.alwaysCreateFreshImmediateBuffer = GraphicsWorkarounds.isIntelGen11(gpuDevice);
        this.isGlOnDx12 = GraphicsWorkarounds.isGlOnDx12(gpuDevice);
        this.isAmd = GraphicsWorkarounds.isAmd(gpuDevice);
    }

    public static GraphicsWorkarounds get(GpuDevice gpuDevice) {
        GraphicsWorkarounds instance = GraphicsWorkarounds.instance;
        if (instance == null || instance.gpuDevice.get() != gpuDevice) {
            GraphicsWorkarounds.instance = instance = new GraphicsWorkarounds(gpuDevice);
        }
        return instance;
    }

    public boolean alwaysCreateFreshImmediateBuffer() {
        return this.alwaysCreateFreshImmediateBuffer;
    }

    public boolean isGlOnDx12() {
        return this.isGlOnDx12;
    }

    public boolean isAmd() {
        return this.isAmd;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean isIntelGen11(GpuDevice gpuDevice) {
        String cpuInfo = GLX._getCpuInfo().toLowerCase(Locale.ROOT);
        String renderer = gpuDevice.getRenderer().toLowerCase(Locale.ROOT);
        if (!cpuInfo.contains("intel")) return false;
        if (!renderer.contains("intel")) return false;
        if (renderer.contains("mesa")) {
            return false;
        }
        if (renderer.endsWith("gen11")) {
            return true;
        }
        if (!renderer.contains("uhd graphics") && !renderer.contains("iris")) {
            return false;
        }
        if (cpuInfo.contains("atom")) {
            if (INTEL_GEN11_ATOM.stream().anyMatch(cpuInfo::contains)) return true;
        }
        if (cpuInfo.contains("celeron")) {
            if (INTEL_GEN11_CELERON.stream().anyMatch(cpuInfo::contains)) return true;
        }
        if (cpuInfo.contains("pentium")) {
            if (INTEL_GEN11_PENTIUM.stream().anyMatch(cpuInfo::contains)) return true;
        }
        if (!INTEL_GEN11_CORE.stream().anyMatch(cpuInfo::contains)) return false;
        return true;
    }

    private static boolean isGlOnDx12(GpuDevice gpuDevice) {
        boolean isWindowsArm64 = Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64();
        return isWindowsArm64 || gpuDevice.getRenderer().startsWith("D3D12");
    }

    private static boolean isAmd(GpuDevice gpuDevice) {
        return gpuDevice.getRenderer().contains("AMD");
    }
}

