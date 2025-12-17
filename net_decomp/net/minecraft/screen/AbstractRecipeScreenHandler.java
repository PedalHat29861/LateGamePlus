/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;

public abstract class AbstractRecipeScreenHandler
extends ScreenHandler {
    public AbstractRecipeScreenHandler(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    public abstract PostFillAction fillInputSlots(boolean var1, boolean var2, RecipeEntry<?> var3, ServerWorld var4, PlayerInventory var5);

    public abstract void populateRecipeFinder(RecipeFinder var1);

    public abstract RecipeBookType getCategory();

    public static final class PostFillAction
    extends Enum<PostFillAction> {
        public static final /* enum */ PostFillAction NOTHING = new PostFillAction();
        public static final /* enum */ PostFillAction PLACE_GHOST_RECIPE = new PostFillAction();
        private static final /* synthetic */ PostFillAction[] field_52574;

        public static PostFillAction[] values() {
            return (PostFillAction[])field_52574.clone();
        }

        public static PostFillAction valueOf(String string) {
            return Enum.valueOf(PostFillAction.class, string);
        }

        private static /* synthetic */ PostFillAction[] method_61636() {
            return new PostFillAction[]{NOTHING, PLACE_GHOST_RECIPE};
        }

        static {
            field_52574 = PostFillAction.method_61636();
        }
    }
}

