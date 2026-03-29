package com.pedalhat.lategameplus.recipe;

import com.mojang.serialization.MapCodec;
import com.pedalhat.lategameplus.registry.ModItems;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Special crafting recipe that converts a lodestone-bound compass plus
 * shaped ingredients into a Lodestone Warp item. The resulting item inherits
 * the {@link DataComponents#LODESTONE_TRACKER} from the center compass.
 *
 * Pattern (3x3):
 *  D Y D
 *  N C N
 *  D E D
 *  D = Diamond, Y = Ender Eye, E = Ender Pearl, N = Netherite Nugget, C = Lodestone-bound Compass
 */
public class WarpFromLodestoneCompassRecipe extends CustomRecipe {
    public static final WarpFromLodestoneCompassRecipe INSTANCE = new WarpFromLodestoneCompassRecipe();
    public static final MapCodec<WarpFromLodestoneCompassRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, WarpFromLodestoneCompassRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<WarpFromLodestoneCompassRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public WarpFromLodestoneCompassRecipe() {
    }

    private static int idx(int x, int y, int w) {
        return y * w + x;
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        if (input.width() != 3 || input.height() != 3) return false;

        List<ItemStack> s = input.items();

        if (!s.get(idx(0,0,3)).is(Items.DIAMOND))     return false;
        if (!s.get(idx(1,0,3)).is(Items.ENDER_EYE))   return false;
        if (!s.get(idx(2,0,3)).is(Items.DIAMOND))     return false;

        if (!s.get(idx(0,1,3)).is(ModItems.NETHERITE_NUGGET)) return false;

        ItemStack compass = s.get(idx(1,1,3));
        if (!compass.is(Items.COMPASS)) return false;
        LodestoneTracker tracker = compass.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) return false;

        if (!s.get(idx(2,1,3)).is(ModItems.NETHERITE_NUGGET)) return false;

        if (!s.get(idx(0,2,3)).is(Items.DIAMOND))     return false;
        if (!s.get(idx(1,2,3)).is(Items.ENDER_PEARL)) return false;
        if (!s.get(idx(2,2,3)).is(Items.DIAMOND))     return false;

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack compass = input.items().get(idx(1,1,3));
        LodestoneTracker tracker = compass.get(DataComponents.LODESTONE_TRACKER);

        ItemStack result = new ItemStack(ModItems.LODESTONE_WARP);
        if (tracker != null && tracker.target().isPresent()) {
            result.set(DataComponents.LODESTONE_TRACKER, tracker);
        }
        return result;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.WARP_FROM_LODESTONE_COMPASS;
    }
}
