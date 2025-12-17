/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.mob;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.ModelAndTexture;
import net.minecraft.util.StringIdentifiable;

public record ZombieNautilusVariant(ModelAndTexture<Model> modelAndTexture, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<ZombieNautilusVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ModelAndTexture.createMapCodec(Model.CODEC, Model.NORMAL).forGetter(ZombieNautilusVariant::modelAndTexture), (App)SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions").forGetter(ZombieNautilusVariant::spawnConditions)).apply((Applicative)instance, ZombieNautilusVariant::new));
    public static final Codec<ZombieNautilusVariant> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ModelAndTexture.createMapCodec(Model.CODEC, Model.NORMAL).forGetter(ZombieNautilusVariant::modelAndTexture)).apply((Applicative)instance, ZombieNautilusVariant::new));
    public static final Codec<RegistryEntry<ZombieNautilusVariant>> ENTRY_CODEC = RegistryFixedCodec.of(RegistryKeys.ZOMBIE_NAUTILUS_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<ZombieNautilusVariant>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.ZOMBIE_NAUTILUS_VARIANT);

    private ZombieNautilusVariant(ModelAndTexture<Model> modelAndTexture) {
        this(modelAndTexture, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<VariantSelectorProvider.Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }

    public static final class Model
    extends Enum<Model>
    implements StringIdentifiable {
        public static final /* enum */ Model NORMAL = new Model("normal");
        public static final /* enum */ Model WARM = new Model("warm");
        public static final Codec<Model> CODEC;
        private final String id;
        private static final /* synthetic */ Model[] field_64369;

        public static Model[] values() {
            return (Model[])field_64369.clone();
        }

        public static Model valueOf(String string) {
            return Enum.valueOf(Model.class, string);
        }

        private Model(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Model[] method_76447() {
            return new Model[]{NORMAL, WARM};
        }

        static {
            field_64369 = Model.method_76447();
            CODEC = StringIdentifiable.createCodec(Model::values);
        }
    }
}

