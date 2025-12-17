/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;

public record BedRule(Condition canSleep, Condition canSetSpawn, boolean explodes, Optional<Text> errorMessage) {
    public static final BedRule OVERWORLD = new BedRule(Condition.WHEN_DARK, Condition.ALWAYS, false, Optional.of(Text.translatable("block.minecraft.bed.no_sleep")));
    public static final BedRule OTHER_DIMENSION = new BedRule(Condition.NEVER, Condition.NEVER, true, Optional.empty());
    public static final Codec<BedRule> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Condition.CODEC.fieldOf("can_sleep").forGetter(BedRule::canSleep), (App)Condition.CODEC.fieldOf("can_set_spawn").forGetter(BedRule::canSetSpawn), (App)Codec.BOOL.optionalFieldOf("explodes", (Object)false).forGetter(BedRule::explodes), (App)TextCodecs.CODEC.optionalFieldOf("error_message").forGetter(BedRule::errorMessage)).apply((Applicative)instance, BedRule::new));

    public boolean canSleep(World world) {
        return this.canSleep.test(world);
    }

    public boolean canSetSpawn(World world) {
        return this.canSetSpawn.test(world);
    }

    public PlayerEntity.SleepFailureReason getFailureReason() {
        return new PlayerEntity.SleepFailureReason(this.errorMessage.orElse(null));
    }

    public static final class Condition
    extends Enum<Condition>
    implements StringIdentifiable {
        public static final /* enum */ Condition ALWAYS = new Condition("always");
        public static final /* enum */ Condition WHEN_DARK = new Condition("when_dark");
        public static final /* enum */ Condition NEVER = new Condition("never");
        public static final Codec<Condition> CODEC;
        private final String name;
        private static final /* synthetic */ Condition[] field_63711;

        public static Condition[] values() {
            return (Condition[])field_63711.clone();
        }

        public static Condition valueOf(String string) {
            return Enum.valueOf(Condition.class, string);
        }

        private Condition(String name) {
            this.name = name;
        }

        public boolean test(World world) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> true;
                case 1 -> world.isNight();
                case 2 -> false;
            };
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ Condition[] method_75645() {
            return new Condition[]{ALWAYS, WHEN_DARK, NEVER};
        }

        static {
            field_63711 = Condition.method_75645();
            CODEC = StringIdentifiable.createCodec(Condition::values);
        }
    }
}

