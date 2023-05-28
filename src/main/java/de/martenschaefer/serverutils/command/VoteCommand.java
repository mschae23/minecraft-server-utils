package de.martenschaefer.serverutils.command;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.state.VotePersistentState;
import de.martenschaefer.serverutils.state.VoteStorage;
import de.martenschaefer.serverutils.vote.StartedVote;
import de.martenschaefer.serverutils.vote.Vote;
import de.martenschaefer.serverutils.vote.VoteOption;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import eu.pb4.placeholders.api.PlaceholderContext;

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
    private static final DynamicCommandExceptionType START_TWICE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" has already started."));
    private static final DynamicCommandExceptionType NEEDS_OPTIONS_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" cannot be started, because it has no options."));
    private static final DynamicCommandExceptionType MODIFY_STARTED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" cannot be modified, as it has already started."));
    private static final DynamicCommandExceptionType ADD_DUPLICATE_OPTION_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Option ").append(FORMAT_NAME.apply(name)).append(" already exists."));
    private static final DynamicCommandExceptionType OPTION_DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Option ").append(FORMAT_NAME.apply(name)).append(" does not exist."));

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
            throw START_TWICE_EXCEPTION.create(vote.getFormattedName());
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

    public static int executeModifyDisplayName(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        String displayName = StringArgumentType.getString(context, "display_name");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();
            vote.setDisplayName(ModUtils.createNodeParser(source.getPlayer()).parseText(displayName, PlaceholderContext.of(source).asParserContext()));

            source.sendFeedback(Text.empty()
                .append("Modified display name of vote ").append(vote.getFormattedName()).append(Text.literal(".")), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeModifyAddOption(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        String optionName = StringArgumentType.getString(context, "option_name");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();

            if (vote.getOptions().stream().anyMatch(option -> option.getName().equals(optionName))) {
                throw ADD_DUPLICATE_OPTION_EXCEPTION.create(optionName);
            }

            VoteOption option = new VoteOption(optionName);
            vote.getOptions().add(option);

            source.sendFeedback(Text.empty()
                .append("Added option ").append(option.getFormattedName())
                .append(" to vote ").append(vote.getFormattedName()).append(Text.literal(".")), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeModifyRemoveOption(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        String optionName = StringArgumentType.getString(context, "option_name");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();
            Optional<VoteOption> optionOption = vote.getOptions().stream().filter(testOption -> testOption.getName().equals(optionName)).findAny();

            if (optionOption.isEmpty()) {
                throw OPTION_DOES_NOT_EXIST_EXCEPTION.create(optionName);
            }

            VoteOption option = optionOption.get();
            vote.getOptions().removeIf(testOption -> testOption.getName().equals(optionName));

            source.sendFeedback(Text.empty()
                .append("Removed option ").append(option.getFormattedName())
                .append(" from vote ").append(vote.getFormattedName()).append(Text.literal(".")), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeModifyOptionModifyDisplayName(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        String optionName = StringArgumentType.getString(context, "option_name");
        String displayName = StringArgumentType.getString(context, "display_name");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();
            Optional<VoteOption> optionOption = vote.getOptions().stream().filter(testOption -> testOption.getName().equals(optionName)).findAny();

            if (optionOption.isEmpty()) {
                throw OPTION_DOES_NOT_EXIST_EXCEPTION.create(optionName);
            }

            VoteOption option = optionOption.get();
            option.setDisplayName(ModUtils.createNodeParser(source.getPlayer()).parseText(displayName, PlaceholderContext.of(source).asParserContext()));

            source.sendFeedback(Text.empty()
                .append("Modified display name of option ").append(option.getFormattedName())
                .append(" from vote ").append(vote.getFormattedName()).append(Text.literal(".")), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeModifySecondsToLive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        int secondsToLive = IntegerArgumentType.getInteger(context, "seconds_to_live");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();
            vote.setSecondsToLive(secondsToLive);

            source.sendFeedback(Text.empty()
                .append("Set seconds to live of vote ").append(vote.getFormattedName()).append(Text.literal(" to " + secondsToLive + ".")), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeModifyAnnounceEnd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        boolean announceEnd = BoolArgumentType.getBool(context, "announce_end");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();
            vote.setAnnounceEnd(announceEnd);

            source.sendFeedback(Text.empty()
                .append("Set announcement of vote ").append(vote.getFormattedName()).append(Text.literal(" end to " + announceEnd + ".")), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeModifyPermission(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        String permission = StringArgumentType.getString(context, "permission");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (startedVoteOption.isPresent()) {
            StartedVote vote = startedVoteOption.get();
            throw MODIFY_STARTED_EXCEPTION.create(vote.getFormattedName());
        } else if (voteOption.isPresent()) {
            Vote vote = voteOption.get();
            vote.setPermission(permission);

            source.sendFeedback(Text.empty()
                .append("Set permission for vote ").append(vote.getFormattedName()).append(Text.literal(" to "))
                .append(Text.literal(permission).styled(style -> style.withColor(Formatting.GRAY))).append("."), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }
    }

    public static int executeList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();

        MutableText result = Text.empty().append("Unstarted votes:\n");

        for (Map.Entry<String, Vote> entry : storage.getVotes().entrySet()) {
            Vote vote = entry.getValue();

            result.append(Text.literal("- ").append(vote.getFormattedName()).append(" ").append(vote.getName()).append("\n"));
        }

        result.append(Text.literal("Started votes:\n"));

        for (Map.Entry<String, StartedVote> entry : storage.getStartedVotes().entrySet()) {
            StartedVote vote = entry.getValue();

            result.append(Text.literal("- ").append(vote.getFormattedName()).append(" ").append(vote.name()).append("\n"));
        }

        source.sendFeedback(result, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeListOptions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        Optional<Vote> voteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);
        Optional<MutableText> formattedNameOption = voteOption.map(Vote::getFormattedName).or(() -> startedVoteOption.map(StartedVote::getFormattedName));
        Optional<List<VoteOption>> optionsOption = voteOption.map(Vote::getOptions).or(() -> startedVoteOption.map(StartedVote::options));

        if (optionsOption.isEmpty() || formattedNameOption.isEmpty()) {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        }

        List<VoteOption> options = optionsOption.get();
        MutableText formattedName = formattedNameOption.get();

        MutableText result = Text.empty().append("Options in vote ").append(formattedName).append(":\n");

        for (VoteOption option : options) {
            result.append("- ").append(option.getFormattedName()).append(" ").append(option.getName()).append("\n");
        }

        source.sendFeedback(result, false);
        return Command.SINGLE_SUCCESS;
    }
}
