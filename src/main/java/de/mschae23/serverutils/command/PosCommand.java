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

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class PosCommand {
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

        BlockPos pos = BlockPos.ofFloored(context.getSource().getPosition());

        MutableText text = Text.empty().append(context.getSource().getDisplayName().copy()).append(Text.literal(" is at ")
            .append(ModUtils.getCoordinateText(pos)).append("."));

        if (inPublicChat) {
            context.getSource().getServer().getPlayerManager().broadcast(text, false);
        } else {
            context.getSource().sendFeedback(() -> text, false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
