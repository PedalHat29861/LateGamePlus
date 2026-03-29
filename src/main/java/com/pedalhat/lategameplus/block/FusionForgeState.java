package com.pedalhat.lategameplus.block;

import net.minecraft.util.StringRepresentable;

public enum FusionForgeState implements StringRepresentable {
    DISABLED("disabled"),
    WORKING("working"),
    NETHER_DISABLED("nether_disabled"),
    NETHER_WORKING("nether_working");

    private final String name;

    FusionForgeState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
