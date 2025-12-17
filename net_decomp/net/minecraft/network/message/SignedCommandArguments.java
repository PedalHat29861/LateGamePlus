/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.message;

import java.util.Map;
import net.minecraft.network.message.SignedMessage;
import org.jspecify.annotations.Nullable;

public interface SignedCommandArguments {
    public static final SignedCommandArguments EMPTY = new SignedCommandArguments(){

        @Override
        public @Nullable SignedMessage getMessage(String argumentName) {
            return null;
        }
    };

    public @Nullable SignedMessage getMessage(String var1);

    public record Impl(Map<String, SignedMessage> arguments) implements SignedCommandArguments
    {
        @Override
        public @Nullable SignedMessage getMessage(String argumentName) {
            return this.arguments.get(argumentName);
        }
    }
}

