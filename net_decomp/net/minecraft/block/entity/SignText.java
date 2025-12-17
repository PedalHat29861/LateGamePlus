/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.DyeColor;
import org.jspecify.annotations.Nullable;

public class SignText {
    private static final Codec<Text[]> MESSAGES_CODEC = TextCodecs.CODEC.listOf().comapFlatMap(messages -> {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.UnsupportedOperationException
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray.getDimSize(NewAnonymousArray.java:142)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.isNewArrayLambda(LambdaRewriter.java:455)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:409)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:103)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }, messages -> List.of(messages[0], messages[1], messages[2], messages[3]));
    public static final Codec<SignText> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MESSAGES_CODEC.fieldOf("messages").forGetter(signText -> signText.messages), (App)MESSAGES_CODEC.lenientOptionalFieldOf("filtered_messages").forGetter(SignText::getFilteredMessages), (App)DyeColor.CODEC.fieldOf("color").orElse((Object)DyeColor.BLACK).forGetter(signText -> signText.color), (App)Codec.BOOL.fieldOf("has_glowing_text").orElse((Object)false).forGetter(signText -> signText.glowing)).apply((Applicative)instance, SignText::create));
    public static final int field_43299 = 4;
    private final Text[] messages;
    private final Text[] filteredMessages;
    private final DyeColor color;
    private final boolean glowing;
    private OrderedText @Nullable [] orderedMessages;
    private boolean filtered;

    public SignText() {
        this(SignText.getDefaultText(), SignText.getDefaultText(), DyeColor.BLACK, false);
    }

    public SignText(Text[] messages, Text[] filteredMessages, DyeColor color, boolean glowing) {
        this.messages = messages;
        this.filteredMessages = filteredMessages;
        this.color = color;
        this.glowing = glowing;
    }

    private static Text[] getDefaultText() {
        return new Text[]{ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY, ScreenTexts.EMPTY};
    }

    private static SignText create(Text[] messages, Optional<Text[]> filteredMessages, DyeColor color, boolean glowing) {
        return new SignText(messages, filteredMessages.orElse(Arrays.copyOf(messages, messages.length)), color, glowing);
    }

    public boolean isGlowing() {
        return this.glowing;
    }

    public SignText withGlowing(boolean glowing) {
        if (glowing == this.glowing) {
            return this;
        }
        return new SignText(this.messages, this.filteredMessages, this.color, glowing);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public SignText withColor(DyeColor color) {
        if (color == this.getColor()) {
            return this;
        }
        return new SignText(this.messages, this.filteredMessages, color, this.glowing);
    }

    public Text getMessage(int line, boolean filtered) {
        return this.getMessages(filtered)[line];
    }

    public SignText withMessage(int line, Text message) {
        return this.withMessage(line, message, message);
    }

    public SignText withMessage(int line, Text message, Text filteredMessage) {
        Text[] texts = Arrays.copyOf(this.messages, this.messages.length);
        Text[] texts2 = Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
        texts[line] = message;
        texts2[line] = filteredMessage;
        return new SignText(texts, texts2, this.color, this.glowing);
    }

    public boolean hasText(PlayerEntity player) {
        return Arrays.stream(this.getMessages(player.shouldFilterText())).anyMatch(text -> !text.getString().isEmpty());
    }

    public Text[] getMessages(boolean filtered) {
        return filtered ? this.filteredMessages : this.messages;
    }

    public OrderedText[] getOrderedMessages(boolean filtered, Function<Text, OrderedText> messageOrderer) {
        if (this.orderedMessages == null || this.filtered != filtered) {
            this.filtered = filtered;
            this.orderedMessages = new OrderedText[4];
            for (int i = 0; i < 4; ++i) {
                this.orderedMessages[i] = messageOrderer.apply(this.getMessage(i, filtered));
            }
        }
        return this.orderedMessages;
    }

    private Optional<Text[]> getFilteredMessages() {
        for (int i = 0; i < 4; ++i) {
            if (this.filteredMessages[i].equals(this.messages[i])) continue;
            return Optional.of(this.filteredMessages);
        }
        return Optional.empty();
    }

    public boolean hasRunCommandClickEvent(PlayerEntity player) {
        for (Text text : this.getMessages(player.shouldFilterText())) {
            Style style = text.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) continue;
            return true;
        }
        return false;
    }
}

