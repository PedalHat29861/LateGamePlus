/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.item.equipment.trim;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.trim.ArmorTrimAssets;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record ArmorTrim(RegistryEntry<ArmorTrimMaterial> material, RegistryEntry<ArmorTrimPattern> pattern) implements TooltipAppender
{
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ArmorTrimMaterial.ENTRY_CODEC.fieldOf("material").forGetter(ArmorTrim::material), (App)ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)).apply((Applicative)instance, ArmorTrim::new));
    public static final PacketCodec<RegistryByteBuf, ArmorTrim> PACKET_CODEC = PacketCodec.tuple(ArmorTrimMaterial.ENTRY_PACKET_CODEC, ArmorTrim::material, ArmorTrimPattern.ENTRY_PACKET_CODEC, ArmorTrim::pattern, ArmorTrim::new);
    private static final Text UPGRADE_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.upgrade"))).formatted(Formatting.GRAY);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(UPGRADE_TEXT);
        textConsumer.accept(ScreenTexts.space().append(this.pattern.value().getDescription(this.material)));
        textConsumer.accept(ScreenTexts.space().append(this.material.value().description()));
    }

    public Identifier getTextureId(String trimsDirectory, RegistryKey<EquipmentAsset> equipmentAsset) {
        ArmorTrimAssets.AssetId assetId = this.material().value().assets().getAssetId(equipmentAsset);
        return this.pattern().value().assetId().withPath(patternId -> trimsDirectory + "/" + patternId + "_" + assetId.suffix());
    }
}

