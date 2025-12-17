/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.SignedArgumentType;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public class MessageArgumentType
implements SignedArgumentType<MessageFormat> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    static final Dynamic2CommandExceptionType MESSAGE_TOO_LONG_EXCEPTION = new Dynamic2CommandExceptionType((length, maxLength) -> Text.stringifiedTranslatable("argument.message.too_long", length, maxLength));

    public static MessageArgumentType message() {
        return new MessageArgumentType();
    }

    public static Text getMessage(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        MessageFormat messageFormat = (MessageFormat)context.getArgument(name, MessageFormat.class);
        return messageFormat.format((ServerCommandSource)context.getSource());
    }

    public static void getSignedMessage(CommandContext<ServerCommandSource> context, String name, Consumer<SignedMessage> callback) throws CommandSyntaxException {
        MessageFormat messageFormat = (MessageFormat)context.getArgument(name, MessageFormat.class);
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        Text text = messageFormat.format(serverCommandSource);
        SignedCommandArguments signedCommandArguments = serverCommandSource.getSignedArguments();
        SignedMessage signedMessage = signedCommandArguments.getMessage(name);
        if (signedMessage != null) {
            MessageArgumentType.chain(callback, serverCommandSource, signedMessage.withUnsignedContent(text));
        } else {
            MessageArgumentType.chainUnsigned(callback, serverCommandSource, SignedMessage.ofUnsigned(messageFormat.contents).withUnsignedContent(text));
        }
    }

    private static void chain(Consumer<SignedMessage> callback, ServerCommandSource source, SignedMessage message) {
        MinecraftServer minecraftServer = source.getServer();
        CompletableFuture<FilteredMessage> completableFuture = MessageArgumentType.filterText(source, message);
        Text text = minecraftServer.getMessageDecorator().decorate(source.getPlayer(), message.getContent());
        source.getMessageChainTaskQueue().append(completableFuture, filtered -> {
            SignedMessage signedMessage2 = message.withUnsignedContent(text).withFilterMask(filtered.mask());
            callback.accept(signedMessage2);
        });
    }

    private static void chainUnsigned(Consumer<SignedMessage> callback, ServerCommandSource source, SignedMessage message) {
        MessageDecorator messageDecorator = source.getServer().getMessageDecorator();
        Text text = messageDecorator.decorate(source.getPlayer(), message.getContent());
        callback.accept(message.withUnsignedContent(text));
    }

    private static CompletableFuture<FilteredMessage> filterText(ServerCommandSource source, SignedMessage message) {
        ServerPlayerEntity serverPlayerEntity = source.getPlayer();
        if (serverPlayerEntity != null && message.canVerifyFrom(serverPlayerEntity.getUuid())) {
            return serverPlayerEntity.getTextStream().filterText(message.getSignedContent());
        }
        return CompletableFuture.completedFuture(FilteredMessage.permitted(message.getSignedContent()));
    }

    public MessageFormat parse(StringReader stringReader) throws CommandSyntaxException {
        return MessageFormat.parse(stringReader, true);
    }

    public <S> MessageFormat parse(StringReader stringReader, @Nullable S object) throws CommandSyntaxException {
        return MessageFormat.parse(stringReader, EntitySelectorReader.shouldAllowAtSelectors(object));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader reader, @Nullable Object source) throws CommandSyntaxException {
        return this.parse(reader, source);
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static final class MessageFormat
    extends Record {
        final String contents;
        private final MessageSelector[] selectors;

        public MessageFormat(String contents, MessageSelector[] selectors) {
            this.contents = contents;
            this.selectors = selectors;
        }

        Text format(ServerCommandSource source) throws CommandSyntaxException {
            return this.format(source, source.getPermissions().hasPermission(DefaultPermissions.ENTITY_SELECTORS));
        }

        public Text format(ServerCommandSource source, boolean canUseSelectors) throws CommandSyntaxException {
            if (this.selectors.length == 0 || !canUseSelectors) {
                return Text.literal(this.contents);
            }
            MutableText mutableText = Text.literal(this.contents.substring(0, this.selectors[0].start()));
            int i = this.selectors[0].start();
            for (MessageSelector messageSelector : this.selectors) {
                Text text = messageSelector.format(source);
                if (i < messageSelector.start()) {
                    mutableText.append(this.contents.substring(i, messageSelector.start()));
                }
                mutableText.append(text);
                i = messageSelector.end();
            }
            if (i < this.contents.length()) {
                mutableText.append(this.contents.substring(i));
            }
            return mutableText;
        }

        public static MessageFormat parse(StringReader reader, boolean allowAtSelectors) throws CommandSyntaxException {
            if (reader.getRemainingLength() > 256) {
                throw MESSAGE_TOO_LONG_EXCEPTION.create((Object)reader.getRemainingLength(), (Object)256);
            }
            String string = reader.getRemaining();
            if (!allowAtSelectors) {
                reader.setCursor(reader.getTotalLength());
                return new MessageFormat(string, new MessageSelector[0]);
            }
            ArrayList list = Lists.newArrayList();
            int i = reader.getCursor();
            while (reader.canRead()) {
                if (reader.peek() == '@') {
                    EntitySelector entitySelector;
                    int j = reader.getCursor();
                    try {
                        EntitySelectorReader entitySelectorReader = new EntitySelectorReader(reader, true);
                        entitySelector = entitySelectorReader.read();
                    }
                    catch (CommandSyntaxException commandSyntaxException) {
                        if (commandSyntaxException.getType() == EntitySelectorReader.MISSING_EXCEPTION || commandSyntaxException.getType() == EntitySelectorReader.UNKNOWN_SELECTOR_EXCEPTION) {
                            reader.setCursor(j + 1);
                            continue;
                        }
                        throw commandSyntaxException;
                    }
                    list.add(new MessageSelector(j - i, reader.getCursor() - i, entitySelector));
                    continue;
                }
                reader.skip();
            }
            return new MessageFormat(string, list.toArray(new MessageSelector[0]));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MessageFormat.class, "text;parts", "contents", "selectors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MessageFormat.class, "text;parts", "contents", "selectors"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MessageFormat.class, "text;parts", "contents", "selectors"}, this, object);
        }

        public String contents() {
            return this.contents;
        }

        public MessageSelector[] selectors() {
            return this.selectors;
        }
    }

    public record MessageSelector(int start, int end, EntitySelector selector) {
        public Text format(ServerCommandSource source) throws CommandSyntaxException {
            return EntitySelector.getNames(this.selector.getEntities(source));
        }
    }
}

