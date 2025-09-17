package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            final ModConfig cfg = ConfigManager.get();

            var crossbowDamageMultiplier = Option.<Float>createBuilder()
                .name(Text.translatable("lategameplus.config.crossbow.damage_multiplier"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.crossbow.damage_multiplier.desc")))
                .binding(
                    1.5f,
                    () -> cfg.netheriteCrossbowDamageMultiplier,
                    v  -> cfg.netheriteCrossbowDamageMultiplier = Math.max(0f, Math.min(5f, v))
                )
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                    .range(0f, 5f)
                    .step(0.05f))
                .build();

            var netheriteAnvilCap = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.anvil.max_cost"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.anvil.max_cost.desc")))
                .binding(
                    35,
                    () -> Math.max(20, Math.min(39, cfg.netheriteAnvilMaxLevelCost)),
                    v  -> cfg.netheriteAnvilMaxLevelCost = Math.max(20, Math.min(39, v))
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(20, 39)
                    .step(1))
                .build();

            var nuggetRepairPercent = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.repair.nugget_percent"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.repair.nugget_percent.desc")))
                .binding(
                    Math.round(100f / 18f),
                    () -> Math.round(cfg.nuggetRepairPercent * 100f),
                    v  -> cfg.nuggetRepairPercent = Math.max(0f, Math.min(100f, v)) / 100f
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 100)
                    .step(1))
                .build();

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
                    .step(1))
                .build();

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
                    .step(1))
                .build();

            var lodestoneWarpCooldownSeconds = Option.<Integer>createBuilder()
                .name(Text.translatable("lategameplus.config.lodestone.warp_cooldown_seconds"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.lodestone.warp_cooldown_seconds.desc")))
                .binding(
                    4,
                    () -> Math.max(0, cfg.lodestoneWarpCooldownTicks / 20),
                    v  -> cfg.lodestoneWarpCooldownTicks = Math.max(0, v) * 20
                )
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                    .min(0)
                    .max(3600))
                .build();

            var lodestoneWarpCrossDim = Option.<Boolean>createBuilder()
                .name(Text.translatable("lategameplus.config.lodestone.warp_across_dimensions"))
                .description(OptionDescription.of(Text.translatable("lategameplus.config.lodestone.warp_across_dimensions.desc")))
                .binding(
                    false,
                    () -> cfg.lodestoneWarpCrossDim,
                    v  -> cfg.lodestoneWarpCrossDim = v
                )
                .controller(BooleanControllerBuilder::create)
                .build();

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
                    .step(1))
                .build();

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
                    .step(1))
                .build();

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
                    .step(1))
                .build();

            var catGeneral = ConfigCategory.createBuilder()
                .name(Text.translatable("lategameplus.config.category.general"))
                .option(crossbowDamageMultiplier)
                .option(netheriteAnvilCap)
                .option(elytraProt)
                .option(netheriteTotemUses)
                .option(nuggetRepairPercent)
                .build();

            var catLodestone = ConfigCategory.createBuilder()
                .name(Text.translatable("lategameplus.config.category.lodestone"))
                .option(lodestoneWarpCooldownSeconds)
                .option(lodestoneWarpCrossDim)
                .build();

            var catPiglin = ConfigCategory.createBuilder()
                .name(Text.translatable("lategameplus.config.category.piglin"))
                .option(bruteDropChance)
                .option(bruteNuggetMin)
                .option(bruteNuggetMax)
                .build();

            return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("LateGamePlus"))
                .category(catGeneral)
                .category(catLodestone)
                .category(catPiglin)
                .save(() -> {
                    if (cfg.piglinBruteNuggetMin > cfg.piglinBruteNuggetMax) {
                        int tmp = cfg.piglinBruteNuggetMin;
                        cfg.piglinBruteNuggetMin = cfg.piglinBruteNuggetMax;
                        cfg.piglinBruteNuggetMax = tmp;
                    }
                    cfg.netheriteCrossbowDamageMultiplier = Math.max(0f, Math.min(5f, cfg.netheriteCrossbowDamageMultiplier));
                    cfg.netheriteAnvilMaxLevelCost = Math.max(20, Math.min(39, cfg.netheriteAnvilMaxLevelCost));
                    ConfigManager.save();
                })
                .build()
                .generateScreen(parent);
        };
    }
}
