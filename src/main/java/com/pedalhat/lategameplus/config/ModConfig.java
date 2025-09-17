package com.pedalhat.lategameplus.config;

public class ModConfig {
    public int netheriteElytraProtectionLevel = 2;           // 0..4 (Iron default)
    public int netheriteTotemUses = 2;              // >=1
    public int voidTotemUses = 3;                   // >=1
    public float piglinBruteDropChance = 0.15f;     // 0..1
    public int piglinBruteNuggetMin = 0;            // 0..16
    public int piglinBruteNuggetMax = 2;            // 0..16
    public float nuggetRepairPercent = 1f/18f;      // 0..1

    public float netheriteCrossbowDamageMultiplier = 1.5f; // projectile damage multiplier

    public int lodestoneWarpCooldownTicks = 80;    // IN TPS; 1s = 20 TPS
    public boolean lodestoneWarpCrossDim = false;   // allow across dimensions

    public int netheriteAnvilMaxLevelCost = 35;     // 20..39 levels
}
