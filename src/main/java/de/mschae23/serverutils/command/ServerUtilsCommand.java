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

package de.mschae23.serverutils.command;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.state.VotePersistentState;
import de.mschae23.serverutils.state.VoteStorage;
import de.mschae23.serverutils.vote.Vote;
import de.mschae23.serverutils.vote.VoteOption;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class ServerUtilsCommand {
    public static final String PERMISSION_ROOT = ".command." + ServerUtilsMod.MODID + ".root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command." + ServerUtilsMod.MODID + ".vote",
    };

    private static final SuggestionProvider<ServerCommandSource> VOTE_NAME_SUGGESTION_PROVIDER = (context, builder) -> {
        VoteStorage storage = VotePersistentState.get(context.getSource().getServer()).getStorage();
        Stream<String> votes = Stream.concat(storage.getVotes().keySet().stream(), storage.getStartedVotes().keySet().stream());

        CommandSource.suggestMatching(votes, builder);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> UNSTARTED_VOTE_NAME_SUGGESTION_PROVIDER = (context, builder) -> {
        VoteStorage storage = VotePersistentState.get(context.getSource().getServer()).getStorage();
        Set<String> votes = storage.getVotes().keySet();

        CommandSource.suggestMatching(votes, builder);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> STARTED_VOTE_NAME_SUGGESTION_PROVIDER = (context, builder) -> {
        VoteStorage storage = VotePersistentState.get(context.getSource().getServer()).getStorage();
        Set<String> votes = storage.getStartedVotes().keySet();

        CommandSource.suggestMatching(votes, builder);
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> VOTE_OPTION_NAME_SUGGESTION_PROVIDER = (context, builder) -> {
        VoteStorage storage = VotePersistentState.get(context.getSource().getServer()).getStorage();

        try {
            String voteName = StringArgumentType.getString(context, "name");
            Optional<Vote> voteOption = storage.getUnstartedVote(voteName);

            voteOption.ifPresent(vote ->
                CommandSource.suggestMatching(vote.getOptions().stream().map(VoteOption::getName), builder));
        } catch (IllegalArgumentException e) {
            // Ignore
        }

        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(ServerUtilsMod.MODID)
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, 2))
            .then(CommandManager.literal("vote")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command." + ServerUtilsMod.MODID + ".vote", true))
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeAdd)))
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("name", StringArgumentType.word()).suggests(VOTE_NAME_SUGGESTION_PROVIDER)
                        .executes(VoteCommand::executeRemove)))
                .then(CommandManager.literal("modify")
                    .then(CommandManager.argument("name", StringArgumentType.word()).suggests(UNSTARTED_VOTE_NAME_SUGGESTION_PROVIDER)
                        .then(CommandManager.literal("display_name")
                            .then(CommandManager.argument("display_name", StringArgumentType.greedyString())
                                .executes(VoteCommand::executeModifyDisplayName)))
                        .then(CommandManager.literal("option")
                            .then(CommandManager.literal("add")
                                .then(CommandManager.argument("option_name", StringArgumentType.word())
                                    .executes(VoteCommand::executeModifyAddOption)))
                            .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("option_name", StringArgumentType.word()).suggests(VOTE_OPTION_NAME_SUGGESTION_PROVIDER)
                                    .executes(VoteCommand::executeModifyRemoveOption)))
                            .then(CommandManager.literal("modify")
                                .then(CommandManager.literal("display_name")
                                    .then(CommandManager.argument("option_name", StringArgumentType.word()).suggests(VOTE_OPTION_NAME_SUGGESTION_PROVIDER)
                                        .then(CommandManager.argument("display_name", StringArgumentType.greedyString())
                                        .executes(VoteCommand::executeModifyOptionModifyDisplayName)))))
                            .then(CommandManager.literal("list")
                                .executes(VoteCommand::executeListOptions)))
                        .then(CommandManager.literal("seconds_to_live")
                            .then(CommandManager.argument("seconds_to_live", IntegerArgumentType.integer(0, 1000000))
                                .executes(VoteCommand::executeModifySecondsToLive)))
                        .then(CommandManager.literal("announce_end")
                            .then(CommandManager.argument("announce_end", BoolArgumentType.bool())
                                .executes(VoteCommand::executeModifyAnnounceEnd)))
                        .then(CommandManager.literal("permission")
                            .then(CommandManager.argument("permission", StringArgumentType.string())
                                .executes(VoteCommand::executeModifyPermission)))))
                .then(CommandManager.literal("start")
                    .then(CommandManager.argument("name", StringArgumentType.word()).suggests(UNSTARTED_VOTE_NAME_SUGGESTION_PROVIDER)
                        .executes(VoteCommand::executeStart)))
                .then(CommandManager.literal("end")
                    .then(CommandManager.argument("name", StringArgumentType.word()).suggests(STARTED_VOTE_NAME_SUGGESTION_PROVIDER)
                        .executes(VoteCommand::executeEnd)))
                .then(CommandManager.literal("list")
                    .executes(VoteCommand::executeList))));
    }
}
