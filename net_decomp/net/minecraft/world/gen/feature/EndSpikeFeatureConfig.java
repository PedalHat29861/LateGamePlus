/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.jspecify.annotations.Nullable;

public class EndSpikeFeatureConfig
implements FeatureConfig {
    public static final Codec<EndSpikeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.fieldOf("crystal_invulnerable").orElse((Object)false).forGetter(config -> config.crystalInvulnerable), (App)EndSpikeFeature.Spike.CODEC.listOf().fieldOf("spikes").forGetter(config -> config.spikes), (App)BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter(config -> Optional.ofNullable(config.crystalBeamTarget))).apply((Applicative)instance, EndSpikeFeatureConfig::new));
    private final boolean crystalInvulnerable;
    private final List<EndSpikeFeature.Spike> spikes;
    private final @Nullable BlockPos crystalBeamTarget;

    public EndSpikeFeatureConfig(boolean crystalInvulnerable, List<EndSpikeFeature.Spike> spikes, @Nullable BlockPos crystalBeamTarget) {
        this(crystalInvulnerable, spikes, Optional.ofNullable(crystalBeamTarget));
    }

    private EndSpikeFeatureConfig(boolean crystalInvulnerable, List<EndSpikeFeature.Spike> spikes, Optional<BlockPos> crystalBeamTarget) {
        this.crystalInvulnerable = crystalInvulnerable;
        this.spikes = spikes;
        this.crystalBeamTarget = crystalBeamTarget.orElse(null);
    }

    public boolean isCrystalInvulnerable() {
        return this.crystalInvulnerable;
    }

    public List<EndSpikeFeature.Spike> getSpikes() {
        return this.spikes;
    }

    public @Nullable BlockPos getPos() {
        return this.crystalBeamTarget;
    }
}

