package de.martenschaefer.serverutils.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import de.martenschaefer.serverutils.vote.StartedVote;
import de.martenschaefer.serverutils.vote.Vote;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class VoteStorage {
    public static final Codec<VoteStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, Vote.CODEC).fieldOf("votes").forGetter(VoteStorage::getVotes),
        Codec.unboundedMap(Codec.STRING, StartedVote.CODEC).fieldOf("started_votes").forGetter(VoteStorage::getStartedVotes)
    ).apply(instance, instance.stable(VoteStorage::new)));

    private final Map<String, Vote> votes;
    private final Map<String, StartedVote> startedVotes;

    public VoteStorage(Map<String, Vote> votes, Map<String, StartedVote> startedVotes) {
        this.votes = new HashMap<>(votes);
        this.startedVotes = new HashMap<>(startedVotes);
    }

    public VoteStorage() {
        this(new HashMap<>(), new HashMap<>());
    }

    public Optional<Vote> getUnstartedVote(String name) {
        return Optional.ofNullable(this.votes.get(name));
    }

    public Optional<StartedVote> getStartedVote(String name) {
        return Optional.ofNullable(this.startedVotes.get(name));
    }

    public List<String> getVoteNames() {
        return Stream.concat(this.votes.keySet().stream(), this.startedVotes.keySet().stream()).toList();
    }

    public Optional<Vote> addVote(String name) {
        if (this.votes.containsKey(name) || this.startedVotes.containsKey(name)) {
            return Optional.empty();
        }

        Vote vote = new Vote(name);
        this.votes.put(name, vote);
        return Optional.of(vote);
    }

    public boolean removeVote(String name) {
        if (this.votes.containsKey(name)) {
            this.votes.remove(name);
            return true;
        } else if (this.startedVotes.containsKey(name)) {
            this.startedVotes.remove(name);
            return true;
        } else {
            return false;
        }
    }

    public boolean startVote(String name, long startTime) {
        if (this.votes.containsKey(name)) {
            Vote vote = this.votes.get(name);

            this.votes.remove(name);
            this.startedVotes.put(name, vote.toStartedVote(startTime));
            return true;
        } else {
            return false;
        }
    }

    public List<String> getEndedVotes(long time) {
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, StartedVote> entry: this.startedVotes.entrySet()) {
            StartedVote vote = entry.getValue();

            if (vote.secondsToLive() > 0 && time >= vote.startTime() + 20L * vote.secondsToLive()) {
                result.add(vote.name());
            }
        }

        return result;
    }

    public Map<String, Vote> getVotes() {
        return this.votes;
    }

    public Map<String, StartedVote> getStartedVotes() {
        return this.startedVotes;
    }
}
