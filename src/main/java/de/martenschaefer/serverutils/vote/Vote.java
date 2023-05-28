package de.martenschaefer.serverutils.vote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Vote {
    public static final Codec<Vote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(Vote::getName),
        Codecs.TEXT.fieldOf("displayname").forGetter(Vote::getDisplayName),
        Codec.INT.fieldOf("seconds_to_live").forGetter(Vote::getSecondsToLive),
        Codec.BOOL.fieldOf("announce_end").forGetter(Vote::shouldAnnounceEnd),
        Codec.STRING.fieldOf("permission").forGetter(Vote::getPermission),
        VoteOption.CODEC.listOf().fieldOf("options").forGetter(Vote::getOptions)
    ).apply(instance, instance.stable(Vote::new)));

    private final String name;
    private Text displayName;
    private int secondsToLive;
    private boolean announceEnd;
    private String permission;
    private final List<VoteOption> options;
    private final Style nameStyle;

    public Vote(String name, Text displayName, int secondsToLive, boolean announceEnd, String permission, List<VoteOption> options) {
        this.name = name;
        this.displayName = displayName;
        this.secondsToLive = secondsToLive;
        this.announceEnd = announceEnd;
        this.permission = permission;
        this.options = options;
        this.nameStyle = Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(name)));
    }

    public Vote(String name) {
        this(name, Text.literal(name), 0, false, "", new ArrayList<>());
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

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<VoteOption> getOptions() {
        return this.options;
    }

    public StartedVote toStartedVote(long startTime) {
        return new StartedVote(this.name, this.displayName, startTime, this.secondsToLive, this.announceEnd, this.permission, this.options, new HashMap<>());
    }

    public MutableText getFormattedName() {
        return Texts.bracketed(this.displayName.copy().fillStyle(this.nameStyle));
    }
}
