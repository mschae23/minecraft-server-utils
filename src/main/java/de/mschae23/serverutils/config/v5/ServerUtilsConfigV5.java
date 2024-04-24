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

package de.mschae23.serverutils.config.v5;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.serverutils.config.ChatConfig;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.config.MiscConfig;
import de.mschae23.serverutils.config.ServerUtilsConfigV6;
import de.mschae23.serverutils.config.VoteConfig;
import de.mschae23.serverutils.config.command.CommandConfig;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV5(CommandConfig command,
                                  ChatConfig chat,
                                  RegionConfigV5 region,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final MapCodec<ServerUtilsConfigV5> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV5::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV5::chat),
        RegionConfigV5.CODEC.fieldOf("region").forGetter(ServerUtilsConfigV5::region),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV5::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV5::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV5::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV5::new)));

    public static final Type<ServerUtilsConfigV6, ServerUtilsConfigV5> TYPE = new Type<>(5, TYPE_CODEC);

    public static final ServerUtilsConfigV5 DEFAULT =
        new ServerUtilsConfigV5(CommandConfig.DEFAULT, ChatConfig.DEFAULT, RegionConfigV5.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfig.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV6(this.command, this.chat, this.lock, this.vote, this.misc);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
