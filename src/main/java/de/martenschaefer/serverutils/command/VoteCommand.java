package de.martenschaefer.serverutils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
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
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class VoteCommand {
    public static final String PERMISSION_ROOT = ".command.vote.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
    };

    private static final Function<Object, MutableText> FORMAT_NAME = name -> Texts.bracketed(Text.literal((String) name)
        .fillStyle(Style.EMPTY.withInsertion((String) name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal((String) name)))));

    private static final DynamicCommandExceptionType ADD_DUPLICATE_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" already exists."));
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
    private static final DynamicCommandExceptionType NOT_STARTED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("Vote ").append((Text) name).append(" has not started yet."));
    private static final DynamicCommandExceptionType MISSING_PERMISSION_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("You do not have permission to vote for ").append((Text) name).append("."));
    private static final DynamicCommandExceptionType ALREADY_VOTED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.empty()
        .append("You already voted for ").append((Text) name).append("."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("vote")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, true))
            // TODO Create VoteArgumentType and VoteOptionArgumentType (if possible)
            .then(CommandManager.argument("name", StringArgumentType.word())
                .executes(VoteCommand::executeVoteList)
                .then(CommandManager.argument("option_name", StringArgumentType.word())
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .executes(VoteCommand::executeVote))));
    }

    public static int executeVoteList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        Optional<Vote> unstartedVoteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (unstartedVoteOption.isEmpty() && startedVoteOption.isEmpty()) {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        } else if (unstartedVoteOption.isPresent()) {
            Vote vote = unstartedVoteOption.get();
            throw NOT_STARTED_EXCEPTION.create(vote.getFormattedName());
        }

        StartedVote vote = startedVoteOption.get();

        List<VoteOption> options = vote.options();

        MutableText result = Text.empty().append("Options in vote ").append(Texts.bracketed(Text.empty()
            .append(vote.displayName()).styled(style -> style.withColor(Formatting.RESET))).styled(style -> style.withColor(Formatting.GRAY)))
            .append(":");

        if (!options.isEmpty()) {
            result.append("\n");
        }

        for (int i = 0; i < options.size(); i++) {
            VoteOption option = options.get(i);

            result.append(" ").append(Texts.bracketed(option.getDisplayName().copy().fillStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/vote " + name + " " + option.getName()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(option.getName()))))));

            if (i < options.size() - 1) {
                result.append("\n");
            }
        }

        if (!vote.permission().isEmpty() && !Permissions.check(source, ServerUtilsMod.MODID + "." + ServerUtilsMod.getConfig().vote().permissionPrefix() + "." + vote.permission())) {
            result.append("\n").append(Text.literal("You do not have permission to vote.").styled(style -> style.withColor(Formatting.RED)));
        }

        source.sendFeedback(result, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int executeVote(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VoteStorage storage = VotePersistentState.get(source.getServer()).getStorage();
        String name = StringArgumentType.getString(context, "name");
        String optionName = StringArgumentType.getString(context, "option_name");
        Optional<Vote> unstartedVoteOption = storage.getUnstartedVote(name);
        Optional<StartedVote> startedVoteOption = storage.getStartedVote(name);

        if (unstartedVoteOption.isEmpty() && startedVoteOption.isEmpty()) {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        } else if (unstartedVoteOption.isPresent()) {
            Vote vote = unstartedVoteOption.get();
            throw NOT_STARTED_EXCEPTION.create(vote.getFormattedName());
        }

        StartedVote vote = startedVoteOption.get();

        if (!vote.permission().isEmpty() && !Permissions.check(source, ServerUtilsMod.MODID + "." + ServerUtilsMod.getConfig().vote().permissionPrefix() + "." + vote.permission())) {
            throw MISSING_PERMISSION_EXCEPTION.create(vote.getFormattedName());
        }

        Optional<Integer> optionOption = Stream.iterate(0, i -> ++i).limit(vote.options().size())
            .filter(i -> vote.options().get(i).getName().equals(optionName)).findAny();

        if (optionOption.isEmpty()) {
            throw OPTION_DOES_NOT_EXIST_EXCEPTION.create(optionName);
        } else {
            @SuppressWarnings("DataFlowIssue") // Command has requirement isExecutedByPlayer
            UUID playerUuid = source.getPlayer().getUuid();

            int optionIndex = optionOption.get();
            // VoteOption option = vote.options().get(optionIndex);

            if (vote.votes().containsKey(playerUuid)) {
                throw ALREADY_VOTED_EXCEPTION.create(vote.getFormattedName());
            }

            vote.votes().put(playerUuid, optionIndex);

            source.sendFeedback(Text.empty()
                .append("Voted for ").append(vote.getFormattedName()).append("."), true);
            return Command.SINGLE_SUCCESS;
        }
    }

    // Admin commands implementations

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
            throw ADD_DUPLICATE_EXCEPTION.create(storage.getUnstartedVote(name).map(Vote::getFormattedName)
                .or(() -> storage.getStartedVote(name).map(StartedVote::getFormattedName)).orElseGet(() -> FORMAT_NAME.apply(name)));
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

    public static int executeEnd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        VotePersistentState voteState = VotePersistentState.get(source.getServer());
        VoteStorage storage = voteState.getStorage();
        String name = StringArgumentType.getString(context, "name");

        Optional<Vote> unstartedVoteOption = storage.getUnstartedVote(name);

        if (unstartedVoteOption.isPresent()) {
            Vote vote = unstartedVoteOption.get();
            throw NOT_STARTED_EXCEPTION.create(vote.getFormattedName());
        }

        Optional<StartedVote> voteOption = storage.getStartedVote(name);

        if (voteOption.isEmpty()) {
            throw DOES_NOT_EXIST_EXCEPTION.create(name);
        } else {
            StartedVote vote = voteOption.get();

            int[] counts = new int[vote.options().size()];
            int totalCount = vote.votes().size();

            for (Map.Entry<UUID, Integer> entry : vote.votes().entrySet()) {
                int index = entry.getValue();
                counts[index]++;
            }

            int place1Count = -1;
            List<Integer> place1Indices = new ArrayList<>();
            int place2Count = -1;
            List<Integer> place2Indices = new ArrayList<>();
            int place3Count = -1;
            List<Integer> place3Indices = new ArrayList<>();

            for (int i = 0; i < counts.length; i++) {
                int count = counts[i];

                if (count > place1Count) {
                    place3Count = place2Count;
                    place3Indices = place2Indices;
                    place2Count = place1Count;
                    place2Indices = place1Indices;
                    place1Count = count;
                    place1Indices = new ArrayList<>();
                    place1Indices.add(i);
                } else if (count == place1Count) {
                    place1Indices.add(i);
                } else if (count > place2Count) {
                    place3Count = place2Count;
                    place3Indices = place2Indices;
                    place2Count = count;
                    place2Indices = new ArrayList<>();
                    place2Indices.add(i);
                } else if (count == place2Count) {
                    place2Indices.add(i);
                } else if (count > place3Count) {
                    place3Count = count;
                    place3Indices = new ArrayList<>();
                    place3Indices.add(i);
                } else if (count == place3Count) {
                    place3Indices.add(i);
                }
            }

            MutableText result = Text.empty().append("Results of vote ").append(Texts.bracketed(Text.empty()
                    .append(vote.displayName()).styled(style -> style.withColor(Formatting.RESET))).styled(style -> style.withColor(Formatting.GRAY)))
                .append(":");

            if (!vote.options().isEmpty()) {
                result.append("\n");
            }

            for (int i = 0; i < vote.options().size(); i++) {
                VoteOption option = vote.options().get(i);

                boolean place1 = place1Indices.contains(i);
                boolean place2 = !place1 && place2Indices.contains(i);
                boolean place3 = !place2 && !place1 && place3Indices.contains(i);
                Formatting color = place1 ? Formatting.GREEN : place2 && vote.options().size() > 2 ? Formatting.YELLOW
                    : place3 || (place2 && vote.options().size() == 2) ? Formatting.RED : Formatting.RESET;

                int voteStringLength = place1Count == 0 ? 1 : (((int) Math.floor(Math.log10(place1Count))) + 1);

                MutableText optionCount = Text.literal(String.format("%" + voteStringLength + "d", counts[i]));

                if (color != Formatting.RESET) {
                    optionCount.styled(style -> style.withColor(color));
                }

                MutableText prefix = Texts.bracketed(Text.empty().append(optionCount).append(" / ").append(String.valueOf(totalCount)));
                MutableText optionName = Text.empty().append(option.getDisplayName());

                if (color != Formatting.RESET) {
                    optionName.styled(style -> style.withColor(color));
                }

                result.append(Text.empty().append(prefix).append(" ").append(optionName));

                if (i < vote.options().size() - 1) {
                    result.append("\n");
                }
            }

            if (vote.announceEnd()) {
                source.getServer().getPlayerManager().broadcast(result, false);
            } else {
                source.sendFeedback(result, true);
            }

            storage.removeVote(name);
            return Command.SINGLE_SUCCESS;
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
                .append(Text.literal(ServerUtilsMod.MODID + "." + ServerUtilsMod.getConfig().vote().permissionPrefix() + "." + permission).styled(style -> style.withColor(Formatting.GRAY))).append("."), true);
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

            result.append(Text.literal(" - ").append(vote.getFormattedName()).append(" ").append(vote.getName()).append("\n"));
        }

        result.append(Text.literal("Started votes:"));

        for (Map.Entry<String, StartedVote> entry : storage.getStartedVotes().entrySet()) {
            StartedVote vote = entry.getValue();

            result.append(Text.literal("\n - ")).append(vote.getFormattedName()).append(" ").append(vote.name());
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

        MutableText result = Text.empty().append("Options in vote ").append(formattedName).append(":");

        for (VoteOption option : options) {
            result.append("\n - ").append(option.getFormattedName()).append(" ").append(option.getName());
        }

        source.sendFeedback(result, false);
        return Command.SINGLE_SUCCESS;
    }
}
