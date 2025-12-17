/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

public interface ClickEvent {
    public static final Codec<ClickEvent> CODEC = Action.CODEC.dispatch("action", ClickEvent::getAction, action -> action.codec);

    public Action getAction();

    public static final class Action
    extends Enum<Action>
    implements StringIdentifiable {
        public static final /* enum */ Action OPEN_URL = new Action("open_url", true, OpenUrl.CODEC);
        public static final /* enum */ Action OPEN_FILE = new Action("open_file", false, OpenFile.CODEC);
        public static final /* enum */ Action RUN_COMMAND = new Action("run_command", true, RunCommand.CODEC);
        public static final /* enum */ Action SUGGEST_COMMAND = new Action("suggest_command", true, SuggestCommand.CODEC);
        public static final /* enum */ Action SHOW_DIALOG = new Action("show_dialog", true, ShowDialog.CODEC);
        public static final /* enum */ Action CHANGE_PAGE = new Action("change_page", true, ChangePage.CODEC);
        public static final /* enum */ Action COPY_TO_CLIPBOARD = new Action("copy_to_clipboard", true, CopyToClipboard.CODEC);
        public static final /* enum */ Action CUSTOM = new Action("custom", true, Custom.CODEC);
        public static final Codec<Action> UNVALIDATED_CODEC;
        public static final Codec<Action> CODEC;
        private final boolean userDefinable;
        private final String name;
        final MapCodec<? extends ClickEvent> codec;
        private static final /* synthetic */ Action[] field_11747;

        public static Action[] values() {
            return (Action[])field_11747.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private Action(String name, boolean userDefinable, MapCodec<? extends ClickEvent> codec) {
            this.name = name;
            this.userDefinable = userDefinable;
            this.codec = codec;
        }

        public boolean isUserDefinable() {
            return this.userDefinable;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public MapCodec<? extends ClickEvent> getCodec() {
            return this.codec;
        }

        public static DataResult<Action> validate(Action action) {
            if (!action.isUserDefinable()) {
                return DataResult.error(() -> "Click event type not allowed: " + String.valueOf(action));
            }
            return DataResult.success((Object)action, (Lifecycle)Lifecycle.stable());
        }

        private static /* synthetic */ Action[] method_36945() {
            return new Action[]{OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, SHOW_DIALOG, CHANGE_PAGE, COPY_TO_CLIPBOARD, CUSTOM};
        }

        static {
            field_11747 = Action.method_36945();
            UNVALIDATED_CODEC = StringIdentifiable.createCodec(Action::values);
            CODEC = UNVALIDATED_CODEC.validate(Action::validate);
        }
    }

    public record Custom(Identifier id, Optional<NbtElement> payload) implements ClickEvent
    {
        public static final MapCodec<Custom> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("id").forGetter(Custom::id), (App)Codecs.NBT_ELEMENT.optionalFieldOf("payload").forGetter(Custom::payload)).apply((Applicative)instance, Custom::new));

        @Override
        public Action getAction() {
            return Action.CUSTOM;
        }
    }

    public record CopyToClipboard(String value) implements ClickEvent
    {
        public static final MapCodec<CopyToClipboard> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("value").forGetter(CopyToClipboard::value)).apply((Applicative)instance, CopyToClipboard::new));

        @Override
        public Action getAction() {
            return Action.COPY_TO_CLIPBOARD;
        }
    }

    public record ChangePage(int page) implements ClickEvent
    {
        public static final MapCodec<ChangePage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.POSITIVE_INT.fieldOf("page").forGetter(ChangePage::page)).apply((Applicative)instance, ChangePage::new));

        @Override
        public Action getAction() {
            return Action.CHANGE_PAGE;
        }
    }

    public record ShowDialog(RegistryEntry<Dialog> dialog) implements ClickEvent
    {
        public static final MapCodec<ShowDialog> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Dialog.ENTRY_CODEC.fieldOf("dialog").forGetter(ShowDialog::dialog)).apply((Applicative)instance, ShowDialog::new));

        @Override
        public Action getAction() {
            return Action.SHOW_DIALOG;
        }
    }

    public record SuggestCommand(String command) implements ClickEvent
    {
        public static final MapCodec<SuggestCommand> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.CHAT_TEXT.fieldOf("command").forGetter(SuggestCommand::command)).apply((Applicative)instance, SuggestCommand::new));

        @Override
        public Action getAction() {
            return Action.SUGGEST_COMMAND;
        }
    }

    public record RunCommand(String command) implements ClickEvent
    {
        public static final MapCodec<RunCommand> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.CHAT_TEXT.fieldOf("command").forGetter(RunCommand::command)).apply((Applicative)instance, RunCommand::new));

        @Override
        public Action getAction() {
            return Action.RUN_COMMAND;
        }
    }

    public record OpenFile(String path) implements ClickEvent
    {
        public static final MapCodec<OpenFile> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("path").forGetter(OpenFile::path)).apply((Applicative)instance, OpenFile::new));

        public OpenFile(File file) {
            this(file.toString());
        }

        public OpenFile(Path path) {
            this(path.toFile());
        }

        public File file() {
            return new File(this.path);
        }

        @Override
        public Action getAction() {
            return Action.OPEN_FILE;
        }
    }

    public record OpenUrl(URI uri) implements ClickEvent
    {
        public static final MapCodec<OpenUrl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.URI.fieldOf("url").forGetter(OpenUrl::uri)).apply((Applicative)instance, OpenUrl::new));

        @Override
        public Action getAction() {
            return Action.OPEN_URL;
        }
    }
}

