package de.martenschaefer.serverutils.command;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.state.VotePersistentState;
import de.martenschaefer.serverutils.state.VoteStorage;
import de.martenschaefer.serverutils.vote.StartedVote;
import de.martenschaefer.serverutils.vote.Vote;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

public final class VoteCommand {
    public static final String PERMISSION_ROOT = ".command.vote.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
    };

    private static final Function<Object, MutableText> FORMAT_NAME = name -> Texts.bracketed(Text.literal((String) name)
        .fillStyle(Style.EMPTY.withInsertion((String) name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal((String) name)))));

    private static final DynamicCommandExceptionType ADD_DUPLICATE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append(FORMAT_NAME.apply(name)).append(" already exists."));
    private static final DynamicCommandExceptionType DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append(FORMAT_NAME.apply(name)).append(" does not exist."));
    private static final DynamicCommandExceptionType ALREADY_STARTED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" has already started."));
    private static final DynamicCommandExceptionType NEEDS_OPTIONS_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" cannot be started, because it has no options."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    }

    public static int executeAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VotePersistentState voteState = VotePersistentState.get(source.getServer());
        VoteStorage storage = voteState.getStorage();
        String name = StringArgumentType.getString(context, "name");

        Optional<Vote> voteOption = storage.addVote(name);

        if (voteOption.isPresent()) {
            Vote vote = voteOption.get();

            source.sendFeedback(Text.empty()
                .append("Added vote ").append(vote.getFormattedName()).append("."), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw ADD_DUPLICATE_EXCEPTION.create(name);
        }
    }

    public static int executeRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VotePersistentState voteState = VotePersistentState.get(source.getServer());
        VoteStorage storage = voteState.getStorage();
        String name = StringArgumentType.getString(context, "name");

        Optional<Vote> unstartedVoteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);
        MutableText formattedName = unstartedVoteOption.map(Vote::getFormattedName)
            .or(() -> startedVoteOption.map(StartedVote::getFormattedName))
            .orElseGet(() -> Texts.bracketed(Text.literal(name)
                .fillStyle(Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(name))))));

        boolean success = storage.removeVote(name);

        if (success) {
            source.sendFeedback(Text.empty()
                .append("Removed vote ").append(formattedName).append("."), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeStart(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VotePersistentState voteState = VotePersistentState.get(source.getServer());
        VoteStorage storage = voteState.getStorage();
        String name = StringArgumentType.getString(context, "name");

        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw ALREADY_STARTED_EXCEPTION.create(vote.getFormattedName());
        }

        Optional<Vote> voteOption = storage.getUnstartedVote(name);

        if (voteOption.isEmpty()) {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        } else {
            Vote vote = voteOption.get();

            if (vote.getOptions().isEmpty()) {
                throw NEEDS_OPTIONS_EXCEPTION.create(vote.getFormattedName());
            }
        }

        @SuppressWarnings("DataFlowIssue") // getWorld() is Nullable, but not for the Overworld
        boolean success = storage.startVote(name, source.getServer().getWorld(World.OVERWORLD).getTime());

        if (success) {
            source.sendFeedback(Text.empty()
                .append("Started vote ").append(storage.getStartedVote(name).map(StartedVote::getFormattedName).orElseGet(() -> FORMAT_NAME.apply(name)))
                .append("."), true);
            return Command.SINGLE_SUCCESS;
        } else {
            return 0;
        }
    }
}
