/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.component.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BookContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.StringHelper;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

public record WrittenBookContentComponent(RawFilteredPair<String> title, String author, int generation, List<RawFilteredPair<Text>> pages, boolean resolved) implements BookContent<Text, WrittenBookContentComponent>,
TooltipAppender
{
    public static final WrittenBookContentComponent DEFAULT = new WrittenBookContentComponent(RawFilteredPair.of(""), "", 0, List.of(), true);
    public static final int MAX_SERIALIZED_PAGE_LENGTH = Short.MAX_VALUE;
    public static final int field_49377 = 16;
    public static final int MAX_TITLE_LENGTH = 32;
    public static final int MAX_GENERATION = 3;
    public static final int UNCOPIABLE_GENERATION = 2;
    public static final Codec<Text> PAGE_CODEC = TextCodecs.withJsonLengthLimit(Short.MAX_VALUE);
    public static final Codec<List<RawFilteredPair<Text>>> PAGES_CODEC = WrittenBookContentComponent.createPagesCodec(PAGE_CODEC);
    public static final Codec<WrittenBookContentComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RawFilteredPair.createCodec(Codec.string((int)0, (int)32)).fieldOf("title").forGetter(WrittenBookContentComponent::title), (App)Codec.STRING.fieldOf("author").forGetter(WrittenBookContentComponent::author), (App)Codecs.rangedInt(0, 3).optionalFieldOf("generation", (Object)0).forGetter(WrittenBookContentComponent::generation), (App)PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(WrittenBookContentComponent::pages), (App)Codec.BOOL.optionalFieldOf("resolved", (Object)false).forGetter(WrittenBookContentComponent::resolved)).apply((Applicative)instance, WrittenBookContentComponent::new));
    public static final PacketCodec<RegistryByteBuf, WrittenBookContentComponent> PACKET_CODEC = PacketCodec.tuple(RawFilteredPair.createPacketCodec(PacketCodecs.string(32)), WrittenBookContentComponent::title, PacketCodecs.STRING, WrittenBookContentComponent::author, PacketCodecs.VAR_INT, WrittenBookContentComponent::generation, RawFilteredPair.createPacketCodec(TextCodecs.REGISTRY_PACKET_CODEC).collect(PacketCodecs.toList()), WrittenBookContentComponent::pages, PacketCodecs.BOOLEAN, WrittenBookContentComponent::resolved, WrittenBookContentComponent::new);

    public WrittenBookContentComponent {
        if (generation < 0 || generation > 3) {
            throw new IllegalArgumentException("Generation was " + generation + ", but must be between 0 and 3");
        }
    }

    private static Codec<RawFilteredPair<Text>> createPageCodec(Codec<Text> textCodec) {
        return RawFilteredPair.createCodec(textCodec);
    }

    public static Codec<List<RawFilteredPair<Text>>> createPagesCodec(Codec<Text> textCodec) {
        return WrittenBookContentComponent.createPageCodec(textCodec).listOf();
    }

    public @Nullable WrittenBookContentComponent copy() {
        if (this.generation >= 2) {
            return null;
        }
        return new WrittenBookContentComponent(this.title, this.author, this.generation + 1, this.pages, this.resolved);
    }

    public static boolean resolveInStack(ItemStack stack, ServerCommandSource commandSource, @Nullable PlayerEntity player) {
        WrittenBookContentComponent writtenBookContentComponent = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (writtenBookContentComponent != null && !writtenBookContentComponent.resolved()) {
            WrittenBookContentComponent writtenBookContentComponent2 = writtenBookContentComponent.resolve(commandSource, player);
            if (writtenBookContentComponent2 != null) {
                stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentComponent2);
                return true;
            }
            stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, writtenBookContentComponent.asResolved());
        }
        return false;
    }

    public @Nullable WrittenBookContentComponent resolve(ServerCommandSource source, @Nullable PlayerEntity player) {
        if (this.resolved) {
            return null;
        }
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)this.pages.size());
        for (RawFilteredPair<Text> rawFilteredPair : this.pages) {
            Optional<RawFilteredPair<Text>> optional = WrittenBookContentComponent.resolve(source, player, rawFilteredPair);
            if (optional.isEmpty()) {
                return null;
            }
            builder.add(optional.get());
        }
        return new WrittenBookContentComponent(this.title, this.author, this.generation, (List<RawFilteredPair<Text>>)builder.build(), true);
    }

    public WrittenBookContentComponent asResolved() {
        return new WrittenBookContentComponent(this.title, this.author, this.generation, this.pages, true);
    }

    private static Optional<RawFilteredPair<Text>> resolve(ServerCommandSource source, @Nullable PlayerEntity player, RawFilteredPair<Text> page) {
        return page.resolve(text -> {
            try {
                MutableText text2 = Texts.parse(source, text, (Entity)player, 0);
                if (WrittenBookContentComponent.exceedsSerializedLengthLimit(text2, source.getRegistryManager())) {
                    return Optional.empty();
                }
                return Optional.of(text2);
            }
            catch (Exception exception) {
                return Optional.of(text);
            }
        });
    }

    private static boolean exceedsSerializedLengthLimit(Text text, RegistryWrapper.WrapperLookup registries) {
        DataResult dataResult = TextCodecs.CODEC.encodeStart(registries.getOps(JsonOps.INSTANCE), (Object)text);
        return dataResult.isSuccess() && JsonHelper.isTooLarge((JsonElement)dataResult.getOrThrow(), Short.MAX_VALUE);
    }

    public List<Text> getPages(boolean shouldFilter) {
        return Lists.transform(this.pages, page -> (Text)page.get(shouldFilter));
    }

    @Override
    public WrittenBookContentComponent withPages(List<RawFilteredPair<Text>> list) {
        return new WrittenBookContentComponent(this.title, this.author, this.generation, list, false);
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        if (!StringHelper.isBlank(this.author)) {
            textConsumer.accept(Text.translatable("book.byAuthor", this.author).formatted(Formatting.GRAY));
        }
        textConsumer.accept(Text.translatable("book.generation." + this.generation).formatted(Formatting.GRAY));
    }

    @Override
    public /* synthetic */ Object withPages(List pages) {
        return this.withPages(pages);
    }
}

