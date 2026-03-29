package com.pedalhat.lategameplus.util;

import com.pedalhat.lategameplus.config.ConfigManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AnimationSoundSynchronizer {
    private static final Map<Integer, AnimationConfig> ANIMATION_CONFIGS = new HashMap<>();
    private static int lastCustomModelData = -1;
    private static String lastStateString = "";
    private static int animationTick = 0;

    private static float volumeMultiplier() {
        float value = ConfigManager.get().debrisResonatorSoundVolume;
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            value = 1.0f;
        }
        return Mth.clamp(value, 0.0f, 4.0f);
    }
    
    static {
        
        
        
        
        
        ANIMATION_CONFIGS.put(-1, new AnimationConfig(0, 0, null, 0.0f, 0.0f)); 
        ANIMATION_CONFIGS.put(0, new AnimationConfig(15, 2, SoundEvents.NOTE_BLOCK_COW_BELL.value(), 1.0f, 0.8f)); 
        ANIMATION_CONFIGS.put(1, new AnimationConfig(15, 2, SoundEvents.NOTE_BLOCK_COW_BELL.value(), 1.0f, 0.8f)); 
        ANIMATION_CONFIGS.put(2, new AnimationConfig(15, 2, null, 0.0f, 0.0f)); 
        ANIMATION_CONFIGS.put(3, new AnimationConfig(10, 2, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.3f, 1.5f)); 
        ANIMATION_CONFIGS.put(4, new AnimationConfig(20, 2, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0f, 1.0f)); 
        ANIMATION_CONFIGS.put(5, new AnimationConfig(30, 2, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.75f, 0.7f)); 
        ANIMATION_CONFIGS.put(6, new AnimationConfig(60, 2, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.5f, 0.6f)); 
        
    }    public static void tick() {
        Player player = getCurrentPlayer();
        if (player == null) return;
        
        
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        
        ItemStack resonatorStack = null;
        if (isDebrisResonator(mainHand)) {
            resonatorStack = mainHand;
        } else if (isDebrisResonator(offHand)) {
            resonatorStack = offHand;
        }
        
        if (resonatorStack == null) {
            resetAnimationState();
            return;
        }
        
        
        int currentModelData = 1; 
        String currentStateString = "";
        if (resonatorStack.has(DataComponents.CUSTOM_MODEL_DATA)) {
            try {
                
                @SuppressWarnings("null")
                String componentString = resonatorStack.get(DataComponents.CUSTOM_MODEL_DATA).toString();
                
                
                if (componentString.contains("searching")) {
                    currentModelData = 1; currentStateString = "searching";
                } else if (componentString.contains("cooldown")) {
                    currentModelData = 2; currentStateString = "cooldown";
                } else if (componentString.contains("on_close")) {
                    currentModelData = 3; currentStateString = "on_close";
                } else if (componentString.contains("on_mid")) {
                    currentModelData = 4; currentStateString = "on_mid";
                } else if (componentString.contains("on_far")) {
                    currentModelData = 5; currentStateString = "on_far";
                } else if (componentString.contains("on_too_far")) {
                    currentModelData = 6; currentStateString = "on_too_far";
                } else if (componentString.contains("depleted")) {
                    currentModelData = -1; currentStateString = "depleted";
                } else {
                    
                    currentModelData = 0; currentStateString = "default";
                }
            } catch (Exception e) {
                currentModelData = 1; currentStateString = "searching"; 
            }
        } else {
            
            currentModelData = -1; currentStateString = "off";
        }
        
        
        boolean stateChanged = (currentModelData != lastCustomModelData) || !currentStateString.equals(lastStateString);
        if (stateChanged) {
            lastCustomModelData = currentModelData;
            lastStateString = currentStateString;
            animationTick = 1; 
        }
        
        
        AnimationConfig config = ANIMATION_CONFIGS.get(currentModelData);
        if (config != null) {
            
            if (config.frametime > 0 && config.soundEvent != null && animationTick % config.frametime == 1) {
                playAnimationSound(player, config);
            } else if (config.frametime == 0) {
                
            }
            
            animationTick++; 
        } else {
        }
    }
    
    private static boolean isDebrisResonator(ItemStack stack) {
        
        return stack != null && 
               !stack.isEmpty() && 
               stack.getItem().toString().contains("debris_resonator");
    }
    
    private static void resetAnimationState() {
        lastCustomModelData = -1;
        lastStateString = "";
        animationTick = 0;
    }
    
    private static Player getCurrentPlayer() {
        Minecraft client = Minecraft.getInstance();
        return (client != null && client.player != null) ? client.player : null;
    }
    
    private static void playAnimationSound(Player player, AnimationConfig config) {
        if (config.soundEvent == null) {
            return;
        }

        float adjustedVolume = config.volume * volumeMultiplier();
        if (adjustedVolume <= 0.0f) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getSoundManager() == null) {
            return;
        }

        net.minecraft.client.resources.sounds.SimpleSoundInstance soundInstance =
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        config.soundEvent,
                        config.pitch,
                        adjustedVolume
                );

        client.getSoundManager().play(soundInstance);
    }
    
    
    private static class AnimationConfig {
        final int frametime; 
        @SuppressWarnings("unused")
        final int totalFrames;
        final net.minecraft.sounds.SoundEvent soundEvent;
        final float pitch;
        final float volume; 
        
        AnimationConfig(int frametime, int totalFrames, net.minecraft.sounds.SoundEvent soundEvent, float pitch, float volume) {
            this.frametime = frametime;
            this.totalFrames = totalFrames;
            this.soundEvent = soundEvent;
            this.pitch = pitch;
            this.volume = volume;
        }
    }
}