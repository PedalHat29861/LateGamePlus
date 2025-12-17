/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public sealed class VaultState
extends Enum<VaultState>
implements StringIdentifiable {
    public static final /* enum */ VaultState INACTIVE = new VaultState("inactive", Light.HALF_LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            sharedData.setDisplayItem(ItemStack.EMPTY);
            world.syncWorldEvent(3016, pos, ominous ? 1 : 0);
        }
    };
    public static final /* enum */ VaultState ACTIVE = new VaultState("active", Light.LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            if (!sharedData.hasDisplayItem()) {
                VaultBlockEntity.Server.updateDisplayItem(world, this, config, sharedData, pos);
            }
            world.syncWorldEvent(3015, pos, ominous ? 1 : 0);
        }
    };
    public static final /* enum */ VaultState UNLOCKING = new VaultState("unlocking", Light.LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_INSERT_ITEM, SoundCategory.BLOCKS);
        }
    };
    public static final /* enum */ VaultState EJECTING = new VaultState("ejecting", Light.LIT){

        @Override
        protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS);
        }

        @Override
        protected void onChangedFrom(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_CLOSE_SHUTTER, SoundCategory.BLOCKS);
        }
    };
    private static final int field_48903 = 20;
    private static final int field_48904 = 20;
    private static final int field_48905 = 20;
    private static final int field_48906 = 20;
    private final String id;
    private final Light light;
    private static final /* synthetic */ VaultState[] field_48909;

    public static VaultState[] values() {
        return (VaultState[])field_48909.clone();
    }

    public static VaultState valueOf(String string) {
        return Enum.valueOf(VaultState.class, string);
    }

    VaultState(String id, Light light) {
        this.id = id;
        this.light = light;
    }

    @Override
    public String asString() {
        return this.id;
    }

    public int getLuminance() {
        return this.light.luminance;
    }

    public VaultState update(ServerWorld world, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> VaultState.updateActiveState(world, pos, config, serverData, sharedData, config.activationRange());
            case 1 -> VaultState.updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
            case 2 -> {
                serverData.setStateUpdatingResumeTime(world.getTime() + 20L);
                yield EJECTING;
            }
            case 3 -> {
                if (serverData.getItemsToEject().isEmpty()) {
                    serverData.finishEjecting();
                    yield VaultState.updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
                }
                float f = serverData.getEjectSoundPitchModifier();
                this.ejectItem(world, pos, serverData.getItemToEject(), f);
                sharedData.setDisplayItem(serverData.getItemToDisplay());
                boolean bl = serverData.getItemsToEject().isEmpty();
                int i = bl ? 20 : 20;
                serverData.setStateUpdatingResumeTime(world.getTime() + (long)i);
                yield EJECTING;
            }
        };
    }

    private static VaultState updateActiveState(ServerWorld world, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, double radius) {
        sharedData.updateConnectedPlayers(world, pos, serverData, config, radius);
        serverData.setStateUpdatingResumeTime(world.getTime() + 20L);
        return sharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onStateChange(ServerWorld world, BlockPos pos, VaultState newState, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
        this.onChangedFrom(world, pos, config, sharedData);
        newState.onChangedTo(world, pos, config, sharedData, ominous);
    }

    protected void onChangedTo(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean ominous) {
    }

    protected void onChangedFrom(ServerWorld world, BlockPos pos, VaultConfig config, VaultSharedData sharedData) {
    }

    private void ejectItem(ServerWorld world, BlockPos pos, ItemStack stack, float pitchModifier) {
        ItemDispenserBehavior.spawnItem(world, stack, 2, Direction.UP, Vec3d.ofBottomCenter(pos).offset(Direction.UP, 1.2));
        world.syncWorldEvent(3017, pos, 0);
        world.playSound(null, pos, SoundEvents.BLOCK_VAULT_EJECT_ITEM, SoundCategory.BLOCKS, 1.0f, 0.8f + 0.4f * pitchModifier);
    }

    private static /* synthetic */ VaultState[] method_56807() {
        return new VaultState[]{INACTIVE, ACTIVE, UNLOCKING, EJECTING};
    }

    static {
        field_48909 = VaultState.method_56807();
    }

    static final class Light
    extends Enum<Light> {
        public static final /* enum */ Light HALF_LIT = new Light(6);
        public static final /* enum */ Light LIT = new Light(12);
        final int luminance;
        private static final /* synthetic */ Light[] field_48914;

        public static Light[] values() {
            return (Light[])field_48914.clone();
        }

        public static Light valueOf(String string) {
            return Enum.valueOf(Light.class, string);
        }

        private Light(int luminance) {
            this.luminance = luminance;
        }

        private static /* synthetic */ Light[] method_56809() {
            return new Light[]{HALF_LIT, LIT};
        }

        static {
            field_48914 = Light.method_56809();
        }
    }
}

