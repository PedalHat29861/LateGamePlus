/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block.vault;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.block.spawner.EntityDetector;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.RegistryKey;

public record VaultConfig(RegistryKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<RegistryKey<LootTable>> overrideLootTableToDisplay, EntityDetector playerDetector, EntityDetector.Selector entitySelector) {
    private final EntityDetector playerDetector;
    static final String CONFIG_KEY = "config";
    static VaultConfig DEFAULT = new VaultConfig();
    static Codec<VaultConfig> codec = RecordCodecBuilder.create(instance -> instance.group((App)LootTable.TABLE_KEY.lenientOptionalFieldOf("loot_table", DEFAULT.lootTable()).forGetter(VaultConfig::lootTable), (App)Codec.DOUBLE.lenientOptionalFieldOf("activation_range", (Object)DEFAULT.activationRange()).forGetter(VaultConfig::activationRange), (App)Codec.DOUBLE.lenientOptionalFieldOf("deactivation_range", (Object)DEFAULT.deactivationRange()).forGetter(VaultConfig::deactivationRange), (App)ItemStack.createOptionalCodec("key_item").forGetter(VaultConfig::keyItem), (App)LootTable.TABLE_KEY.lenientOptionalFieldOf("override_loot_table_to_display").forGetter(VaultConfig::overrideLootTableToDisplay)).apply((Applicative)instance, VaultConfig::new)).validate(VaultConfig::validate);

    private VaultConfig() {
        this(LootTables.TRIAL_CHAMBERS_REWARD_CHEST, 4.0, 4.5, new ItemStack(Items.TRIAL_KEY), Optional.empty(), EntityDetector.NON_SPECTATOR_PLAYERS, EntityDetector.Selector.IN_WORLD);
    }

    public VaultConfig(RegistryKey<LootTable> lootTable, double activationRange, double deactivationRange, ItemStack keyItem, Optional<RegistryKey<LootTable>> overrideLootTableToDisplay) {
        this(lootTable, activationRange, deactivationRange, keyItem, overrideLootTableToDisplay, DEFAULT.playerDetector(), DEFAULT.entitySelector());
    }

    public EntityDetector playerDetector() {
        return SharedConstants.VAULT_DETECTS_SHEEP_AS_PLAYERS ? EntityDetector.SHEEP : this.playerDetector;
    }

    private DataResult<VaultConfig> validate() {
        if (this.activationRange > this.deactivationRange) {
            return DataResult.error(() -> "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")");
        }
        return DataResult.success((Object)this);
    }
}

