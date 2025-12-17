/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.scanner;

import net.minecraft.nbt.NbtType;

public interface NbtScanner {
    public Result visitEnd();

    public Result visitString(String var1);

    public Result visitByte(byte var1);

    public Result visitShort(short var1);

    public Result visitInt(int var1);

    public Result visitLong(long var1);

    public Result visitFloat(float var1);

    public Result visitDouble(double var1);

    public Result visitByteArray(byte[] var1);

    public Result visitIntArray(int[] var1);

    public Result visitLongArray(long[] var1);

    public Result visitListMeta(NbtType<?> var1, int var2);

    public NestedResult visitSubNbtType(NbtType<?> var1);

    public NestedResult startSubNbt(NbtType<?> var1, String var2);

    public NestedResult startListItem(NbtType<?> var1, int var2);

    public Result endNested();

    public Result start(NbtType<?> var1);

    public static final class NestedResult
    extends Enum<NestedResult> {
        public static final /* enum */ NestedResult ENTER = new NestedResult();
        public static final /* enum */ NestedResult SKIP = new NestedResult();
        public static final /* enum */ NestedResult BREAK = new NestedResult();
        public static final /* enum */ NestedResult HALT = new NestedResult();
        private static final /* synthetic */ NestedResult[] field_36252;

        public static NestedResult[] values() {
            return (NestedResult[])field_36252.clone();
        }

        public static NestedResult valueOf(String string) {
            return Enum.valueOf(NestedResult.class, string);
        }

        private static /* synthetic */ NestedResult[] method_39873() {
            return new NestedResult[]{ENTER, SKIP, BREAK, HALT};
        }

        static {
            field_36252 = NestedResult.method_39873();
        }
    }

    public static final class Result
    extends Enum<Result> {
        public static final /* enum */ Result CONTINUE = new Result();
        public static final /* enum */ Result BREAK = new Result();
        public static final /* enum */ Result HALT = new Result();
        private static final /* synthetic */ Result[] field_36256;

        public static Result[] values() {
            return (Result[])field_36256.clone();
        }

        public static Result valueOf(String string) {
            return Enum.valueOf(Result.class, string);
        }

        private static /* synthetic */ Result[] method_39874() {
            return new Result[]{CONTINUE, BREAK, HALT};
        }

        static {
            field_36256 = Result.method_39874();
        }
    }
}

