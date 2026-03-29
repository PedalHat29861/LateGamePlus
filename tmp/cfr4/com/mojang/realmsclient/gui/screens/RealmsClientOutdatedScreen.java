/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SharedConstants
 *  net.minecraft.network.chat.CommonComponents
 *  net.minecraft.network.chat.Component
 */
package com.mojang.realmsclient.gui.screens;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsClientOutdatedScreen
extends RealmsScreen {
    private static final Component INCOMPATIBLE_TITLE = Component.translatable((String)"mco.client.incompatible.title").withColor(-65536);
    private static final Component INCOMPATIBLE_CLIENT_VERSION = Component.literal((String)SharedConstants.getCurrentVersion().name()).withColor(-65536);
    private static final Component UNSUPPORTED_SNAPSHOT_VERSION = Component.translatable((String)"mco.client.unsupported.snapshot.version", (Object[])new Object[]{INCOMPATIBLE_CLIENT_VERSION});
    private static final Component OUTDATED_STABLE_VERSION = Component.translatable((String)"mco.client.outdated.stable.version", (Object[])new Object[]{INCOMPATIBLE_CLIENT_VERSION});
    private final Screen lastScreen;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public RealmsClientOutdatedScreen(Screen lastScreen) {
        super(INCOMPATIBLE_TITLE);
        this.lastScreen = lastScreen;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(INCOMPATIBLE_TITLE, this.font);
        this.layout.addToContents(new MultiLineTextWidget(this.getErrorMessage(), this.font).setCentered(true));
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(200).build());
        RealmsClientOutdatedScreen realmsClientOutdatedScreen = this;
        this.layout.visitWidgets(x$0 -> realmsClientOutdatedScreen.addRenderableWidget(x$0));
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private Component getErrorMessage() {
        if (SharedConstants.getCurrentVersion().stable()) {
            return OUTDATED_STABLE_VERSION;
        }
        return UNSUPPORTED_SNAPSHOT_VERSION;
    }
}

