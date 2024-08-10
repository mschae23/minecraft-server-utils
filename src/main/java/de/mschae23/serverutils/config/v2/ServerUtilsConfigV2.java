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

package de.mschae23.serverutils.config.v2;

import de.martenschaefer.config.api.ModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.serverutils.config.BroadcastEntityDeathConfig;
import de.mschae23.serverutils.config.ChatConfig;
import de.mschae23.serverutils.config.ContainerLockConfig;
import de.mschae23.serverutils.config.DeathCoordsConfig;
import de.mschae23.serverutils.config.ServerUtilsConfigV6;
import de.mschae23.serverutils.config.VoteConfig;
import de.mschae23.serverutils.config.command.CommandConfig;
import de.mschae23.serverutils.config.v3.MiscConfigV3;
import de.mschae23.serverutils.config.v3.ServerUtilsConfigV3;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV2(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  MiscConfigV3 misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final Codec<ServerUtilsConfigV2> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV2::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV2::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV2::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV2::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV2::lock),
        MiscConfigV3.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV2::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV2::new)));

    public static final Type<ServerUtilsConfigV6, ServerUtilsConfigV2> TYPE = new Type<>(2, TYPE_CODEC);

    public static final ServerUtilsConfigV2 DEFAULT =
        new ServerUtilsConfigV2(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, MiscConfigV3.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV3(this.command, this.chat, this.deathCoords, this.broadcastEntityDeath, this.lock, VoteConfig.DEFAULT, this.misc).latest();
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
