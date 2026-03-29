/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.util.VisibleForDebug
 *  net.minecraft.util.profiling.Profiler
 *  net.minecraft.util.profiling.Zone
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.vertex.TlsfAllocator;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public class UberGpuBuffer<T>
implements AutoCloseable {
    private final int alignSize;
    private final UberGpuBufferStagingBuffer stagingBuffer;
    private int stagingBufferUsedSize;
    private final String name;
    private final List<Pair<TlsfAllocator, UberGpuBufferHeap>> nodes;
    private final Object2ObjectOpenHashMap<T, StagedAllocationEntry<T>> stagedAllocations = new Object2ObjectOpenHashMap(32);
    private final ObjectOpenHashSet<T> skippedStagedAllocations = new ObjectOpenHashSet(32);
    private final Map<T, TlsfAllocator.Allocation> allocationMap = new HashMap<T, TlsfAllocator.Allocation>(256);

    public UberGpuBuffer(String name, int usage, int heapSize, int alignSize, GpuDevice gpuDevice, int stagingBufferSize, GraphicsWorkarounds workarounds) {
        if (stagingBufferSize > heapSize) {
            throw new IllegalArgumentException("Staging buffer size cannot be bigger than heap size");
        }
        this.name = "UberBuffer " + name;
        this.stagingBuffer = UberGpuBufferStagingBuffer.create(this.name, gpuDevice, stagingBufferSize, workarounds);
        this.stagingBufferUsedSize = 0;
        this.nodes = new ArrayList<Pair<TlsfAllocator, UberGpuBufferHeap>>();
        this.alignSize = alignSize;
        String initialHeapName = this.name + " 0";
        UberGpuBufferHeap initialHeap = new UberGpuBufferHeap(heapSize, gpuDevice, usage, initialHeapName);
        TlsfAllocator initialTlsfAllocator = new TlsfAllocator(initialHeap);
        this.nodes.add((Pair<TlsfAllocator, UberGpuBufferHeap>)new Pair((Object)initialTlsfAllocator, (Object)initialHeap));
    }

    public boolean addAllocation(T allocationKey, @Nullable UploadCallback<T> callback, ByteBuffer buffer) {
        int startOffset = this.stagingBufferUsedSize;
        ByteBuffer stagingBuffer = this.stagingBuffer.getStagingBuffer();
        if (buffer.remaining() > stagingBuffer.capacity()) {
            throw new IllegalArgumentException("UberGpuBuffer cannot have any allocations bigger than its staging buffer, increase the staging buffer size!");
        }
        if (buffer.remaining() > stagingBuffer.capacity() - startOffset) {
            return false;
        }
        MemoryUtil.memCopy((ByteBuffer)buffer, (ByteBuffer)stagingBuffer.position(startOffset));
        this.stagingBufferUsedSize += buffer.remaining();
        StagedAllocationEntry<T> entry = new StagedAllocationEntry<T>(callback, startOffset, buffer.remaining());
        this.stagedAllocations.put(allocationKey, entry);
        return true;
    }

    public boolean uploadStagedAllocations(GpuDevice gpuDevice, CommandEncoder encoder) {
        for (Object key : this.stagedAllocations.keySet()) {
            this.freeAllocation(key);
        }
        boolean newHeapCreatedOrDestroyed = false;
        try (Zone ignored = Profiler.get().zone("Upload staged allocations");){
            for (Map.Entry entry : this.stagedAllocations.entrySet()) {
                Pair<TlsfAllocator, UberGpuBufferHeap> node;
                long allocationSize = ((StagedAllocationEntry)entry.getValue()).size;
                if (this.skippedStagedAllocations.contains(entry.getKey())) continue;
                TlsfAllocator.Allocation allocation = null;
                Iterator<Pair<TlsfAllocator, UberGpuBufferHeap>> iterator = this.nodes.iterator();
                while (iterator.hasNext() && (allocation = ((TlsfAllocator)(node = iterator.next()).getFirst()).allocate(allocationSize, this.alignSize)) == null) {
                }
                if (allocation == null) {
                    try (Zone ignored2 = Profiler.get().zone("Create new heap");){
                        UberGpuBufferHeap firstHeap = (UberGpuBufferHeap)this.nodes.getFirst().getSecond();
                        long heapSize = firstHeap.gpuBuffer.size();
                        assert (allocationSize <= heapSize);
                        String heapName = String.format(Locale.ROOT, "%s %d", this.name, this.nodes.size());
                        UberGpuBufferHeap newHeap = new UberGpuBufferHeap(heapSize, gpuDevice, firstHeap.gpuBuffer.usage(), heapName);
                        TlsfAllocator newTlsfAllocator = new TlsfAllocator(newHeap);
                        this.nodes.add((Pair<TlsfAllocator, UberGpuBufferHeap>)new Pair((Object)newTlsfAllocator, (Object)newHeap));
                        allocation = newTlsfAllocator.allocate(allocationSize, this.alignSize);
                        newHeapCreatedOrDestroyed = true;
                    }
                }
                if (allocation == null) continue;
                TlsfAllocator.Heap allocationHeap = allocation.getHeap();
                GpuBuffer allocationDestBuffer = ((UberGpuBufferHeap)allocationHeap).gpuBuffer;
                this.stagingBuffer.copyToHeap(encoder, allocationDestBuffer, allocation.getOffsetFromHeap(), ((StagedAllocationEntry)entry.getValue()).offset, allocationSize);
                this.allocationMap.put(entry.getKey(), allocation);
                if (((StagedAllocationEntry)entry.getValue()).callback == null) continue;
                ((StagedAllocationEntry)entry.getValue()).callback.bufferHasBeenUploaded(entry.getKey());
            }
            this.stagingBuffer.clearFrame(encoder);
            this.stagingBufferUsedSize = 0;
            this.stagedAllocations.clear();
            this.skippedStagedAllocations.clear();
        }
        Iterator<Pair<TlsfAllocator, UberGpuBufferHeap>> iterator = this.nodes.iterator();
        while (iterator.hasNext() && this.nodes.size() > 1) {
            Pair<TlsfAllocator, UberGpuBufferHeap> node = iterator.next();
            if (!((TlsfAllocator)node.getFirst()).isCompletelyFree()) continue;
            ((UberGpuBufferHeap)node.getSecond()).gpuBuffer.close();
            iterator.remove();
            newHeapCreatedOrDestroyed = true;
            break;
        }
        return newHeapCreatedOrDestroyed;
    }

    public @Nullable TlsfAllocator.Allocation getAllocation(T allocationKey) {
        return this.allocationMap.get(allocationKey);
    }

    public void removeAllocation(T allocationKey) {
        this.skippedStagedAllocations.add(allocationKey);
        this.freeAllocation(allocationKey);
    }

    private void freeAllocation(T allocationKey) {
        TlsfAllocator.Allocation allocation = this.allocationMap.remove(allocationKey);
        if (allocation != null) {
            for (Pair<TlsfAllocator, UberGpuBufferHeap> node : this.nodes) {
                if (node.getSecond() != allocation.getHeap()) continue;
                ((TlsfAllocator)node.getFirst()).free(allocation);
                break;
            }
        }
    }

    public GpuBuffer getGpuBuffer(TlsfAllocator.Allocation allocation) {
        return ((UberGpuBufferHeap)allocation.getHeap()).gpuBuffer;
    }

    @VisibleForDebug
    public void printStatistics() {
        for (int i = 0; i < this.nodes.size(); ++i) {
            Pair<TlsfAllocator, UberGpuBufferHeap> node = this.nodes.get(i);
            String heapName = String.format(Locale.ROOT, "%s %d", this.name, i);
            ((TlsfAllocator)node.getFirst()).printAllocatorStatistics(heapName);
        }
    }

    @Override
    public void close() {
        this.stagingBuffer.destroyBuffer();
        this.stagingBufferUsedSize = 0;
        this.stagedAllocations.clear();
        this.allocationMap.clear();
        for (Pair<TlsfAllocator, UberGpuBufferHeap> node : this.nodes) {
            ((UberGpuBufferHeap)node.getSecond()).gpuBuffer.close();
        }
        this.nodes.clear();
    }

    private static abstract class UberGpuBufferStagingBuffer {
        private UberGpuBufferStagingBuffer() {
        }

        public static UberGpuBufferStagingBuffer create(String name, GpuDevice gpuDevice, int stagingBufferSize, GraphicsWorkarounds workarounds) {
            if (!workarounds.isGlOnDx12()) {
                return new CPUStagingBuffer(name, gpuDevice, stagingBufferSize);
            }
            return new MappedStagingBuffer(name, gpuDevice, stagingBufferSize);
        }

        abstract ByteBuffer getStagingBuffer();

        abstract void copyToHeap(CommandEncoder var1, GpuBuffer var2, long var3, long var5, long var7);

        abstract void clearFrame(CommandEncoder var1);

        abstract void destroyBuffer();

        private static class CPUStagingBuffer
        extends UberGpuBufferStagingBuffer {
            private final ByteBuffer stagingBuffer;

            private CPUStagingBuffer(String name, GpuDevice gpuDevice, int stagingBufferSize) {
                this.stagingBuffer = MemoryUtil.memAlloc((int)stagingBufferSize);
            }

            @Override
            ByteBuffer getStagingBuffer() {
                return this.stagingBuffer;
            }

            @Override
            void copyToHeap(CommandEncoder encoder, GpuBuffer heapBuffer, long heapOffset, long stagingBufferOffset, long copySize) {
                encoder.writeToBuffer(heapBuffer.slice(heapOffset, copySize), this.stagingBuffer.slice((int)stagingBufferOffset, (int)copySize));
            }

            @Override
            void clearFrame(CommandEncoder encoder) {
                this.stagingBuffer.clear();
            }

            @Override
            void destroyBuffer() {
                this.stagingBuffer.clear();
                MemoryUtil.memFree((ByteBuffer)this.stagingBuffer);
            }
        }

        private static class MappedStagingBuffer
        extends UberGpuBufferStagingBuffer {
            private final MappableRingBuffer mappableRingBuffer;
            private GpuBuffer.MappedView currentMappedView;
            private GpuBuffer currentGPUBuffer;
            private ByteBuffer currentBuffer;

            private MappedStagingBuffer(String name, GpuDevice gpuDevice, int stagingBufferSize) {
                String stagingBufferName = name + " staging buffer";
                this.mappableRingBuffer = new MappableRingBuffer(() -> stagingBufferName, 18, stagingBufferSize / 2);
                CommandEncoder encoder = gpuDevice.createCommandEncoder();
                this.currentGPUBuffer = this.mappableRingBuffer.currentBuffer();
                this.currentMappedView = encoder.mapBuffer(this.currentGPUBuffer, false, true);
                this.currentBuffer = this.currentMappedView.data();
            }

            @Override
            ByteBuffer getStagingBuffer() {
                return this.currentBuffer;
            }

            @Override
            void copyToHeap(CommandEncoder encoder, GpuBuffer heapBuffer, long heapOffset, long stagingBufferOffset, long copySize) {
                encoder.copyToBuffer(this.currentGPUBuffer.slice(stagingBufferOffset, copySize), heapBuffer.slice(heapOffset, copySize));
            }

            @Override
            void clearFrame(CommandEncoder encoder) {
                this.currentMappedView.close();
                this.mappableRingBuffer.rotate();
                this.currentGPUBuffer = this.mappableRingBuffer.currentBuffer();
                this.currentMappedView = encoder.mapBuffer(this.currentGPUBuffer, false, true);
                this.currentBuffer = this.currentMappedView.data();
            }

            @Override
            void destroyBuffer() {
                this.currentMappedView.close();
                this.mappableRingBuffer.close();
            }
        }
    }

    public static class UberGpuBufferHeap
    extends TlsfAllocator.Heap {
        GpuBuffer gpuBuffer;

        UberGpuBufferHeap(long size, GpuDevice gpuDevice, int usage, String name) {
            super(size);
            this.gpuBuffer = gpuDevice.createBuffer(() -> name, usage | 8 | 0x10, size);
        }
    }

    private static class StagedAllocationEntry<T> {
        @Nullable UploadCallback<T> callback;
        long offset;
        long size;

        private StagedAllocationEntry(@Nullable UploadCallback<T> callback, long offset, long size) {
            this.offset = offset;
            this.size = size;
            this.callback = callback;
        }
    }

    public static interface UploadCallback<T> {
        public void bufferHasBeenUploaded(T var1);
    }
}

