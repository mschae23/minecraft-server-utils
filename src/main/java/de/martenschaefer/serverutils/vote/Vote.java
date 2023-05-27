package de.martenschaefer.serverutils.vote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Vote {
    public static final Codec<Vote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(Vote::getName),
        Codecs.TEXT.fieldOf("displayname").forGetter(Vote::getDisplayName),
        Codec.INT.fieldOf("seconds_to_live").forGetter(Vote::getSecondsToLive),
        Codec.BOOL.fieldOf("announce_end").forGetter(Vote::shouldAnnounceEnd),
        VoteOption.CODEC.listOf().fieldOf("options").forGetter(Vote::getOptions)
    ).apply(instance, instance.stable(Vote::new)));

    private final String name;
    private Text displayName;
    private int secondsToLive;
    private boolean announceEnd;
    private final List<VoteOption> options;

    public Vote(String name, Text displayName, int secondsToLive, boolean announceEnd, List<VoteOption> options) {
        this.name = name;
        this.displayName = displayName;
        this.secondsToLive = secondsToLive;
        this.announceEnd = announceEnd;
        this.options = options;
    }

    public Vote(String name) {
        this(name, Text.literal(name), 0, false, new ArrayList<>());
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

    public int getSecondsToLive() {
        return this.secondsToLive;
    }

    public void setSecondsToLive(int secondsToLive) {
        this.secondsToLive = secondsToLive;
    }

    public boolean shouldAnnounceEnd() {
        return this.announceEnd;
    }

    public void setAnnounceEnd(boolean announceEnd) {
        this.announceEnd = announceEnd;
    }

    public List<VoteOption> getOptions() {
        return this.options;
    }

    public StartedVote toStartedVote(int startTick) {
        return new StartedVote(this.name, this.displayName, startTick, this.secondsToLive, this.announceEnd, this.options, new HashMap<>());
    }
}
