/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.waypoint;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.waypoint.WaypointStyle;
import net.minecraft.world.waypoint.WaypointStyles;

public interface Waypoint {
    public static final int DEFAULT_PLAYER_RANGE = 60000000;
    public static final EntityAttributeModifier DISABLE_TRACKING = new EntityAttributeModifier(Identifier.ofVanilla("waypoint_transmit_range_hide"), -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public static Item.Settings disableTracking(Item.Settings settings) {
        return settings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder().add(EntityAttributes.WAYPOINT_TRANSMIT_RANGE, DISABLE_TRACKING, AttributeModifierSlot.HEAD, AttributeModifiersComponent.Display.getHidden()).build());
    }

    public static class Config {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryKey.createCodec(WaypointStyles.REGISTRY).fieldOf("style").forGetter(config -> config.style), (App)Codecs.RGB.optionalFieldOf("color").forGetter(config -> config.color)).apply((Applicative)instance, Config::new));
        public static final PacketCodec<ByteBuf, Config> PACKET_CODEC = PacketCodec.tuple(RegistryKey.createPacketCodec(WaypointStyles.REGISTRY), config -> config.style, PacketCodecs.optional(PacketCodecs.RGB), config -> config.color, Config::new);
        public static final Config DEFAULT = new Config();
        public RegistryKey<WaypointStyle> style = WaypointStyles.DEFAULT;
        public Optional<Integer> color = Optional.empty();

        public Config() {
        }

        private Config(RegistryKey<WaypointStyle> style, Optional<Integer> color) {
            this.style = style;
            this.color = color;
        }

        public boolean hasCustomStyle() {
            return this.style != WaypointStyles.DEFAULT || this.color.isPresent();
        }

        public Config withTeamColorOf(LivingEntity entity) {
            RegistryKey<WaypointStyle> registryKey = this.getStyle();
            Optional<Integer> optional = this.color.or(() -> Optional.ofNullable(entity.getScoreboardTeam()).map(team -> team.getColor().getColorValue()).map(color -> color == 0 ? -13619152 : color));
            if (registryKey == this.style && optional.isEmpty()) {
                return this;
            }
            return new Config(registryKey, optional);
        }

        public void copyFrom(Config other) {
            this.color = other.color;
            this.style = other.style;
        }

        private RegistryKey<WaypointStyle> getStyle() {
            return this.style != WaypointStyles.DEFAULT ? this.style : WaypointStyles.DEFAULT;
        }
    }
}

