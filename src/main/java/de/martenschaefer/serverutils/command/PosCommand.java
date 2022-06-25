package de.martenschaefer.serverutils.command;

import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class PosCommand {
    public static final String PERMISSION_ROOT = ".command.pos.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command.pos.public",
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pos")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, true)
                .and(ServerCommandSource::isExecutedByPlayer)) // /pos should be used by players, not the server console or command blocks
            .executes(PosCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        boolean inPublicChat = Permissions.check(context.getSource(), ServerUtilsMod.MODID + ".command.pos.public",
            ServerUtilsMod.getConfig().command().pos().inPublicChat());

        BlockPos pos = new BlockPos(context.getSource().getPosition());

        MutableText text = Text.empty().append(context.getSource().getDisplayName().copy()).append(Text.literal(" is at ")
            .append(ModUtils.getCoordinateText(pos)).append("."));

        if (inPublicChat) {
            context.getSource().getServer().getPlayerManager().broadcast(text, MessageType.SYSTEM);
        } else {
            context.getSource().sendFeedback(text, false);
        }

        return 15;
    }
}
