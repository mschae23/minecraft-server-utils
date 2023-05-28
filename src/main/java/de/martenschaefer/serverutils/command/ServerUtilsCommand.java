package de.martenschaefer.serverutils.command;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import de.martenschaefer.serverutils.ServerUtilsMod;
import com.mojang.brigadier.CommandDispatcher;
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
                .requires(Permissions.require(ServerUtilsMod.MODID + "command." + ServerUtilsMod.MODID + ".vote", true))
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeAdd)))
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeRemove)))
                .then(CommandManager.literal("modify")
                    .then(CommandManager.argument("name", StringArgumentType.word())))
                .then(CommandManager.literal("start")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(VoteCommand::executeStart)))));
    }
}
