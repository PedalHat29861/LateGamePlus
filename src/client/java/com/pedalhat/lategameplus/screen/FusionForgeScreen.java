package com.pedalhat.lategameplus.screen;

import com.pedalhat.lategameplus.LateGamePlus;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class FusionForgeScreen extends HandledScreen<FusionForgeScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(LateGamePlus.MOD_ID, "textures/gui/container/fusion_forge.png");
    private static final Identifier BURN_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/burn_progress.png");
    private static final Identifier LIT_PROGRESS_TEXTURE =
        Identifier.of(LateGamePlus.MOD_ID, "textures/gui/sprites/lit_progress.png");
    private static final Identifier RECIPE_BUTTON_TEXTURE =
        Identifier.ofVanilla("textures/gui/sprites/recipe_book/button.png");
    private static final Identifier RECIPE_BUTTON_HIGHLIGHTED_TEXTURE =
        Identifier.ofVanilla("textures/gui/sprites/recipe_book/button_highlighted.png");
    private static final boolean JEI_LOADED = FabricLoader.getInstance().isModLoaded("jei");
    private static final int RECIPE_BUTTON_X = 17;
    private static final int RECIPE_BUTTON_Y = 21;
    private static final int RECIPE_BUTTON_WIDTH = 20;
    private static final int RECIPE_BUTTON_HEIGHT = 18;
    private static final int ARROW_U = 0;
    private static final int ARROW_V = 0;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int ARROW_X = 105;
    private static final int ARROW_Y = 42;
    private static final int FLAME_U = 0;
    private static final int FLAME_V = 0;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;
    private static final int FLAME_TEXTURE_HEIGHT = 56;
    private static final int FLAME_FRAME_COUNT = 4;
    private static final int FLAME_FRAME_TICKS = 20;
    private static final int FLAME_X = 68;
    private static final int FLAME_Y = 40;

    public FusionForgeScreen(FusionForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.titleY = 8;
        this.titleX = 12;
        this.playerInventoryTitleY = 85;
        this.backgroundWidth = 176;
        this.backgroundHeight = 179;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth,
            backgroundHeight, backgroundWidth, backgroundHeight);

        if (JEI_LOADED) {
            int buttonX = x + RECIPE_BUTTON_X;
            int buttonY = y + RECIPE_BUTTON_Y;
            boolean hovered = mouseX >= buttonX
                && mouseX < buttonX + RECIPE_BUTTON_WIDTH
                && mouseY >= buttonY
                && mouseY < buttonY + RECIPE_BUTTON_HEIGHT;
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                hovered ? RECIPE_BUTTON_HIGHLIGHTED_TEXTURE : RECIPE_BUTTON_TEXTURE,
                buttonX,
                buttonY,
                0,
                0,
                RECIPE_BUTTON_WIDTH,
                RECIPE_BUTTON_HEIGHT,
                RECIPE_BUTTON_WIDTH,
                RECIPE_BUTTON_HEIGHT
            );
        }

        int progress = handler.getCookProgress();
        if (progress > 0) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_TEXTURE, x + ARROW_X, y + ARROW_Y, ARROW_U,
                ARROW_V, progress, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        }
        int fuel = handler.getFuelProgress();
        if (fuel > 0) {
            int flameOffset = FLAME_HEIGHT - fuel;
            long worldTime = client != null && client.world != null ? client.world.getTime() : 0L;
            int frame = (int) ((worldTime / FLAME_FRAME_TICKS) % FLAME_FRAME_COUNT);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_TEXTURE, x + FLAME_X,
                y + FLAME_Y + flameOffset, FLAME_U, FLAME_V + frame * FLAME_HEIGHT + flameOffset, FLAME_WIDTH, fuel, FLAME_WIDTH,
                FLAME_TEXTURE_HEIGHT);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        drawFuelTooltip(context, mouseX, mouseY);
    }

    private void drawFuelTooltip(DrawContext context, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        int left = x + FLAME_X;
        int top = y + FLAME_Y;
        if (mouseX < left || mouseX >= left + FLAME_WIDTH || mouseY < top || mouseY >= top + FLAME_HEIGHT) {
            return;
        }

        int capacity = handler.getFuelCapacity();
        if (capacity <= 0) {
            return;
        }
        int current = handler.getFuelStored();
        String formatted = String.format(Locale.US, "Capacidad de combustible: %,d / %,d", capacity, current);
        context.drawTooltip(textRenderer, Text.literal(formatted), mouseX, mouseY);
    }
}
