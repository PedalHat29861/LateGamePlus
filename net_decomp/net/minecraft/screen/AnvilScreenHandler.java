/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.screen;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class AnvilScreenHandler
extends ForgingScreenHandler {
    public static final int INPUT_1_ID = 0;
    public static final int INPUT_2_ID = 1;
    public static final int OUTPUT_ID = 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean field_30752 = false;
    public static final int MAX_NAME_LENGTH = 50;
    private int repairItemUsage;
    private @Nullable String newItemName;
    private final Property levelCost = Property.create();
    private boolean keepSecondSlot = false;
    private static final int field_30753 = 0;
    private static final int field_30754 = 1;
    private static final int field_30755 = 1;
    private static final int field_30747 = 1;
    private static final int field_30748 = 2;
    private static final int field_30749 = 1;
    private static final int field_30750 = 1;
    private static final int INPUT_1_X = 27;
    private static final int INPUT_2_X = 76;
    private static final int OUTPUT_X = 134;
    private static final int SLOT_Y = 47;

    public AnvilScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public AnvilScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.ANVIL, syncId, inventory, context, AnvilScreenHandler.getForgingSlotsManager());
        this.addProperty(this.levelCost);
    }

    private static ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.builder().input(0, 27, 47, stack -> true).input(1, 76, 47, stack -> true).output(2, 134, 47).build();
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isIn(BlockTags.ANVIL);
    }

    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return (player.isInCreativeMode() || player.experienceLevel >= this.levelCost.get()) && this.levelCost.get() > 0;
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        if (!player.isInCreativeMode()) {
            player.addExperienceLevels(-this.levelCost.get());
        }
        if (this.repairItemUsage > 0) {
            ItemStack itemStack = this.input.getStack(1);
            if (!itemStack.isEmpty() && itemStack.getCount() > this.repairItemUsage) {
                itemStack.decrement(this.repairItemUsage);
                this.input.setStack(1, itemStack);
            } else {
                this.input.setStack(1, ItemStack.EMPTY);
            }
        } else if (!this.keepSecondSlot) {
            this.input.setStack(1, ItemStack.EMPTY);
        }
        this.levelCost.set(0);
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
            if (!StringHelper.isBlank(this.newItemName) && !this.input.getStack(0).getName().getString().equals(this.newItemName)) {
                serverPlayerEntity.getTextStream().filterText(this.newItemName);
            }
        }
        this.input.setStack(0, ItemStack.EMPTY);
        this.context.run((world, pos) -> {
            BlockState blockState = world.getBlockState((BlockPos)pos);
            if (!player.isInCreativeMode() && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12f) {
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock((BlockPos)pos, false);
                    world.syncWorldEvent(1029, (BlockPos)pos, 0);
                } else {
                    world.setBlockState((BlockPos)pos, blockState2, 2);
                    world.syncWorldEvent(1030, (BlockPos)pos, 0);
                }
            } else {
                world.syncWorldEvent(1030, (BlockPos)pos, 0);
            }
        });
    }

    @Override
    public void updateResult() {
        int k;
        ItemStack itemStack = this.input.getStack(0);
        this.keepSecondSlot = false;
        this.levelCost.set(1);
        int i = 0;
        long l = 0L;
        int j = 0;
        if (itemStack.isEmpty() || !EnchantmentHelper.canHaveEnchantments(itemStack)) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            return;
        }
        ItemStack itemStack2 = itemStack.copy();
        ItemStack itemStack3 = this.input.getStack(1);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(itemStack2));
        l += (long)itemStack.getOrDefault(DataComponentTypes.REPAIR_COST, 0).intValue() + (long)itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0).intValue();
        this.repairItemUsage = 0;
        if (!itemStack3.isEmpty()) {
            boolean bl = itemStack3.contains(DataComponentTypes.STORED_ENCHANTMENTS);
            if (itemStack2.isDamageable() && itemStack.canRepairWith(itemStack3)) {
                int m;
                k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
                if (k <= 0) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    return;
                }
                for (m = 0; k > 0 && m < itemStack3.getCount(); ++m) {
                    int n = itemStack2.getDamage() - k;
                    itemStack2.setDamage(n);
                    ++i;
                    k = Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / 4);
                }
                this.repairItemUsage = m;
            } else {
                if (!(bl || itemStack2.isOf(itemStack3.getItem()) && itemStack2.isDamageable())) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    return;
                }
                if (itemStack2.isDamageable() && !bl) {
                    int k2 = itemStack.getMaxDamage() - itemStack.getDamage();
                    int m = itemStack3.getMaxDamage() - itemStack3.getDamage();
                    int n = m + itemStack2.getMaxDamage() * 12 / 100;
                    int o = k2 + n;
                    int p = itemStack2.getMaxDamage() - o;
                    if (p < 0) {
                        p = 0;
                    }
                    if (p < itemStack2.getDamage()) {
                        itemStack2.setDamage(p);
                        i += 2;
                    }
                }
                ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(itemStack3);
                boolean bl2 = false;
                boolean bl3 = false;
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
                    int r;
                    RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
                    int q = builder.getLevel(registryEntry);
                    r = q == (r = entry.getIntValue()) ? r + 1 : Math.max(r, q);
                    Enchantment enchantment = (Enchantment)registryEntry.value();
                    boolean bl4 = enchantment.isAcceptableItem(itemStack);
                    if (this.player.isInCreativeMode() || itemStack.isOf(Items.ENCHANTED_BOOK)) {
                        bl4 = true;
                    }
                    for (RegistryEntry<Enchantment> registryEntry2 : builder.getEnchantments()) {
                        if (registryEntry2.equals(registryEntry) || Enchantment.canBeCombined(registryEntry, registryEntry2)) continue;
                        bl4 = false;
                        ++i;
                    }
                    if (!bl4) {
                        bl3 = true;
                        continue;
                    }
                    bl2 = true;
                    if (r > enchantment.getMaxLevel()) {
                        r = enchantment.getMaxLevel();
                    }
                    builder.set(registryEntry, r);
                    int s = enchantment.getAnvilCost();
                    if (bl) {
                        s = Math.max(1, s / 2);
                    }
                    i += s * r;
                    if (itemStack.getCount() <= 1) continue;
                    i = 40;
                }
                if (bl3 && !bl2) {
                    this.output.setStack(0, ItemStack.EMPTY);
                    this.levelCost.set(0);
                    return;
                }
            }
        }
        if (this.newItemName == null || StringHelper.isBlank(this.newItemName)) {
            if (itemStack.contains(DataComponentTypes.CUSTOM_NAME)) {
                j = 1;
                i += j;
                itemStack2.remove(DataComponentTypes.CUSTOM_NAME);
            }
        } else if (!this.newItemName.equals(itemStack.getName().getString())) {
            j = 1;
            i += j;
            itemStack2.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.newItemName));
        }
        int t = i <= 0 ? 0 : (int)MathHelper.clamp(l + (long)i, 0L, Integer.MAX_VALUE);
        this.levelCost.set(t);
        if (i <= 0) {
            itemStack2 = ItemStack.EMPTY;
        }
        if (j == i && j > 0) {
            if (this.levelCost.get() >= 40) {
                this.levelCost.set(39);
            }
            this.keepSecondSlot = true;
        }
        if (this.levelCost.get() >= 40 && !this.player.isInCreativeMode()) {
            itemStack2 = ItemStack.EMPTY;
        }
        if (!itemStack2.isEmpty()) {
            k = itemStack2.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
            if (k < itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0)) {
                k = itemStack3.getOrDefault(DataComponentTypes.REPAIR_COST, 0);
            }
            if (j != i || j == 0) {
                k = AnvilScreenHandler.getNextCost(k);
            }
            itemStack2.set(DataComponentTypes.REPAIR_COST, k);
            EnchantmentHelper.set(itemStack2, builder.build());
        }
        this.output.setStack(0, itemStack2);
        this.sendContentUpdates();
    }

    public static int getNextCost(int cost) {
        return (int)Math.min((long)cost * 2L + 1L, Integer.MAX_VALUE);
    }

    public boolean setNewItemName(String newItemName) {
        String string = AnvilScreenHandler.sanitize(newItemName);
        if (string == null || string.equals(this.newItemName)) {
            return false;
        }
        this.newItemName = string;
        if (this.getSlot(2).hasStack()) {
            ItemStack itemStack = this.getSlot(2).getStack();
            if (StringHelper.isBlank(string)) {
                itemStack.remove(DataComponentTypes.CUSTOM_NAME);
            } else {
                itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(string));
            }
        }
        this.updateResult();
        return true;
    }

    private static @Nullable String sanitize(String name) {
        String string = StringHelper.stripInvalidChars(name);
        if (string.length() <= 50) {
            return string;
        }
        return null;
    }

    public int getLevelCost() {
        return this.levelCost.get();
    }
}

