package com.pedalhat.lategameplus.block;

import net.minecraft.util.StringIdentifiable;

public enum FusionForgeState implements StringIdentifiable {
    IDLE("idle"),
    NETHER("nether"),
    NETHER_WORKING("nether_working");

    private final String name;

    FusionForgeState(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
