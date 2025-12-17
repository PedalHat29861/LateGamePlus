/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class KnowledgeBookItem
extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnowledgeBookItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        List list = itemStack.getOrDefault(DataComponentTypes.RECIPES, List.of());
        itemStack.decrementUnlessCreative(1, user);
        if (list.isEmpty()) {
            return ActionResult.FAIL;
        }
        if (!world.isClient()) {
            ServerRecipeManager serverRecipeManager = world.getServer().getRecipeManager();
            ArrayList list2 = new ArrayList(list.size());
            for (RegistryKey registryKey : list) {
                Optional<RecipeEntry<?>> optional = serverRecipeManager.get(registryKey);
                if (optional.isPresent()) {
                    list2.add(optional.get());
                    continue;
                }
                LOGGER.error("Invalid recipe: {}", (Object)registryKey);
                return ActionResult.FAIL;
            }
            user.unlockRecipes(list2);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return ActionResult.SUCCESS;
    }
}

