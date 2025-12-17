/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.SignedMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
public interface MessageVerifier {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final MessageVerifier NO_SIGNATURE = SignedMessage::stripSignature;
    public static final MessageVerifier UNVERIFIED = message -> {
        LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", (Object)message.getSender());
        return null;
    };

    public @Nullable SignedMessage ensureVerified(SignedMessage var1);

    public static class Impl
    implements MessageVerifier {
        private final SignatureVerifier signatureVerifier;
        private final BooleanSupplier expirationChecker;
        private @Nullable SignedMessage lastVerifiedMessage;
        private boolean lastMessageVerified = true;

        public Impl(SignatureVerifier signatureVerifier, BooleanSupplier expirationChecker) {
            this.signatureVerifier = signatureVerifier;
            this.expirationChecker = expirationChecker;
        }

        private boolean verifyPrecedingSignature(SignedMessage message) {
            if (message.equals(this.lastVerifiedMessage)) {
                return true;
            }
            if (this.lastVerifiedMessage != null && !message.link().linksTo(this.lastVerifiedMessage.link())) {
                LOGGER.error("Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}", new Object[]{message.getSender(), this.lastVerifiedMessage.link().index(), this.lastVerifiedMessage.link().sessionId(), message.link().index(), message.link().sessionId()});
                return false;
            }
            return true;
        }

        private boolean verify(SignedMessage message) {
            if (this.expirationChecker.getAsBoolean()) {
                LOGGER.error("Received message with expired profile public key from {} with session {}", (Object)message.getSender(), (Object)message.link().sessionId());
                return false;
            }
            if (!message.verify(this.signatureVerifier)) {
                LOGGER.error("Received message with invalid signature (is the session wrong, or signature cache out of sync?): {}", (Object)SignedMessage.toString(message));
                return false;
            }
            return this.verifyPrecedingSignature(message);
        }

        @Override
        public @Nullable SignedMessage ensureVerified(SignedMessage message) {
            boolean bl = this.lastMessageVerified = this.lastMessageVerified && this.verify(message);
            if (!this.lastMessageVerified) {
                return null;
            }
            this.lastVerifiedMessage = message;
            return message;
        }
    }
}

