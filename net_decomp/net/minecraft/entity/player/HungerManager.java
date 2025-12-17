/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.player;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.rule.GameRules;

public class HungerManager {
    private static final int DEFAULT_FOOD_TICK_TIMER = 0;
    private static final float DEFAULT_EXHAUSTION = 0.0f;
    private int foodLevel = 20;
    private float saturationLevel = 5.0f;
    private float exhaustion;
    private int foodTickTimer;

    private void addInternal(int nutrition, float saturation) {
        this.foodLevel = MathHelper.clamp(nutrition + this.foodLevel, 0, 20);
        this.saturationLevel = MathHelper.clamp(saturation + this.saturationLevel, 0.0f, (float)this.foodLevel);
    }

    public void add(int food, float saturationModifier) {
        this.addInternal(food, HungerConstants.calculateSaturation(food, saturationModifier));
    }

    public void eat(FoodComponent foodComponent) {
        this.addInternal(foodComponent.nutrition(), foodComponent.saturation());
    }

    public void update(ServerPlayerEntity player) {
        boolean bl;
        ServerWorld serverWorld = player.getEntityWorld();
        Difficulty difficulty = serverWorld.getDifficulty();
        if (this.exhaustion > 4.0f) {
            this.exhaustion -= 4.0f;
            if (this.saturationLevel > 0.0f) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0f, 0.0f);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        if ((bl = serverWorld.getGameRules().getValue(GameRules.NATURAL_HEALTH_REGENERATION).booleanValue()) && this.saturationLevel > 0.0f && player.canFoodHeal() && this.foodLevel >= 20) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0f);
                player.heal(f / 6.0f);
                this.addExhaustion(f);
                this.foodTickTimer = 0;
            }
        } else if (bl && this.foodLevel >= 18 && player.canFoodHeal()) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                player.heal(1.0f);
                this.addExhaustion(6.0f);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                if (player.getHealth() > 10.0f || difficulty == Difficulty.HARD || player.getHealth() > 1.0f && difficulty == Difficulty.NORMAL) {
                    player.damage(serverWorld, player.getDamageSources().starve(), 1.0f);
                }
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
    }

    public void readData(ReadView view) {
        this.foodLevel = view.getInt("foodLevel", 20);
        this.foodTickTimer = view.getInt("foodTickTimer", 0);
        this.saturationLevel = view.getFloat("foodSaturationLevel", 5.0f);
        this.exhaustion = view.getFloat("foodExhaustionLevel", 0.0f);
    }

    public void writeData(WriteView view) {
        view.putInt("foodLevel", this.foodLevel);
        view.putInt("foodTickTimer", this.foodTickTimer);
        view.putFloat("foodSaturationLevel", this.saturationLevel);
        view.putFloat("foodExhaustionLevel", this.exhaustion);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean canSprint() {
        return (float)this.getFoodLevel() > 6.0f;
    }

    public boolean isNotFull() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float exhaustion) {
        this.exhaustion = Math.min(this.exhaustion + exhaustion, 40.0f);
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }
}

