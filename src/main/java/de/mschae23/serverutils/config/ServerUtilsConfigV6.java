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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.serverutils.config.command.CommandConfig;
import de.mschae23.serverutils.config.v1.ServerUtilsConfigV1;
import de.mschae23.serverutils.config.v2.ServerUtilsConfigV2;
import de.mschae23.serverutils.config.v3.ServerUtilsConfigV3;
import de.mschae23.serverutils.config.v4.ServerUtilsConfigV4;
import de.mschae23.serverutils.config.v5.ServerUtilsConfigV5;

public record ServerUtilsConfigV6(CommandConfig command,
                                  ChatConfig chat,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final MapCodec<ServerUtilsConfigV6> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV6::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV6::chat),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV6::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV6::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV6::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV6::new)));

    public static final ModConfig.Type<ServerUtilsConfigV6, ServerUtilsConfigV6> TYPE = new ModConfig.Type<>(6, TYPE_CODEC);
    @SuppressWarnings({"unchecked", "deprecation"})
    public static final ModConfig.Type<ServerUtilsConfigV6, ?>[] VERSIONS = new ModConfig.Type[] {
        ServerUtilsConfigV1.TYPE, ServerUtilsConfigV2.TYPE, ServerUtilsConfigV3.TYPE, ServerUtilsConfigV4.TYPE, ServerUtilsConfigV5.TYPE, ServerUtilsConfigV6.TYPE,
    };

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
