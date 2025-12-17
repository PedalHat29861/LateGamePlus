/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.message;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import org.jspecify.annotations.Nullable;

public class MessageSignatureStorage {
    public static final int MISSING = -1;
    private static final int MAX_ENTRIES = 128;
    private final @Nullable MessageSignatureData[] signatures;

    public MessageSignatureStorage(int maxEntries) {
        this.signatures = new MessageSignatureData[maxEntries];
    }

    public static MessageSignatureStorage create() {
        return new MessageSignatureStorage(128);
    }

    public int indexOf(MessageSignatureData signature) {
        for (int i = 0; i < this.signatures.length; ++i) {
            if (!signature.equals(this.signatures[i])) continue;
            return i;
        }
        return -1;
    }

    public @Nullable MessageSignatureData get(int index) {
        return this.signatures[index];
    }

    public void add(MessageBody body, @Nullable MessageSignatureData signature) {
        List<MessageSignatureData> list = body.lastSeenMessages().entries();
        ArrayDeque<MessageSignatureData> arrayDeque = new ArrayDeque<MessageSignatureData>(list.size() + 1);
        arrayDeque.addAll(list);
        if (signature != null) {
            arrayDeque.add(signature);
        }
        this.addFrom(arrayDeque);
    }

    @VisibleForTesting
    void addFrom(List<MessageSignatureData> signatures) {
        this.addFrom(new ArrayDeque<MessageSignatureData>(signatures));
    }

    private void addFrom(ArrayDeque<MessageSignatureData> deque) {
        ObjectOpenHashSet set = new ObjectOpenHashSet(deque);
        for (int i = 0; !deque.isEmpty() && i < this.signatures.length; ++i) {
            MessageSignatureData messageSignatureData = this.signatures[i];
            this.signatures[i] = deque.removeLast();
            if (messageSignatureData == null || set.contains(messageSignatureData)) continue;
            deque.addFirst(messageSignatureData);
        }
    }
}

