/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.dynamic.Codecs;

public record ToolComponent(List<Rule> rules, float defaultMiningSpeed, int damagePerBlock, boolean canDestroyBlocksInCreative) {
    public static final Codec<ToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Rule.CODEC.listOf().fieldOf("rules").forGetter(ToolComponent::rules), (App)Codec.FLOAT.optionalFieldOf("default_mining_speed", (Object)Float.valueOf(1.0f)).forGetter(ToolComponent::defaultMiningSpeed), (App)Codecs.NON_NEGATIVE_INT.optionalFieldOf("damage_per_block", (Object)1).forGetter(ToolComponent::damagePerBlock), (App)Codec.BOOL.optionalFieldOf("can_destroy_blocks_in_creative", (Object)true).forGetter(ToolComponent::canDestroyBlocksInCreative)).apply((Applicative)instance, ToolComponent::new));
    public static final PacketCodec<RegistryByteBuf, ToolComponent> PACKET_CODEC = PacketCodec.tuple(Rule.PACKET_CODEC.collect(PacketCodecs.toList()), ToolComponent::rules, PacketCodecs.FLOAT, ToolComponent::defaultMiningSpeed, PacketCodecs.VAR_INT, ToolComponent::damagePerBlock, PacketCodecs.BOOLEAN, ToolComponent::canDestroyBlocksInCreative, ToolComponent::new);

    public float getSpeed(BlockState state) {
        for (Rule rule : this.rules) {
            if (!rule.speed.isPresent() || !state.isIn(rule.blocks)) continue;
            return rule.speed.get().floatValue();
        }
        return this.defaultMiningSpeed;
    }

    public boolean isCorrectForDrops(BlockState state) {
        for (Rule rule : this.rules) {
            if (!rule.correctForDrops.isPresent() || !state.isIn(rule.blocks)) continue;
            return rule.correctForDrops.get();
        }
        return false;
    }

    public static final class Rule
    extends Record {
        final RegistryEntryList<Block> blocks;
        final Optional<Float> speed;
        final Optional<Boolean> correctForDrops;
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("blocks").forGetter(Rule::blocks), (App)Codecs.POSITIVE_FLOAT.optionalFieldOf("speed").forGetter(Rule::speed), (App)Codec.BOOL.optionalFieldOf("correct_for_drops").forGetter(Rule::correctForDrops)).apply((Applicative)instance, Rule::new));
        public static final PacketCodec<RegistryByteBuf, Rule> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.registryEntryList(RegistryKeys.BLOCK), Rule::blocks, PacketCodecs.FLOAT.collect(PacketCodecs::optional), Rule::speed, PacketCodecs.BOOLEAN.collect(PacketCodecs::optional), Rule::correctForDrops, Rule::new);

        public Rule(RegistryEntryList<Block> blocks, Optional<Float> speed, Optional<Boolean> correctForDrops) {
            this.blocks = blocks;
            this.speed = speed;
            this.correctForDrops = correctForDrops;
        }

        public static Rule ofAlwaysDropping(RegistryEntryList<Block> blocks, float speed) {
            return new Rule(blocks, Optional.of(Float.valueOf(speed)), Optional.of(true));
        }

        public static Rule ofNeverDropping(RegistryEntryList<Block> blocks) {
            return new Rule(blocks, Optional.empty(), Optional.of(false));
        }

        public static Rule of(RegistryEntryList<Block> blocks, float speed) {
            return new Rule(blocks, Optional.of(Float.valueOf(speed)), Optional.empty());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Rule.class, "blocks;speed;correctForDrops", "blocks", "speed", "correctForDrops"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Rule.class, "blocks;speed;correctForDrops", "blocks", "speed", "correctForDrops"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Rule.class, "blocks;speed;correctForDrops", "blocks", "speed", "correctForDrops"}, this, object);
        }

        public RegistryEntryList<Block> blocks() {
            return this.blocks;
        }

        public Optional<Float> speed() {
            return this.speed;
        }

        public Optional<Boolean> correctForDrops() {
            return this.correctForDrops;
        }
    }
}

