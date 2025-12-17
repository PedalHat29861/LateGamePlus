/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SmithingTemplateItem
extends Item {
    private static final Formatting TITLE_FORMATTING = Formatting.GRAY;
    private static final Formatting DESCRIPTION_FORMATTING = Formatting.BLUE;
    private static final Text INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.ingredients"))).formatted(TITLE_FORMATTING);
    private static final Text APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.applies_to"))).formatted(TITLE_FORMATTING);
    private static final Text SMITHING_TEMPLATE_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template"))).formatted(TITLE_FORMATTING);
    private static final Text ARMOR_TRIM_APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.armor_trim.applies_to"))).formatted(DESCRIPTION_FORMATTING);
    private static final Text ARMOR_TRIM_INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.armor_trim.ingredients"))).formatted(DESCRIPTION_FORMATTING);
    private static final Text ARMOR_TRIM_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.armor_trim.base_slot_description")));
    private static final Text ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.armor_trim.additions_slot_description")));
    private static final Text NETHERITE_UPGRADE_APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.netherite_upgrade.applies_to"))).formatted(DESCRIPTION_FORMATTING);
    private static final Text NETHERITE_UPGRADE_INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.netherite_upgrade.ingredients"))).formatted(DESCRIPTION_FORMATTING);
    private static final Text NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.netherite_upgrade.base_slot_description")));
    private static final Text NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", Identifier.ofVanilla("smithing_template.netherite_upgrade.additions_slot_description")));
    private static final Identifier EMPTY_ARMOR_SLOT_HELMET_TEXTURE = Identifier.ofVanilla("container/slot/helmet");
    private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE = Identifier.ofVanilla("container/slot/chestplate");
    private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE = Identifier.ofVanilla("container/slot/leggings");
    private static final Identifier EMPTY_ARMOR_SLOT_BOOTS_TEXTURE = Identifier.ofVanilla("container/slot/boots");
    private static final Identifier EMPTY_SLOT_HOE_TEXTURE = Identifier.ofVanilla("container/slot/hoe");
    private static final Identifier EMPTY_SLOT_AXE_TEXTURE = Identifier.ofVanilla("container/slot/axe");
    private static final Identifier EMPTY_SLOT_SWORD_TEXTURE = Identifier.ofVanilla("container/slot/sword");
    private static final Identifier EMPTY_SLOT_SHOVEL_TEXTURE = Identifier.ofVanilla("container/slot/shovel");
    private static final Identifier EMPTY_SLOT_SPEAR_TEXTURE = Identifier.ofVanilla("container/slot/spear");
    private static final Identifier EMPTY_SLOT_PICKAXE_TEXTURE = Identifier.ofVanilla("container/slot/pickaxe");
    private static final Identifier EMPTY_SLOT_INGOT_TEXTURE = Identifier.ofVanilla("container/slot/ingot");
    private static final Identifier EMPTY_SLOT_REDSTONE_DUST_TEXTURE = Identifier.ofVanilla("container/slot/redstone_dust");
    private static final Identifier EMPTY_SLOT_QUARTZ_TEXTURE = Identifier.ofVanilla("container/slot/quartz");
    private static final Identifier EMPTY_SLOT_EMERALD_TEXTURE = Identifier.ofVanilla("container/slot/emerald");
    private static final Identifier EMPTY_SLOT_DIAMOND_TEXTURE = Identifier.ofVanilla("container/slot/diamond");
    private static final Identifier EMPTY_SLOT_LAPIS_LAZULI_TEXTURE = Identifier.ofVanilla("container/slot/lapis_lazuli");
    private static final Identifier EMPTY_SLOT_AMETHYST_SHARD_TEXTURE = Identifier.ofVanilla("container/slot/amethyst_shard");
    private static final Identifier EMPTY_SLOT_NAUTILUS_ARMOR_TEXTURE = Identifier.ofVanilla("container/slot/nautilus_armor");
    private final Text appliesToText;
    private final Text ingredientsText;
    private final Text baseSlotDescriptionText;
    private final Text additionsSlotDescriptionText;
    private final List<Identifier> emptyBaseSlotTextures;
    private final List<Identifier> emptyAdditionsSlotTextures;

    public SmithingTemplateItem(Text appliesToText, Text ingredientsText, Text baseSlotDescriptionText, Text additionsSlotDescriptionText, List<Identifier> emptyBaseSlotTextures, List<Identifier> emptyAdditionsSlotTextures, Item.Settings settings) {
        super(settings);
        this.appliesToText = appliesToText;
        this.ingredientsText = ingredientsText;
        this.baseSlotDescriptionText = baseSlotDescriptionText;
        this.additionsSlotDescriptionText = additionsSlotDescriptionText;
        this.emptyBaseSlotTextures = emptyBaseSlotTextures;
        this.emptyAdditionsSlotTextures = emptyAdditionsSlotTextures;
    }

    public static SmithingTemplateItem of(Item.Settings settings) {
        return new SmithingTemplateItem(ARMOR_TRIM_APPLIES_TO_TEXT, ARMOR_TRIM_INGREDIENTS_TEXT, ARMOR_TRIM_BASE_SLOT_DESCRIPTION_TEXT, ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION_TEXT, SmithingTemplateItem.getArmorTrimEmptyBaseSlotTextures(), SmithingTemplateItem.getArmorTrimEmptyAdditionsSlotTextures(), settings);
    }

    public static SmithingTemplateItem createNetheriteUpgrade(Item.Settings settings) {
        return new SmithingTemplateItem(NETHERITE_UPGRADE_APPLIES_TO_TEXT, NETHERITE_UPGRADE_INGREDIENTS_TEXT, NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT, NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT, SmithingTemplateItem.getNetheriteUpgradeEmptyBaseSlotTextures(), SmithingTemplateItem.getNetheriteUpgradeEmptyAdditionsSlotTextures(), settings);
    }

    private static List<Identifier> getArmorTrimEmptyBaseSlotTextures() {
        return List.of(EMPTY_ARMOR_SLOT_HELMET_TEXTURE, EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE, EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE, EMPTY_ARMOR_SLOT_BOOTS_TEXTURE);
    }

    private static List<Identifier> getArmorTrimEmptyAdditionsSlotTextures() {
        return List.of(EMPTY_SLOT_INGOT_TEXTURE, EMPTY_SLOT_REDSTONE_DUST_TEXTURE, EMPTY_SLOT_LAPIS_LAZULI_TEXTURE, EMPTY_SLOT_QUARTZ_TEXTURE, EMPTY_SLOT_DIAMOND_TEXTURE, EMPTY_SLOT_EMERALD_TEXTURE, EMPTY_SLOT_AMETHYST_SHARD_TEXTURE);
    }

    private static List<Identifier> getNetheriteUpgradeEmptyBaseSlotTextures() {
        return List.of(EMPTY_ARMOR_SLOT_HELMET_TEXTURE, EMPTY_SLOT_SWORD_TEXTURE, EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE, EMPTY_SLOT_PICKAXE_TEXTURE, EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE, EMPTY_SLOT_AXE_TEXTURE, EMPTY_ARMOR_SLOT_BOOTS_TEXTURE, EMPTY_SLOT_HOE_TEXTURE, EMPTY_SLOT_SHOVEL_TEXTURE, EMPTY_SLOT_NAUTILUS_ARMOR_TEXTURE, EMPTY_SLOT_SPEAR_TEXTURE);
    }

    private static List<Identifier> getNetheriteUpgradeEmptyAdditionsSlotTextures() {
        return List.of(EMPTY_SLOT_INGOT_TEXTURE);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(SMITHING_TEMPLATE_TEXT);
        textConsumer.accept(ScreenTexts.EMPTY);
        textConsumer.accept(APPLIES_TO_TEXT);
        textConsumer.accept(ScreenTexts.space().append(this.appliesToText));
        textConsumer.accept(INGREDIENTS_TEXT);
        textConsumer.accept(ScreenTexts.space().append(this.ingredientsText));
    }

    public Text getBaseSlotDescription() {
        return this.baseSlotDescriptionText;
    }

    public Text getAdditionsSlotDescription() {
        return this.additionsSlotDescriptionText;
    }

    public List<Identifier> getEmptyBaseSlotTextures() {
        return this.emptyBaseSlotTextures;
    }

    public List<Identifier> getEmptyAdditionsSlotTextures() {
        return this.emptyAdditionsSlotTextures;
    }
}

