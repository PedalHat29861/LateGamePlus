/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.message.AcknowledgedMessage;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import org.jspecify.annotations.Nullable;

public class LastSeenMessagesCollector {
    private final @Nullable AcknowledgedMessage[] acknowledgedMessages;
    private int nextIndex;
    private int messageCount;
    private @Nullable MessageSignatureData lastAdded;

    public LastSeenMessagesCollector(int size) {
        this.acknowledgedMessages = new AcknowledgedMessage[size];
    }

    public boolean add(MessageSignatureData signature, boolean displayed) {
        if (Objects.equals(signature, this.lastAdded)) {
            return false;
        }
        this.lastAdded = signature;
        this.add(displayed ? new AcknowledgedMessage(signature, true) : null);
        return true;
    }

    private void add(@Nullable AcknowledgedMessage message) {
        int i = this.nextIndex;
        this.nextIndex = (i + 1) % this.acknowledgedMessages.length;
        ++this.messageCount;
        this.acknowledgedMessages[i] = message;
    }

    public void remove(MessageSignatureData signature) {
        for (int i = 0; i < this.acknowledgedMessages.length; ++i) {
            AcknowledgedMessage acknowledgedMessage = this.acknowledgedMessages[i];
            if (acknowledgedMessage == null || !acknowledgedMessage.pending() || !signature.equals(acknowledgedMessage.signature())) continue;
            this.acknowledgedMessages[i] = null;
            break;
        }
    }

    public int resetMessageCount() {
        int i = this.messageCount;
        this.messageCount = 0;
        return i;
    }

    public LastSeenMessages collect() {
        int i = this.resetMessageCount();
        BitSet bitSet = new BitSet(this.acknowledgedMessages.length);
        ObjectArrayList objectList = new ObjectArrayList(this.acknowledgedMessages.length);
        for (int j = 0; j < this.acknowledgedMessages.length; ++j) {
            int k = (this.nextIndex + j) % this.acknowledgedMessages.length;
            AcknowledgedMessage acknowledgedMessage = this.acknowledgedMessages[k];
            if (acknowledgedMessage == null) continue;
            bitSet.set(j, true);
            objectList.add((Object)acknowledgedMessage.signature());
            this.acknowledgedMessages[k] = acknowledgedMessage.unmarkAsPending();
        }
        LastSeenMessageList lastSeenMessageList = new LastSeenMessageList((List<MessageSignatureData>)objectList);
        LastSeenMessageList.Acknowledgment acknowledgment = new LastSeenMessageList.Acknowledgment(i, bitSet, lastSeenMessageList.calculateChecksum());
        return new LastSeenMessages(lastSeenMessageList, acknowledgment);
    }

    public int getMessageCount() {
        return this.messageCount;
    }

    public record LastSeenMessages(LastSeenMessageList lastSeen, LastSeenMessageList.Acknowledgment update) {
    }
}

