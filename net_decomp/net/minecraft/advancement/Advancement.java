/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public record Advancement(Optional<Identifier> parent, Optional<AdvancementDisplay> display, AdvancementRewards rewards, Map<String, AdvancementCriterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent, Optional<Text> name) {
    private static final Codec<Map<String, AdvancementCriterion<?>>> CRITERIA_CODEC = Codec.unboundedMap((Codec)Codec.STRING, AdvancementCriterion.CODEC).validate((T criteria) -> criteria.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success((Object)criteria));
    public static final Codec<Advancement> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Identifier.CODEC.optionalFieldOf("parent").forGetter(Advancement::parent), (App)AdvancementDisplay.CODEC.optionalFieldOf("display").forGetter(Advancement::display), (App)AdvancementRewards.CODEC.optionalFieldOf("rewards", (Object)AdvancementRewards.NONE).forGetter(Advancement::rewards), (App)CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria), (App)AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter(advancement -> Optional.of(advancement.requirements())), (App)Codec.BOOL.optionalFieldOf("sends_telemetry_event", (Object)false).forGetter(Advancement::sendsTelemetryEvent)).apply((Applicative)instance, (parent, display, rewards, criteria, requirements, sendsTelemetryEvent) -> {
        AdvancementRequirements advancementRequirements = requirements.orElseGet(() -> AdvancementRequirements.allOf(criteria.keySet()));
        return new Advancement((Optional<Identifier>)parent, (Optional<AdvancementDisplay>)display, (AdvancementRewards)rewards, (Map<String, AdvancementCriterion<?>>)criteria, advancementRequirements, (boolean)sendsTelemetryEvent);
    })).validate(Advancement::validate);
    public static final PacketCodec<RegistryByteBuf, Advancement> PACKET_CODEC = PacketCodec.of(Advancement::write, Advancement::read);

    public Advancement(Optional<Identifier> parent, Optional<AdvancementDisplay> display, AdvancementRewards rewards, Map<String, AdvancementCriterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent) {
        this(parent, display, rewards, Map.copyOf(criteria), requirements, sendsTelemetryEvent, display.map(Advancement::createNameFromDisplay));
    }

    private static DataResult<Advancement> validate(Advancement advancement) {
        return advancement.requirements().validate(advancement.criteria().keySet()).map(validated -> advancement);
    }

    private static Text createNameFromDisplay(AdvancementDisplay display) {
        Text text = display.getTitle();
        Formatting formatting = display.getFrame().getTitleFormat();
        MutableText text2 = Texts.setStyleIfAbsent(text.copy(), Style.EMPTY.withColor(formatting)).append("\n").append(display.getDescription());
        MutableText text3 = text.copy().styled(style -> style.withHoverEvent(new HoverEvent.ShowText(text2)));
        return Texts.bracketed(text3).formatted(formatting);
    }

    public static Text getNameFromIdentity(AdvancementEntry identifiedAdvancement) {
        return identifiedAdvancement.value().name().orElseGet(() -> Text.literal(identifiedAdvancement.id().toString()));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeOptional(this.parent, PacketByteBuf::writeIdentifier);
        AdvancementDisplay.PACKET_CODEC.collect(PacketCodecs::optional).encode(buf, this.display);
        this.requirements.writeRequirements(buf);
        buf.writeBoolean(this.sendsTelemetryEvent);
    }

    private static Advancement read(RegistryByteBuf buf) {
        return new Advancement(buf.readOptional(PacketByteBuf::readIdentifier), (Optional)AdvancementDisplay.PACKET_CODEC.collect(PacketCodecs::optional).decode(buf), AdvancementRewards.NONE, Map.of(), new AdvancementRequirements(buf), buf.readBoolean());
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ErrorReporter errorReporter, RegistryEntryLookup.RegistryLookup lookup) {
        this.criteria.forEach((name, criterion) -> {
            LootContextPredicateValidator lootContextPredicateValidator = new LootContextPredicateValidator(errorReporter.makeChild(new ErrorReporter.CriterionContext((String)name)), lookup);
            criterion.conditions().validate(lootContextPredicateValidator);
        });
    }

    public static class Builder {
        private Optional<Identifier> parentObj = Optional.empty();
        private Optional<AdvancementDisplay> display = Optional.empty();
        private AdvancementRewards rewards = AdvancementRewards.NONE;
        private final ImmutableMap.Builder<String, AdvancementCriterion<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private AdvancementRequirements.CriterionMerger merger = AdvancementRequirements.CriterionMerger.AND;
        private boolean sendsTelemetryEvent;

        public static Builder create() {
            return new Builder().sendsTelemetryEvent();
        }

        public static Builder createUntelemetered() {
            return new Builder();
        }

        public Builder parent(AdvancementEntry parent) {
            this.parentObj = Optional.of(parent.id());
            return this;
        }

        @Deprecated(forRemoval=true)
        public Builder parent(Identifier parentId) {
            this.parentObj = Optional.of(parentId);
            return this;
        }

        public Builder display(ItemStack icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return this.display(new AdvancementDisplay(icon, title, description, Optional.ofNullable(background).map(AssetInfo.TextureAssetInfo::new), frame, showToast, announceToChat, hidden));
        }

        public Builder display(ItemConvertible icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return this.display(new AdvancementDisplay(new ItemStack(icon.asItem()), title, description, Optional.ofNullable(background).map(AssetInfo.TextureAssetInfo::new), frame, showToast, announceToChat, hidden));
        }

        public Builder display(AdvancementDisplay display) {
            this.display = Optional.of(display);
            return this;
        }

        public Builder rewards(AdvancementRewards.Builder builder) {
            return this.rewards(builder.build());
        }

        public Builder rewards(AdvancementRewards rewards) {
            this.rewards = rewards;
            return this;
        }

        public Builder criterion(String name, AdvancementCriterion<?> criterion) {
            this.criteria.put((Object)name, criterion);
            return this;
        }

        public Builder criteriaMerger(AdvancementRequirements.CriterionMerger merger) {
            this.merger = merger;
            return this;
        }

        public Builder requirements(AdvancementRequirements requirements) {
            this.requirements = Optional.of(requirements);
            return this;
        }

        public Builder sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementEntry build(Identifier id) {
            ImmutableMap map = this.criteria.buildOrThrow();
            AdvancementRequirements advancementRequirements = this.requirements.orElseGet(() -> this.method_53633((Map)map));
            return new AdvancementEntry(id, new Advancement(this.parentObj, this.display, this.rewards, (Map<String, AdvancementCriterion<?>>)map, advancementRequirements, this.sendsTelemetryEvent));
        }

        public AdvancementEntry build(Consumer<AdvancementEntry> exporter, String id) {
            AdvancementEntry advancementEntry = this.build(Identifier.of(id));
            exporter.accept(advancementEntry);
            return advancementEntry;
        }

        private /* synthetic */ AdvancementRequirements method_53633(Map map) {
            return this.merger.create(map.keySet());
        }
    }
}

