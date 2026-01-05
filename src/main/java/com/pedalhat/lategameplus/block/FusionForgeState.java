package com.pedalhat.lategameplus.block;

import net.minecraft.util.StringIdentifiable;

public enum FusionForgeState implements StringIdentifiable {
    DISABLED("disabled"),
    NETHER_DISABLED("nether_disabled"),
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
