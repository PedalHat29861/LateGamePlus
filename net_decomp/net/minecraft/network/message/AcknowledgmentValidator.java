/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import org.jspecify.annotations.Nullable;

public class AcknowledgmentValidator {
    private final int size;
    private final ObjectList<AcknowledgedMessage> messages = new ObjectArrayList();
    private @Nullable MessageSignatureData lastSignature;

    public AcknowledgmentValidator(int size) {
        this.size = size;
        for (int i = 0; i < size; ++i) {
            this.messages.add(null);
        }
    }

    public void addPending(MessageSignatureData signature) {
        if (!signature.equals(this.lastSignature)) {
            this.messages.add((Object)new AcknowledgedMessage(signature, true));
            this.lastSignature = signature;
        }
    }

    public int getMessageCount() {
        return this.messages.size();
    }

    public void removeUntil(int index) throws ValidationException {
        int i = this.messages.size() - this.size;
        if (index < 0 || index > i) {
            throw new ValidationException("Advanced last seen window by " + index + " messages, but expected at most " + i);
        }
        this.messages.removeElements(0, index);
    }

    public LastSeenMessageList validate(LastSeenMessageList.Acknowledgment acknowledgment) throws ValidationException {
        this.removeUntil(acknowledgment.offset());
        ObjectArrayList objectList = new ObjectArrayList(acknowledgment.acknowledged().cardinality());
        if (acknowledgment.acknowledged().length() > this.size) {
            throw new ValidationException("Last seen update contained " + acknowledgment.acknowledged().length() + " messages, but maximum window size is " + this.size);
        }
        for (int i = 0; i < this.size; ++i) {
            boolean bl = acknowledgment.acknowledged().get(i);
            AcknowledgedMessage acknowledgedMessage = (AcknowledgedMessage)this.messages.get(i);
            if (bl) {
                if (acknowledgedMessage == null) {
                    throw new ValidationException("Last seen update acknowledged unknown or previously ignored message at index " + i);
                }
                this.messages.set(i, (Object)acknowledgedMessage.unmarkAsPending());
                objectList.add((Object)acknowledgedMessage.signature());
                continue;
            }
            if (acknowledgedMessage != null && !acknowledgedMessage.pending()) {
                throw new ValidationException("Last seen update ignored previously acknowledged message at index " + i + " and signature " + String.valueOf(acknowledgedMessage.signature()));
            }
            this.messages.set(i, null);
        }
        LastSeenMessageList lastSeenMessageList = new LastSeenMessageList((List<MessageSignatureData>)objectList);
        if (!acknowledgment.checksumEquals(lastSeenMessageList)) {
            throw new ValidationException("Checksum mismatch on last seen update: the client and server must have desynced");
        }
        return lastSeenMessageList;
    }

    public static class ValidationException
    extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}

