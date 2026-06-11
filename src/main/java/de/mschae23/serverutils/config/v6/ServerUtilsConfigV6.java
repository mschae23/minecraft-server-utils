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

package de.mschae23.serverutils.config.v6;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.serverutils.config.ChatConfig;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.config.ServerUtilsConfigV7;
import de.mschae23.serverutils.config.VoteConfig;
import de.mschae23.serverutils.config.command.CommandConfig;

@Deprecated
public record ServerUtilsConfigV6(CommandConfig command,
                                  ChatConfig chat,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfigV6 misc) implements ModConfig<ServerUtilsConfigV7> {
    public static final MapCodec<ServerUtilsConfigV6> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV6::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV6::chat),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV6::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV6::vote),
        MiscConfigV6.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV6::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV6::new)));

    public static final ModConfig.Type<ServerUtilsConfigV7, ServerUtilsConfigV6> TYPE = new ModConfig.Type<>(6, TYPE_CODEC);

    public static final ServerUtilsConfigV6 DEFAULT =
        new ServerUtilsConfigV6(CommandConfig.DEFAULT, ChatConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfigV6.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV7, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV7 latest() {
        return new ServerUtilsConfigV7(this.command, this.chat, this.lock, this.vote, this.misc.latest());
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
