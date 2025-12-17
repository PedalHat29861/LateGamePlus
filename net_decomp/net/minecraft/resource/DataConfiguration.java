/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.resource;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;

public record DataConfiguration(DataPackSettings dataPacks, FeatureSet enabledFeatures) {
    public static final String ENABLED_FEATURES_KEY = "enabled_features";
    public static final MapCodec<DataConfiguration> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DataPackSettings.CODEC.lenientOptionalFieldOf("DataPacks", (Object)DataPackSettings.SAFE_MODE).forGetter(DataConfiguration::dataPacks), (App)FeatureFlags.CODEC.lenientOptionalFieldOf(ENABLED_FEATURES_KEY, (Object)FeatureFlags.DEFAULT_ENABLED_FEATURES).forGetter(DataConfiguration::enabledFeatures)).apply((Applicative)instance, DataConfiguration::new));
    public static final Codec<DataConfiguration> CODEC = MAP_CODEC.codec();
    public static final DataConfiguration SAFE_MODE = new DataConfiguration(DataPackSettings.SAFE_MODE, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    public DataConfiguration withFeaturesAdded(FeatureSet features) {
        return new DataConfiguration(this.dataPacks, this.enabledFeatures.combine(features));
    }
}

