package com.pedalhat.lategameplus.mixin.client;

import com.pedalhat.lategameplus.mixinutil.LGPChestedRenderState;
import net.minecraft.client.render.entity.state.HappyGhastEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HappyGhastEntityRenderState.class)
public class HappyGhastEntityRenderStateMixin implements LGPChestedRenderState {
    @Unique
    private int lategameplus$chestCount;

    @Override
    public int lategameplus$getChestCount() {
        return this.lategameplus$chestCount;
    }

    @Override
    public void lategameplus$setChestCount(int count) {
        this.lategameplus$chestCount = count;
    }
}
