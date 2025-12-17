/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 */
package net.minecraft.command;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.server.function.CommandFunction;

public record MacroInvocation(List<String> segments, List<String> variables) {
    public static MacroInvocation parse(String command) {
        ImmutableList.Builder builder = ImmutableList.builder();
        ImmutableList.Builder builder2 = ImmutableList.builder();
        int i = command.length();
        int j = 0;
        int k = command.indexOf(36);
        while (k != -1) {
            if (k == i - 1 || command.charAt(k + 1) != '(') {
                k = command.indexOf(36, k + 1);
                continue;
            }
            builder.add((Object)command.substring(j, k));
            int l = command.indexOf(41, k + 1);
            if (l == -1) {
                throw new IllegalArgumentException("Unterminated macro variable");
            }
            String string = command.substring(k + 2, l);
            if (!MacroInvocation.isValidMacroName(string)) {
                throw new IllegalArgumentException("Invalid macro variable name '" + string + "'");
            }
            builder2.add((Object)string);
            j = l + 1;
            k = command.indexOf(36, j);
        }
        if (j == 0) {
            throw new IllegalArgumentException("No variables in macro");
        }
        if (j != i) {
            builder.add((Object)command.substring(j));
        }
        return new MacroInvocation((List<String>)builder.build(), (List<String>)builder2.build());
    }

    public static boolean isValidMacroName(String name) {
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') continue;
            return false;
        }
        return true;
    }

    public String apply(List<String> arguments) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.variables.size(); ++i) {
            stringBuilder.append(this.segments.get(i)).append(arguments.get(i));
            CommandFunction.validateCommandLength(stringBuilder);
        }
        if (this.segments.size() > this.variables.size()) {
            stringBuilder.append(this.segments.getLast());
        }
        CommandFunction.validateCommandLength(stringBuilder);
        return stringBuilder.toString();
    }
}

