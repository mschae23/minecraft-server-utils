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

package de.mschae23.serverutils.config;

import de.martenschaefer.config.api.ModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.serverutils.config.command.CommandConfig;

public record ServerUtilsConfigV6(CommandConfig command,
                                  ChatConfig chat,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final Codec<ServerUtilsConfigV6> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV6::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV6::chat),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV6::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV6::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV6::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV6::new)));

    public static final ModConfig.Type<ServerUtilsConfigV6, ServerUtilsConfigV6> TYPE = new ModConfig.Type<>(6, TYPE_CODEC);

    public static final ServerUtilsConfigV6 DEFAULT =
        new ServerUtilsConfigV6(CommandConfig.DEFAULT, ChatConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfig.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
