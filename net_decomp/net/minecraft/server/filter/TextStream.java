/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.server.filter;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.filter.FilteredMessage;

public interface TextStream {
    public static final TextStream UNFILTERED = new TextStream(){

        @Override
        public CompletableFuture<FilteredMessage> filterText(String text) {
            return CompletableFuture.completedFuture(FilteredMessage.permitted(text));
        }

        @Override
        public CompletableFuture<List<FilteredMessage>> filterTexts(List<String> texts) {
            return CompletableFuture.completedFuture((List)texts.stream().map(FilteredMessage::permitted).collect(ImmutableList.toImmutableList()));
        }
    };

    default public void onConnect() {
    }

    default public void onDisconnect() {
    }

    public CompletableFuture<FilteredMessage> filterText(String var1);

    public CompletableFuture<List<FilteredMessage>> filterTexts(List<String> var1);
}

