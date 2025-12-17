/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.context.CommandContextBuilder
 *  com.mojang.brigadier.context.ParsedArgument
 *  com.mojang.brigadier.context.ParsedCommandNode
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.argument.SignedArgumentType;
import org.jspecify.annotations.Nullable;

public record SignedArgumentList<S>(List<ParsedArgument<S>> arguments) {
    public static <S> boolean isNotEmpty(ParseResults<S> parseResults) {
        return !SignedArgumentList.of(parseResults).arguments().isEmpty();
    }

    public static <S> SignedArgumentList<S> of(ParseResults<S> parseResults) {
        CommandContextBuilder commandContextBuilder3;
        CommandContextBuilder commandContextBuilder;
        String string = parseResults.getReader().getString();
        CommandContextBuilder commandContextBuilder2 = commandContextBuilder = parseResults.getContext();
        List<ParsedArgument<S>> list = SignedArgumentList.collectDecoratableArguments(string, commandContextBuilder2);
        while ((commandContextBuilder3 = commandContextBuilder2.getChild()) != null && commandContextBuilder3.getRootNode() != commandContextBuilder.getRootNode()) {
            list.addAll(SignedArgumentList.collectDecoratableArguments(string, commandContextBuilder3));
            commandContextBuilder2 = commandContextBuilder3;
        }
        return new SignedArgumentList<S>(list);
    }

    private static <S> List<ParsedArgument<S>> collectDecoratableArguments(String argumentName, CommandContextBuilder<S> builder) {
        ArrayList<ParsedArgument<S>> list = new ArrayList<ParsedArgument<S>>();
        for (ParsedCommandNode parsedCommandNode : builder.getNodes()) {
            com.mojang.brigadier.context.ParsedArgument parsedArgument;
            ArgumentCommandNode argumentCommandNode;
            CommandNode commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((argumentCommandNode = (ArgumentCommandNode)commandNode).getType() instanceof SignedArgumentType) || (parsedArgument = (com.mojang.brigadier.context.ParsedArgument)builder.getArguments().get(argumentCommandNode.getName())) == null) continue;
            String string = parsedArgument.getRange().get(argumentName);
            list.add(new ParsedArgument(argumentCommandNode, string));
        }
        return list;
    }

    public @Nullable ParsedArgument<S> get(String name) {
        for (ParsedArgument<S> parsedArgument : this.arguments) {
            if (!name.equals(parsedArgument.getNodeName())) continue;
            return parsedArgument;
        }
        return null;
    }

    public record ParsedArgument<S>(ArgumentCommandNode<S, ?> node, String value) {
        public String getNodeName() {
            return this.node.getName();
        }
    }
}

