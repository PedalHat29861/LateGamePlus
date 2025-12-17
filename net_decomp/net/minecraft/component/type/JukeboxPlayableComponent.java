/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public record JukeboxPlayableComponent(LazyRegistryEntryReference<JukeboxSong> song) implements TooltipAppender
{
    public static final Codec<JukeboxPlayableComponent> CODEC = LazyRegistryEntryReference.createCodec(RegistryKeys.JUKEBOX_SONG, JukeboxSong.ENTRY_CODEC).xmap(JukeboxPlayableComponent::new, JukeboxPlayableComponent::song);
    public static final PacketCodec<RegistryByteBuf, JukeboxPlayableComponent> PACKET_CODEC = PacketCodec.tuple(LazyRegistryEntryReference.createPacketCodec(RegistryKeys.JUKEBOX_SONG, JukeboxSong.ENTRY_PACKET_CODEC), JukeboxPlayableComponent::song, JukeboxPlayableComponent::new);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        RegistryWrapper.WrapperLookup wrapperLookup = context.getRegistryLookup();
        if (wrapperLookup != null) {
            this.song.resolveEntry(wrapperLookup).ifPresent(entry -> {
                Text text = Texts.withStyle(((JukeboxSong)entry.value()).description(), Style.EMPTY.withColor(Formatting.GRAY));
                textConsumer.accept(text);
            });
        }
    }

    public static ActionResult tryPlayStack(World world, BlockPos pos, ItemStack stack, PlayerEntity player) {
        JukeboxPlayableComponent jukeboxPlayableComponent = stack.get(DataComponentTypes.JUKEBOX_PLAYABLE);
        if (jukeboxPlayableComponent == null) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.isOf(Blocks.JUKEBOX) || blockState.get(JukeboxBlock.HAS_RECORD).booleanValue()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!world.isClient()) {
            ItemStack itemStack = stack.splitUnlessCreative(1, player);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof JukeboxBlockEntity) {
                JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
                jukeboxBlockEntity.setStack(itemStack);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
            }
            player.incrementStat(Stats.PLAY_RECORD);
        }
        return ActionResult.SUCCESS;
    }
}

