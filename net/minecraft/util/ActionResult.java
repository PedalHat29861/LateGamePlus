/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.ActionResult$Fail
 *  net.minecraft.util.ActionResult$ItemContext
 *  net.minecraft.util.ActionResult$Pass
 *  net.minecraft.util.ActionResult$PassToDefaultBlockAction
 *  net.minecraft.util.ActionResult$Success
 *  net.minecraft.util.ActionResult$SwingSource
 */
package net.minecraft.util;

import net.minecraft.util.ActionResult;

public sealed interface ActionResult {
    public static final Success SUCCESS = new Success(SwingSource.CLIENT, ItemContext.KEEP_HAND_STACK);
    public static final Success SUCCESS_SERVER = new Success(SwingSource.SERVER, ItemContext.KEEP_HAND_STACK);
    public static final Success CONSUME = new Success(SwingSource.NONE, ItemContext.KEEP_HAND_STACK);
    public static final Fail FAIL = new Fail();
    public static final Pass PASS = new Pass();
    public static final PassToDefaultBlockAction PASS_TO_DEFAULT_BLOCK_ACTION = new PassToDefaultBlockAction();

    default public boolean isAccepted() {
        return false;
    }
}
