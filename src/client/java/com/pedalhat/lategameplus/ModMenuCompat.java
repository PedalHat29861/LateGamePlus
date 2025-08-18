package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            final ModConfig cfg = ConfigManager.get();

            // Lodestone warp config
            var lodestoneWarpMaxUses = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.lodestone.warp"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.lodestone.warp.desc")))
                .binding(
                    1,
                    () -> cfg.lodestoneWarpMaxUses,
                    v  -> cfg.lodestoneWarpMaxUses = Math.max(0, Math.min(16, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 16)
                    .step(1)
                )
                .build();
            // Cooldown en ticks (80 = 4s)
            var lodestoneWarpCooldownTicks = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.lodestone.warp_cooldown"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.lodestone.warp_cooldown.desc")))
                .binding(
                    1,
                    () -> cfg.lodestoneWarpCooldownTicks,
                    v  -> cfg.lodestoneWarpCooldownTicks = Math.max(0, Math.min(1600, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 1600)
                    .step(1)
                )
                .build();
            // allow tp across dimensions
            var lodestoneWarpCrossDim = Option.<Boolean>createBuilder()
                .name(Text.translatable("lategameplus.config.lodestone.warp_across_dimensions"))
                .binding(
                    false,
                    () -> cfg.lodestoneWarpCrossDim,
                    v  -> cfg.lodestoneWarpCrossDim = v
                )
                .controller(opt -> BooleanControllerBuilder.create(opt))
                .build();

            // Elytra: nivel de protección (0..4)
            var elytraProt = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.elytra.protection_level"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.elytra.protection_level.desc")))
                .binding(
                    2,
                    () -> cfg.netheriteElytraProtectionLevel,
                    v  -> cfg.netheriteElytraProtectionLevel = Math.max(0, Math.min(4, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 4)
                    .step(1)
                )
                .build();

            // Tótem de netherite: usos (1..16)
            var netheriteTotemUses = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.totem.netherite_uses"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.totem.netherite_uses.desc")))
                .binding(
                    2,
                    () -> cfg.netheriteTotemUses,
                    v  -> cfg.netheriteTotemUses = Math.max(1, Math.min(16, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(1, 16)
                    .step(1)
                )
                .build();

            // Tótem de void: usos (1..16)
            var voidTotemUses = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.totem.void_uses"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.totem.void_uses.desc")))
                .binding(
                    3,
                    () -> cfg.voidTotemUses,
                    v  -> cfg.voidTotemUses = Math.max(1, Math.min(16, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(1, 16)
                    .step(1)
                )
                .build();

            // Piglin Brute: % de drop (0..100)
            var bruteDropChance = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.piglin_brute.drop_chance_percent"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.piglin_brute.drop_chance_percent.desc")))
                .binding(
                    15,
                    () -> Math.round(cfg.piglinBruteDropChance * 100f),
                    v  -> cfg.piglinBruteDropChance = Math.max(0f, Math.min(100f, v)) / 100f
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 100)
                    .step(1)
                )
                .build();

            // Piglin Brute: nuggets mínimos (0..16)
            var bruteNuggetMin = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.piglin_brute.nugget_min"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.piglin_brute.nugget_min.desc")))
                .binding(
                    0,
                    () -> cfg.piglinBruteNuggetMin,
                    v  -> cfg.piglinBruteNuggetMin = Math.max(0, Math.min(16, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 16)
                    .step(1)
                )
                .build();

            // Piglin Brute: nuggets máximos (0..16)
            var bruteNuggetMax = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.piglin_brute.nugget_max"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.piglin_brute.nugget_max.desc")))
                .binding(
                    2,
                    () -> cfg.piglinBruteNuggetMax,
                    v  -> cfg.piglinBruteNuggetMax = Math.max(0, Math.min(16, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 16)
                    .step(1)
                )
                .build();

            // Reparación por nugget (0..100%) – por defecto ~6% (1/18)
            var nuggetRepairPercent = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.repair.nugget_percent"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.repair.nugget_percent.desc")))
                .binding(
                    Math.round(100f / 18f), // ≈ 6
                    () -> Math.round(cfg.nuggetRepairPercent * 100f),
                    v  -> cfg.nuggetRepairPercent = Math.max(0f, Math.min(100f, v)) / 100f
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 100)
                    .step(1)
                )
                .build();

            var general = ConfigCategory.createBuilder()
                .name(Text.translatable("lategameplus.config.category.general"))
                .option(elytraProt)
                .option(netheriteTotemUses)
                .option(voidTotemUses)
                .option(bruteDropChance)
                .option(bruteNuggetMin)
                .option(bruteNuggetMax)
                .option(nuggetRepairPercent)
                .option(lodestoneWarpMaxUses)
                .option(lodestoneWarpCooldownTicks)
                .option(lodestoneWarpCrossDim)
                .build();

            return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("LateGamePlus"))
                .category(general)
                .save(() -> {
                    // Normalizar min ≤ max
                    if (cfg.piglinBruteNuggetMin > cfg.piglinBruteNuggetMax) {
                        int tmp = cfg.piglinBruteNuggetMin;
                        cfg.piglinBruteNuggetMin = cfg.piglinBruteNuggetMax;
                        cfg.piglinBruteNuggetMax = tmp;
                    }
                    ConfigManager.save();
                })
                .build()
                .generateScreen(parent);
        };
    }
}
