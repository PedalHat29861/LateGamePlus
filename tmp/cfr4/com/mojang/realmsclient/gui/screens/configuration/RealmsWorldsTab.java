/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.network.chat.Component
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsPopups;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsSelectWorldTemplateScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsBackupScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigurationTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsSlotOptionsScreen;
import com.mojang.realmsclient.util.task.SwitchMinigameTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

class RealmsWorldsTab
extends GridLayoutTab
implements RealmsConfigurationTab {
    static final Component TITLE = Component.translatable((String)"mco.configure.worlds.title");
    private final RealmsConfigureWorldScreen configurationScreen;
    private final Minecraft minecraft;
    private RealmsServer serverData;
    private final Button optionsButton;
    private final Button backupButton;
    private final Button resetWorldButton;
    private final List<RealmsWorldSlotButton> slotButtonList = Lists.newArrayList();

    RealmsWorldsTab(RealmsConfigureWorldScreen configurationScreen, Minecraft minecraft, RealmsServer serverData) {
        super(TITLE);
        this.configurationScreen = configurationScreen;
        this.minecraft = minecraft;
        this.serverData = serverData;
        GridLayout.RowHelper helper = this.layout.spacing(20).createRowHelper(1);
        GridLayout.RowHelper slots = new GridLayout().spacing(16).createRowHelper(4);
        this.slotButtonList.clear();
        for (int i = 1; i < 5; ++i) {
            this.slotButtonList.add(slots.addChild(this.createSlotButton(i), LayoutSettings.defaults().alignVerticallyBottom()));
        }
        helper.addChild(slots.getGrid());
        GridLayout.RowHelper buttons = new GridLayout().spacing(8).createRowHelper(1);
        this.optionsButton = buttons.addChild(Button.builder((Component)Component.translatable((String)"mco.configure.world.buttons.options"), button -> minecraft.setScreen(new RealmsSlotOptionsScreen(configurationScreen, serverData.slots.get(serverData.activeSlot).copy(), serverData.worldType, serverData.activeSlot))).bounds(0, 0, 150, 20).build());
        this.backupButton = buttons.addChild(Button.builder((Component)Component.translatable((String)"mco.configure.world.backup"), button -> minecraft.setScreen(new RealmsBackupScreen(configurationScreen, serverData.copy(), serverData.activeSlot))).bounds(0, 0, 150, 20).build());
        this.resetWorldButton = buttons.addChild(Button.builder((Component)Component.empty(), button -> this.resetButtonPressed()).bounds(0, 0, 150, 20).build());
        helper.addChild(buttons.getGrid(), LayoutSettings.defaults().alignHorizontallyCenter());
        this.backupButton.active = true;
        this.updateData(serverData);
    }

    private void resetButtonPressed() {
        if (this.isMinigame()) {
            this.minecraft.setScreen(new RealmsSelectWorldTemplateScreen((Component)Component.translatable((String)"mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME, null));
        } else {
            this.minecraft.setScreen(RealmsResetWorldScreen.forResetSlot(this.configurationScreen, this.serverData.copy(), () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.configurationScreen.getNewScreen()))));
        }
    }

    private void templateSelectionCallback(@Nullable WorldTemplate worldTemplate) {
        if (worldTemplate != null && WorldTemplate.WorldTemplateType.MINIGAME == worldTemplate.type()) {
            this.configurationScreen.stateChanged();
            RealmsConfigureWorldScreen newScreen = this.configurationScreen.getNewScreen();
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(newScreen, new SwitchMinigameTask(this.serverData.id, worldTemplate, newScreen)));
        } else {
            this.minecraft.setScreen(this.configurationScreen);
        }
    }

    private boolean isMinigame() {
        return this.serverData.isMinigameActive();
    }

    @Override
    public void onSelected(RealmsServer serverData) {
        this.updateData(serverData);
    }

    @Override
    public void updateData(RealmsServer serverData) {
        this.serverData = serverData;
        this.optionsButton.active = !serverData.expired && !this.isMinigame();
        boolean bl = this.resetWorldButton.active = !serverData.expired;
        if (this.isMinigame()) {
            this.resetWorldButton.setMessage((Component)Component.translatable((String)"mco.configure.world.buttons.switchminigame"));
        } else {
            boolean emptySlot;
            boolean bl2 = emptySlot = serverData.slots.containsKey(serverData.activeSlot) && serverData.slots.get((Object)Integer.valueOf((int)serverData.activeSlot)).options.empty;
            if (emptySlot) {
                this.resetWorldButton.setMessage((Component)Component.translatable((String)"mco.configure.world.buttons.newworld"));
            } else {
                this.resetWorldButton.setMessage((Component)Component.translatable((String)"mco.configure.world.buttons.resetworld"));
            }
        }
        this.backupButton.active = !this.isMinigame();
        for (RealmsWorldSlotButton realmsWorldSlotButton : this.slotButtonList) {
            RealmsWorldSlotButton.State state = realmsWorldSlotButton.setServerData(serverData);
            if (state.activeSlot) {
                realmsWorldSlotButton.setSize(80, 80);
                continue;
            }
            realmsWorldSlotButton.setSize(50, 50);
        }
        this.slotButtonList.get(serverData.activeSlot - 1).updateSlotState(serverData);
    }

    private RealmsWorldSlotButton createSlotButton(int i) {
        return new RealmsWorldSlotButton(0, 0, 80, 80, i, this.serverData, button -> {
            RealmsWorldSlotButton.State state = ((RealmsWorldSlotButton)button).getState();
            switch (state.action) {
                case NOTHING: {
                    break;
                }
                case SWITCH_SLOT: {
                    if (state.minigame) {
                        this.switchToMinigame();
                        break;
                    }
                    if (state.empty) {
                        this.switchToEmptySlot(i, this.serverData);
                        break;
                    }
                    this.switchToFullSlot(i, this.serverData);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown action " + String.valueOf((Object)state.action));
                }
            }
        });
    }

    private void switchToMinigame() {
        RealmsSelectWorldTemplateScreen screen = new RealmsSelectWorldTemplateScreen((Component)Component.translatable((String)"mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME, null, List.of(Component.translatable((String)"mco.minigame.world.info.line1").withColor(-4539718), Component.translatable((String)"mco.minigame.world.info.line2").withColor(-4539718)));
        this.minecraft.setScreen(screen);
    }

    private void switchToFullSlot(int selectedSlot, RealmsServer serverData) {
        this.minecraft.setScreen(RealmsPopups.infoPopupScreen(this.configurationScreen, (Component)Component.translatable((String)"mco.configure.world.slot.switch.question.line1"), popup -> {
            RealmsConfigureWorldScreen newScreen = this.configurationScreen.getNewScreen();
            this.configurationScreen.stateChanged();
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(newScreen, new SwitchSlotTask(serverData.id, selectedSlot, () -> this.minecraft.execute(() -> this.minecraft.setScreen(newScreen)))));
        }));
    }

    private void switchToEmptySlot(int selectedSlot, RealmsServer serverData) {
        this.minecraft.setScreen(RealmsPopups.infoPopupScreen(this.configurationScreen, (Component)Component.translatable((String)"mco.configure.world.slot.switch.question.line1"), popups -> {
            this.configurationScreen.stateChanged();
            RealmsResetWorldScreen resetWorldScreen = RealmsResetWorldScreen.forEmptySlot(this.configurationScreen, selectedSlot, serverData, () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.configurationScreen.getNewScreen())));
            this.minecraft.setScreen(resetWorldScreen);
        }));
    }
}

