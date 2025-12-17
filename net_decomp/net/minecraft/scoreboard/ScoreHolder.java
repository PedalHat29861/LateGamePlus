/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public interface ScoreHolder {
    public static final String WILDCARD_NAME = "*";
    public static final ScoreHolder WILDCARD = new ScoreHolder(){

        @Override
        public String getNameForScoreboard() {
            return ScoreHolder.WILDCARD_NAME;
        }
    };

    public String getNameForScoreboard();

    default public @Nullable Text getDisplayName() {
        return null;
    }

    default public Text getStyledDisplayName() {
        Text text = this.getDisplayName();
        if (text != null) {
            return text.copy().styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.literal(this.getNameForScoreboard()))));
        }
        return Text.literal(this.getNameForScoreboard());
    }

    public static ScoreHolder fromName(final String name) {
        if (name.equals(WILDCARD_NAME)) {
            return WILDCARD;
        }
        final MutableText text = Text.literal(name);
        return new ScoreHolder(){

            @Override
            public String getNameForScoreboard() {
                return name;
            }

            @Override
            public Text getStyledDisplayName() {
                return text;
            }
        };
    }

    public static ScoreHolder fromProfile(GameProfile gameProfile) {
        final String string = gameProfile.name();
        return new ScoreHolder(){

            @Override
            public String getNameForScoreboard() {
                return string;
            }
        };
    }
}

