/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import java.util.Map;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.passive.CopperGolemOxidationLevel;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class CopperGolemOxidationLevels {
    private static final CopperGolemOxidationLevel UNAFFECTED = new CopperGolemOxidationLevel(SoundEvents.ENTITY_COPPER_GOLEM_SPIN, SoundEvents.ENTITY_COPPER_GOLEM_HURT, SoundEvents.ENTITY_COPPER_GOLEM_DEATH, SoundEvents.ENTITY_COPPER_GOLEM_STEP, Identifier.ofVanilla("textures/entity/copper_golem/copper_golem.png"), Identifier.ofVanilla("textures/entity/copper_golem/copper_golem_eyes.png"));
    private static final CopperGolemOxidationLevel EXPOSED = new CopperGolemOxidationLevel(SoundEvents.ENTITY_COPPER_GOLEM_SPIN, SoundEvents.ENTITY_COPPER_GOLEM_HURT, SoundEvents.ENTITY_COPPER_GOLEM_DEATH, SoundEvents.ENTITY_COPPER_GOLEM_STEP, Identifier.ofVanilla("textures/entity/copper_golem/exposed_copper_golem.png"), Identifier.ofVanilla("textures/entity/copper_golem/exposed_copper_golem_eyes.png"));
    private static final CopperGolemOxidationLevel WEATHERED = new CopperGolemOxidationLevel(SoundEvents.ENTITY_COPPER_GOLEM_WEATHERED_SPIN, SoundEvents.ENTITY_COPPER_GOLEM_WEATHERED_HURT, SoundEvents.ENTITY_COPPER_GOLEM_WEATHERED_DEATH, SoundEvents.ENTITY_COPPER_GOLEM_WEATHERED_STEP, Identifier.ofVanilla("textures/entity/copper_golem/weathered_copper_golem.png"), Identifier.ofVanilla("textures/entity/copper_golem/weathered_copper_golem_eyes.png"));
    private static final CopperGolemOxidationLevel OXIDIZED = new CopperGolemOxidationLevel(SoundEvents.ENTITY_COPPER_GOLEM_OXIDIZED_SPIN, SoundEvents.ENTITY_COPPER_GOLEM_OXIDIZED_HURT, SoundEvents.ENTITY_COPPER_GOLEM_OXIDIZED_DEATH, SoundEvents.ENTITY_COPPER_GOLEM_OXIDIZED_STEP, Identifier.ofVanilla("textures/entity/copper_golem/oxidized_copper_golem.png"), Identifier.ofVanilla("textures/entity/copper_golem/oxidized_copper_golem_eyes.png"));
    private static final Map<Oxidizable.OxidationLevel, CopperGolemOxidationLevel> LEVELS = Map.of(Oxidizable.OxidationLevel.UNAFFECTED, UNAFFECTED, Oxidizable.OxidationLevel.EXPOSED, EXPOSED, Oxidizable.OxidationLevel.WEATHERED, WEATHERED, Oxidizable.OxidationLevel.OXIDIZED, OXIDIZED);

    public static CopperGolemOxidationLevel get(Oxidizable.OxidationLevel oxidationLevel) {
        return LEVELS.get(oxidationLevel);
    }
}

