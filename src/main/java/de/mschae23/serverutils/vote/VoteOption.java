/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Server utils.
 *
 * Server utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.serverutils.vote;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class VoteOption {
        public static final Codec<VoteOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(VoteOption::getName),
        TextCodecs.CODEC.fieldOf("displayname").forGetter(VoteOption::getDisplayName)
    ).apply(instance, instance.stable(VoteOption::new)));

    private final String name;
    private Text displayName;
    private final Style nameStyle;

    public VoteOption(String name, Text displayName) {
        this.name = name;
        this.displayName = displayName;
        this.nameStyle = Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(name)));
    }

    public VoteOption(String name) {
        this(name, Text.literal(name));
    }

    public String getName() {
        return this.name;
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(Text displayName) {
        this.displayName = displayName;
    }

    public MutableText getFormattedName() {
        return Texts.bracketed(this.displayName.copy().fillStyle(this.nameStyle));
    }
}
