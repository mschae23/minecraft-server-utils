package de.martenschaefer.serverutils.command;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import de.martenschaefer.serverutils.ServerUtilsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class ServerUtilsCommand {
    public static final String PERMISSION_ROOT = ".command." + ServerUtilsMod.MODID + ".root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command." + ServerUtilsMod.MODID + ".vote",
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
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeRemove)))
                .then(CommandManager.literal("modify")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .then(CommandManager.literal("display_name")
                            .then(CommandManager.argument("display_name", StringArgumentType.greedyString())
                                .executes(VoteCommand::executeModifyDisplayName)))
                        .then(CommandManager.literal("option")
                            .then(CommandManager.literal("add")
                                .then(CommandManager.argument("option_name", StringArgumentType.word())
                                    .executes(VoteCommand::executeModifyAddOption)))
                            .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("option_name", StringArgumentType.word())
                                    .executes(VoteCommand::executeModifyRemoveOption)))
                            .then(CommandManager.literal("modify")
                                .then(CommandManager.literal("display_name")
                                    .then(CommandManager.argument("option_name", StringArgumentType.word())
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
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeStart)))
                .then(CommandManager.literal("end")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeEnd)))
                .then(CommandManager.literal("list")
                    .executes(VoteCommand::executeList))));
    }
}
