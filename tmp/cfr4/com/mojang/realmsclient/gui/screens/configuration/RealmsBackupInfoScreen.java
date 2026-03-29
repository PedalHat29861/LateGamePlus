/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.CommonComponents
 *  net.minecraft.network.chat.Component
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.screens.configuration.RealmsSlotOptionsScreen;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsBackupInfoScreen
extends RealmsScreen {
    private static final Component TITLE = Component.translatable((String)"mco.backup.info.title");
    private static final Component UNKNOWN = Component.translatable((String)"mco.backup.unknown");
    private final Screen lastScreen;
    private final Backup backup;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen lastScreen, Backup backup) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.backup = backup;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.backupInfoList = this.layout.addToContents(new BackupInfoList(this, this.minecraft));
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
        this.repositionElements();
        RealmsBackupInfoScreen realmsBackupInfoScreen = this;
        this.layout.visitWidgets(x$0 -> realmsBackupInfoScreen.addRenderableWidget(x$0));
    }

    @Override
    protected void repositionElements() {
        this.backupInfoList.updateSize(this.width, this.layout);
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private Component checkForSpecificMetadata(String key, String value) {
        String k = key.toLowerCase(Locale.ROOT);
        if (k.contains("game") && k.contains("mode")) {
            return this.gameModeMetadata(value);
        }
        if (k.contains("game") && k.contains("difficulty")) {
            return this.gameDifficultyMetadata(value);
        }
        if (key.equals("world_type")) {
            return this.parseWorldType(value);
        }
        return Component.literal((String)value);
    }

    private Component gameDifficultyMetadata(String value) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(value)).getDisplayName();
        }
        catch (Exception ignored) {
            return UNKNOWN;
        }
    }

    private Component gameModeMetadata(String value) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(value)).getShortDisplayName();
        }
        catch (Exception ignored) {
            return UNKNOWN;
        }
    }

    private Component parseWorldType(String value) {
        try {
            return RealmsServer.WorldType.valueOf(value.toUpperCase(Locale.ROOT)).getDisplayName();
        }
        catch (Exception ignored) {
            return RealmsServer.WorldType.UNKNOWN.getDisplayName();
        }
    }

    private class BackupInfoList
    extends ObjectSelectionList<BackupInfoListEntry> {
        public BackupInfoList(RealmsBackupInfoScreen realmsBackupInfoScreen, Minecraft minecraft) {
            Objects.requireNonNull(realmsBackupInfoScreen);
            super(minecraft, realmsBackupInfoScreen.width, realmsBackupInfoScreen.layout.getContentHeight(), realmsBackupInfoScreen.layout.getHeaderHeight(), 36);
            if (realmsBackupInfoScreen.backup.changeList != null) {
                realmsBackupInfoScreen.backup.changeList.forEach((key, value) -> this.addEntry(new BackupInfoListEntry(realmsBackupInfoScreen, (String)key, (String)value)));
            }
        }
    }

    private class BackupInfoListEntry
    extends ObjectSelectionList.Entry<BackupInfoListEntry> {
        private static final Component TEMPLATE_NAME = Component.translatable((String)"mco.backup.entry.templateName");
        private static final Component GAME_DIFFICULTY = Component.translatable((String)"mco.backup.entry.gameDifficulty");
        private static final Component NAME = Component.translatable((String)"mco.backup.entry.name");
        private static final Component GAME_SERVER_VERSION = Component.translatable((String)"mco.backup.entry.gameServerVersion");
        private static final Component UPLOADED = Component.translatable((String)"mco.backup.entry.uploaded");
        private static final Component ENABLED_PACK = Component.translatable((String)"mco.backup.entry.enabledPack");
        private static final Component DESCRIPTION = Component.translatable((String)"mco.backup.entry.description");
        private static final Component GAME_MODE = Component.translatable((String)"mco.backup.entry.gameMode");
        private static final Component SEED = Component.translatable((String)"mco.backup.entry.seed");
        private static final Component WORLD_TYPE = Component.translatable((String)"mco.backup.entry.worldType");
        private static final Component UNDEFINED = Component.translatable((String)"mco.backup.entry.undefined");
        private final String key;
        private final String value;
        private final Component keyComponent;
        private final Component valueComponent;
        final /* synthetic */ RealmsBackupInfoScreen this$0;

        public BackupInfoListEntry(RealmsBackupInfoScreen realmsBackupInfoScreen, String key, String value) {
            RealmsBackupInfoScreen realmsBackupInfoScreen2 = realmsBackupInfoScreen;
            Objects.requireNonNull(realmsBackupInfoScreen2);
            this.this$0 = realmsBackupInfoScreen2;
            this.key = key;
            this.value = value;
            this.keyComponent = this.translateKey(key);
            this.valueComponent = realmsBackupInfoScreen.checkForSpecificMetadata(key, value);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            graphics.text(this.this$0.font, this.keyComponent, this.getContentX(), this.getContentY(), -6250336);
            graphics.text(this.this$0.font, this.valueComponent, this.getContentX(), this.getContentY() + 12, -1);
        }

        private Component translateKey(String key) {
            return switch (key) {
                case "template_name" -> TEMPLATE_NAME;
                case "game_difficulty" -> GAME_DIFFICULTY;
                case "name" -> NAME;
                case "game_server_version" -> GAME_SERVER_VERSION;
                case "uploaded" -> UPLOADED;
                case "enabled_packs" -> ENABLED_PACK;
                case "description" -> DESCRIPTION;
                case "game_mode" -> GAME_MODE;
                case "seed" -> SEED;
                case "world_type" -> WORLD_TYPE;
                default -> UNDEFINED;
            };
        }

        @Override
        public Component getNarration() {
            return Component.translatable((String)"narrator.select", (Object[])new Object[]{this.key + " " + this.value});
        }
    }
}

