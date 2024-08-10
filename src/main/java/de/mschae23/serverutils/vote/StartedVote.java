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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class StartedVote {
    public static final Codec<StartedVote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(StartedVote::name),
        Codecs.TEXT.fieldOf("displayname").forGetter(StartedVote::displayName),
        Codec.LONG.fieldOf("start_time").forGetter(StartedVote::startTime),
        Codec.INT.fieldOf("seconds_to_live").forGetter(StartedVote::secondsToLive),
        Codec.BOOL.fieldOf("announce_end").forGetter(StartedVote::announceEnd),
        Codec.STRING.fieldOf("permission").forGetter(StartedVote::permission),
        VoteOption.CODEC.listOf().fieldOf("options").forGetter(StartedVote::options),
        Codec.unboundedMap(Uuids.INT_STREAM_CODEC, Codec.INT).fieldOf("votes").forGetter(StartedVote::votes)
    ).apply(instance, instance.stable(StartedVote::new)));

    private final String name;
    private final Text displayName;
    private final long startTime;
    private final int secondsToLive;
    private final boolean announceEnd;
    private final String permission;
    private final List<VoteOption> options;
    private final Map<UUID, Integer> votes;
    private final Style nameStyle;

    /**
     * @param votes the value ({@code int}) is an index into options
     */
    public StartedVote(String name, Text displayName, long startTime, int secondsToLive, boolean announceEnd, String permission, List<VoteOption> options, Map<UUID, Integer> votes) {
        this.name = name;
        this.displayName = displayName;
        this.startTime = startTime;
        this.secondsToLive = secondsToLive;
        this.announceEnd = announceEnd;
        this.permission = permission;
        this.options = options;
        this.votes = new HashMap<>(votes);
        this.nameStyle = Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(name)));
    }

    public String name() {
        return this.name;
    }

    public Text displayName() {
        return this.displayName;
    }

    public long startTime() {
        return this.startTime;
    }

    public int secondsToLive() {
        return this.secondsToLive;
    }

    public boolean announceEnd() {
        return this.announceEnd;
    }

    public String permission() {
        return this.permission;
    }

    public List<VoteOption> options() {
        return this.options;
    }

    public Map<UUID, Integer> votes() {
        return this.votes;
    }

    public MutableText getFormattedName() {
        return Texts.bracketed(this.displayName.copy().fillStyle(this.nameStyle));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StartedVote) obj;
        return Objects.equals(this.name, that.name) &&
            Objects.equals(this.displayName, that.displayName) &&
            this.startTime == that.startTime &&
            this.secondsToLive == that.secondsToLive &&
            this.announceEnd == that.announceEnd &&
            Objects.equals(this.permission, that.permission) &&
            Objects.equals(this.options, that.options) &&
            Objects.equals(this.votes, that.votes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.displayName, this.startTime, this.secondsToLive, this.announceEnd, this.permission, this.options, this.votes);
    }

    @Override
    public String toString() {
        return "StartedVote[" +
            "name=" + this.name + ", " +
            "displayName=" + this.displayName + ", " +
            "startTick=" + this.startTime + ", " +
            "secondsToLive=" + this.secondsToLive + ", " +
            "announceEnd=" + this.announceEnd + ", " +
            "permission=" + this.permission + ", " +
            "options=" + this.options + ", " +
            "votes=" + this.votes + ']';
    }
}
