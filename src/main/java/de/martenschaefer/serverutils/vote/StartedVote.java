package de.martenschaefer.serverutils.vote;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * @param votes the value ({@code int}) is an index into options
 */
public record StartedVote(String name, Text displayName, int startTick, int secondsToLive, boolean announceEnd, String permission, List<VoteOption> options, Map<UUID, Integer> votes) {
    public static final Codec<StartedVote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(StartedVote::name),
        Codecs.TEXT.fieldOf("displayname").forGetter(StartedVote::displayName),
        Codec.INT.fieldOf("start_tick").forGetter(StartedVote::startTick),
        Codec.INT.fieldOf("seconds_to_live").forGetter(StartedVote::secondsToLive),
        Codec.BOOL.fieldOf("announce_end").forGetter(StartedVote::announceEnd),
        Codec.STRING.fieldOf("permission").forGetter(StartedVote::permission),
        VoteOption.CODEC.listOf().fieldOf("options").forGetter(StartedVote::options),
        Codec.unboundedMap(Uuids.INT_STREAM_CODEC, Codec.INT).fieldOf("votes").forGetter(StartedVote::votes)
    ).apply(instance, instance.stable(StartedVote::new)));
}
